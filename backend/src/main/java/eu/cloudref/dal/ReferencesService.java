package eu.cloudref.dal;

import eu.cloudref.db.User;
import eu.cloudref.models.MergeInstruction;
import eu.cloudref.models.Rating;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.jbibtex.*;

import java.io.*;
import java.util.*;

import difflib.Delta;
import difflib.DiffUtils;

public class ReferencesService {

    private static final int THRESHOLD_CONFIRMATION = 3;
    private static final int THRESHOLD_MIN = -2;
    private static final int THRESHOLD_MAX = 2;

    public static List<BibTeXEntry> getReferences() {
        List<BibTeXEntry> result = new ArrayList<>();
        GitService.getLock().readLock().lock();
        try {
            result = BibService.loadBibs();
        } finally {
            GitService.getLock().readLock().unlock();
        }
        return result;
    }

    public static BibTeXEntry getReference(String bibtexkey, boolean modified) {
        BibTeXEntry result;
        GitService.getLock().readLock().lock();
        try {
            result = BibService.loadBib(bibtexkey, modified);
        } finally {
            GitService.getLock().readLock().unlock();
        }
        return result;
    }

    /**
     * Check if the crossref exists in the system.
     *
     * @param newReference the reference to insert which possibly contains a field 'crossrefString'.
     * @return the bibtexkey of the cross reference if the reference does not exists in the system, null otherwise.
     */
    public static String getCrossrefKeyIfReferenceNotExists(BibTeXEntry newReference) {
        Value crossrefValue = newReference.getField(new Key("crossrefString"));
        if (crossrefValue != null) {
            String crossref = crossrefValue.toUserString();
            if (!crossref.equals("")) {
                if (ReferencesService.getReference(crossref, false) == null) {
                    return crossref;
                }
            }
        }
        return null;
    }

    /**
     * Save or update a reference.
     *
     * @param newReference a new or changed reference.
     * @param user         the user who posted the reference.
     * @return Integer - committed BibTeXEntry.
     */
    public static Integer saveReference(BibTeXEntry newReference, User user) {
        return GitService.commitFile(user, newReference);
    }

    public static boolean saveReferences(InputStream uploadedInputStream, User user) {
        boolean result = false;
        if (uploadedInputStream != null && user != null) {
            GitService.getLock().writeLock().lock();
            try {
                result = BibService.saveReferences(uploadedInputStream, user);
            } finally {
                GitService.getLock().writeLock().unlock();
            }
        }
        return result;
    }

    private static MergeInstruction.MergeEnum thresholdMergeReached(List<eu.cloudref.db.Rating> rating) {

        if (rating != null && !rating.isEmpty()) {
            // get rating
            int rate = calculateRating(rating);
            int id = rating.get(0).getId();
            String bibtexkey = rating.get(0).getBibtexkey();

            // check if a threshold is reached
            if (rate <= THRESHOLD_MIN) {
                // reject suggestion
                GitService.mergeSuggestion(false, bibtexkey, id, false);
                return MergeInstruction.MergeEnum.REJECT;
            } else if (rate >= THRESHOLD_MAX) {
                // accept suggestion
                GitService.mergeSuggestion(true, bibtexkey, id, false);
                return MergeInstruction.MergeEnum.ACCEPT;
            }
        }
        return null;
    }

    private static boolean thresholdConfirmationReached(List<eu.cloudref.db.Rating> rating) {

        if (rating != null && !rating.isEmpty()) {
            // get rating
            int rate = calculateRating(rating);

            if (rate >= THRESHOLD_CONFIRMATION) {
                return true;
            }
        }
        return false;
    }

    static private int calculateRating(List<eu.cloudref.db.Rating> rating) {
        int rate = 0;
        if (rating != null && !rating.isEmpty()) {
            for (eu.cloudref.db.Rating r : rating) {
                switch (r.getRating()) {
                    case POSITIVE:
                        rate += 1;
                        break;
                    case NEGATIVE:
                        rate -= 1;
                        break;
                }
            }
        }
        return rate;
    }

    public static int getRatingSuggestion(String bibtexkey, int id) {
        List<eu.cloudref.db.Rating> ratings = getSuggestionRating(bibtexkey, id);
        return calculateRating(ratings);
    }

    public static int getRatingReference(String bibtexkey) {
        List<eu.cloudref.db.Rating> ratings = getSuggestionRating(bibtexkey, 0);
        return calculateRating(ratings);
    }

    public static String[] getRatingsReference(String bibtexkey, String username) {
        String[] result = new String[3];
        List<eu.cloudref.db.Rating> ratings = getSuggestionRating(bibtexkey, 0);

        result[0] = String.valueOf(thresholdConfirmationReached(ratings));
        result[1] = String.valueOf(calculateRating(ratings));
        result[2] = String.valueOf(getUserRating(ratings, username));

        return result;
    }

    /**
     * Delete the rating of a reference in the database.
     *
     * @param bibtexkey the BibTeX key of the reference.
     * @param session   Session object.
     */
    private static void deleteRatingReference(String bibtexkey, Session session) {
        deleteRatingDB(bibtexkey, session, 0);
    }

    /**
     * Delete the rating of a suggestion in the database.
     *
     * @param bibtexkey the BibTeX key of the reference.
     * @param id        the identifier of the suggestion.
     */
    public static void deleteRatingSuggestion(String bibtexkey, int id) {

        Session session = null;
        Transaction tx = null;
        try {
            session = DBService.getSessionFactory().openSession();
            tx = session.beginTransaction();

            deleteRatingDB(bibtexkey, session, id);

            session.flush();
            tx.commit();

        } catch (Exception var11) {
            var11.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void deleteRatingDB(String bibtexkey, Session session, int id) {
        Criteria criteria = session.createCriteria(eu.cloudref.db.Rating.class);
        criteria.add(Restrictions.like("bibtexkey", bibtexkey));
        criteria.add(Restrictions.like("id", id));

        List result = criteria.list();
        if (result == null) {
            return;
        }
        List<eu.cloudref.db.Rating> res = (List<eu.cloudref.db.Rating>) result;
        for (eu.cloudref.db.Rating r : res) {
            session.delete(r);
        }
        session.flush();
    }

    public static void rateReference(String bibtexkey, Rating.RatingEnum rating, User user) {
        rateReferenceOrSuggestion(bibtexkey, 0, rating, user);
    }

    public static MergeInstruction.MergeEnum rateSuggestion(String bibtexkey, int id, Rating.RatingEnum rating, User user) {
        if (bibtexkey != null && rating != null && user != null && id > 0) {
            return rateReferenceOrSuggestion(bibtexkey, id, rating, user);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Rate a reference or suggestion to modify a reference.
     *
     * @param bibtexkey the bibtexkey of the reference/suggestion.
     * @param id        0 if reference, > 1 if suggestion.
     * @param rating    the rating of the user.
     * @param user      the user who rates the reference/suggestion.
     */
    @SuppressWarnings("unchecked")
    private static MergeInstruction.MergeEnum rateReferenceOrSuggestion(String bibtexkey, int id, Rating.RatingEnum rating, User user) {
        MergeInstruction.MergeEnum merged = null;
        Session session = null;
        Transaction tx = null;
        try {
            session = DBService.getSessionFactory().openSession();
            tx = session.beginTransaction();

            eu.cloudref.db.Rating r = new eu.cloudref.db.Rating(user, bibtexkey, id, rating);

            session.saveOrUpdate(r);

            if (id != 0) {
                //check if threshold is reached
                Criterion key = Restrictions.like("bibtexkey", bibtexkey);
                Criterion identifier = Restrictions.like("id", id);

                Criteria criteria = session.createCriteria(eu.cloudref.db.Rating.class);
                criteria.add(Restrictions.and(key, identifier));

                List<eu.cloudref.db.Rating> rate = criteria.list();
                merged = thresholdMergeReached(rate);

                if (merged != null && merged == MergeInstruction.MergeEnum.ACCEPT) {
                    System.out.println("DELETE RATINGS REFERENCE");
                    // delete entries for confirmation of reference
                    deleteRatingReference(bibtexkey, session);
                }
            }

            session.flush();
            tx.commit();

        } catch (Exception var11) {
            var11.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return merged;
    }

    public static boolean isConfirmed(String bibtexkey) {
        return thresholdConfirmationReached(getSuggestionRating(bibtexkey, 0));
    }

    private static Rating.RatingEnum getUserRating(List<eu.cloudref.db.Rating> ratings, String username) {
        Rating.RatingEnum result = null;

        if (username != null && ratings != null && !ratings.isEmpty()) {
            for (eu.cloudref.db.Rating r : ratings) {
                if (r.getUsername().equals(username)) {
                    result = r.getRating();
                    break;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<eu.cloudref.db.Rating> getSuggestionRating(String bibtexkey, int id) {
        List<eu.cloudref.db.Rating> result = new ArrayList<>();
        Session session = null;
        Transaction tx = null;
        try {
            session = DBService.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // get rating
            Criterion key = Restrictions.like("bibtexkey", bibtexkey);
            Criterion identifier = Restrictions.like("id", id);

            Criteria criteria = session.createCriteria(eu.cloudref.db.Rating.class);
            criteria.add(Restrictions.and(key, identifier));

            Object res = criteria.list();
            Hibernate.initialize(res);
            if (res != null) {
                result = (List<eu.cloudref.db.Rating>) res;
            }
            tx.commit();

        } catch (Exception var11) {
            var11.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return result;
    }

    public static List<Map<String, String>> getSuggestions(String bibtexkey, User user) {
        List<Map<String, String>> result = new ArrayList<>();

        // get reference from master branch
        Map<String, String> master = new HashMap<>();
        BibTeXEntry masterEntry = getReference(bibtexkey, false);
        String masterKey = masterEntry.getKey().toString();
        master.put("BibTeX key", masterKey);
        String masterType = masterEntry.getType().toString();
        master.put("Type", masterType);

        // get all fields of reference on master branch
        for (Map.Entry<Key, Value> entry : masterEntry.getFields().entrySet()) {
            // get field name with first character in uppercase
            String fieldName = getFirstLetterUppercase(entry.getKey().toString());
            master.put(fieldName, entry.getValue().toUserString());
        }
        // add reference to result
        result.add(master);

        // get suggestion IDs from git
        List<Integer> suggestions = GitService.getSuggestionIDs(bibtexkey);
        // generate diff and get rating of user if available
        for (Integer suggestionId : suggestions) {
            // simulate merge of suggestion
            BibTeXEntry e = GitService.mergeSuggestion(true, bibtexkey, suggestionId, true);
            Map<String, String> m = new HashMap<>();

            // get key
            String modK = e.getKey().toString();
            // get word diff
            String rK = generateDiffHighlighting(masterKey, modK, " ");
            // add to result
            m.put("BibTeX key", rK);

            // get type
            String modT = e.getType().toString();
            // get word diff
            String rT = generateDiffHighlighting(masterType, modT, " ");
            // add to result
            m.put("Type", rT);

            // get diff of all fields of suggestion
            for (Map.Entry<Key, Value> entry : e.getFields().entrySet()) {
                // get value of field
                String modifiedString = entry.getValue().toUserString();
                // get value of field from reference on master branch
                // test if it exists
                String baseString = "";
                Value v = masterEntry.getField(entry.getKey());
                if (v != null) {
                    baseString = v.toUserString();
                }
                // get word diff of both
                String r = generateDiffHighlighting(baseString, modifiedString, " ");

                // get field name with first character in uppercase
                String fieldName = getFirstLetterUppercase(entry.getKey().toString());
                m.put(fieldName, r);
            }

            // check if reference on master has additional fields -> removed at branch
            for (Key fieldKey : masterEntry.getFields().keySet()) {
                // get field name with first character in uppercase
                String fieldName = getFirstLetterUppercase(fieldKey.toString());
                // check if field is not in result already
                if (m.get(fieldName) == null) {
                    // set field to empty string because it does not exist
                    String modifiedString = "";
                    // get value of field from reference on master branch
                    String baseString = masterEntry.getField(fieldKey).toUserString();

                    // get word diff of both
                    String r = generateDiffHighlighting(baseString, modifiedString, " ");
                    m.put(fieldName, r);
                }
            }

            // get rating of all users
            List<eu.cloudref.db.Rating> r = getSuggestionRating(bibtexkey, suggestionId);
            if (r == null || r.isEmpty()) {
                // rating is 0
                m.put("OverallRating", String.valueOf(0));
            } else {
                // get rating
                m.put("OverallRating", String.valueOf(calculateRating(r)));

                // get rating of user if available
                for (eu.cloudref.db.Rating ra : r) {
                    if (ra.getUsername().equals(user.getName())) {
                        Rating.RatingEnum uR = ra.getRating();
                        if (uR != null) {
                            m.put("RatedByUser", uR.toString());
                        }
                    }
                }
            }

            // add to result with id of suggestion
            m.put("ID", String.valueOf(suggestionId));
            result.add(m);
        }

        return result;
    }

    private static String getFirstLetterUppercase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /*
    Following source code is copied from JabRef: https://github.com/JabRef/jabref/blob/b0489d5b381275dbc7fd2d35fccf52aa4d7093c7/src/main/java/org/jabref/logic/util/strings/DiffHighlighting.java#L25
     */
    private static final String ADDITION_START = "<span class=add>";
    private static final String REMOVAL_START = "<span class=del>";
    private static final String TAG_END = "</span>";

    private static String generateDiffHighlighting(String baseString, String modifiedString, String separator) {
        Objects.requireNonNull(separator);
        if ((baseString != null) && (modifiedString != null)) {
            List<String> stringList = new ArrayList<>(Arrays.asList(baseString.split(separator)));
            List<Delta<String>> deltaList = new ArrayList<>(
                    DiffUtils.diff(stringList, Arrays.asList(modifiedString.split(separator))).getDeltas());
            Collections.reverse(deltaList);
            for (Delta<String> delta : deltaList) {
                int startPos = delta.getOriginal().getPosition();
                List<String> lines = delta.getOriginal().getLines();
                int offset = 0;
                switch (delta.getType()) {
                    case CHANGE:
                        for (String line : lines) {
                            stringList.set(startPos + offset, (offset == 0 ? REMOVAL_START : "") + line);
                            offset++;
                        }
                        stringList.set((startPos + offset) - 1,
                                stringList.get((startPos + offset) - 1) + TAG_END + separator + ADDITION_START
                                        + String.join(separator, delta.getRevised().getLines()) + TAG_END);
                        break;
                    case DELETE:
                        for (String line : lines) {
                            stringList.set(startPos + offset, (offset == 0 ? REMOVAL_START : "") + line);
                            offset++;
                        }
                        stringList.set((startPos + offset) - 1,
                                stringList.get((startPos + offset) - 1) + TAG_END);
                        break;
                    case INSERT:
                        stringList.add(delta.getOriginal().getPosition(),
                                ADDITION_START + String.join(separator, delta.getRevised().getLines()) + TAG_END);
                        break;
                    default:
                        break;
                }
            }
            return String.join(separator, stringList);
        }
        return modifiedString;
    }

    /**
     * Merge a suggestion into the master branch. Merge is initiated by a user.
     *
     * @param bibtexkey            the BibTeX key of a reference.
     * @param id                   the identifier of the suggestion.
     * @param mergeEnum accept or reject the suggestion.
     * @param user                 the user who initiates the merge.
     * @return true if merged successfully, false otherwise.
     */
    public static boolean mergeSuggestion(String bibtexkey, int id,
                                          MergeInstruction.MergeEnum mergeEnum, User user) {
        BibTeXEntry result = null;

        if (bibtexkey != null && id > 0 && mergeEnum != null && user != null) {
            switch (mergeEnum) {
                case ACCEPT:
                    result = GitService.mergeSuggestion(true, bibtexkey, id, false, user);
                    // delete ratings for reference
                    deleteRatingSuggestion(bibtexkey, 0);
                    break;
                case REJECT:
                    result = GitService.mergeSuggestion(false, bibtexkey, id, false, user);
                    break;
            }
        }

        if (result != null) {
            return true;
        } else {
            return false;
        }
    }
}
