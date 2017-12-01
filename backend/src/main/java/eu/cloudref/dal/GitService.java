package eu.cloudref.dal;

import eu.cloudref.Configuration;
import eu.cloudref.db.User;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jbibtex.BibTeXEntry;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GitService {

    private static final PersonIdent CLOUD_REF = new PersonIdent("CloudRef", "");
    // use UNIX (LF) because it is used from jBibTeX
    private static final String LINE_SEPARATOR = "\n";

    private static ReadWriteLock lock = null;

    public static ReadWriteLock getLock() {
        if (lock == null) {
            lock = new ReentrantReadWriteLock();
        }
        return lock;
    }

    /**
     * Get the directory of the repository with the .bib files.
     *
     * @return String - location of the repository.
     */
    static public String getRepositoryDirectory() {
        return Configuration.getCloudRefDirectory() + "bibDirectory/";
    }

    static private Git getGit() {
        File repo = new File(getRepositoryDirectory());
        try {
            // check if repository exists already
            File gitDir = new File(getRepositoryDirectory() + ".git");
            if (!gitDir.exists()) {
                // create repository
                return Git.init().setDirectory(repo).call();
            } else {
                // open repository
                return Git.open(repo);
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Commit a new or changed .bib file to the repository.
     *
     * @param user  - the user who posted the entry.
     * @param entry - a new or changed BibTeX entry.
     * @return committed BibTeXEntry
     */
    static public Integer commitFile(User user, BibTeXEntry entry) {
        Integer result = null;
        Git git = getGit();
        if (git != null) {
            String bibtexkey = entry.getKey().getValue();
            // get write lock before writing file or switching branch
            getLock().writeLock().lock();
            // check if a update has to be performed
            boolean update = BibService.bibtexkeyExists(bibtexkey);
            try {
                if (!update) {
                    // save and commit new reference file
                    String commitMessage = "Add reference with bibtexkey '" + bibtexkey + "'.";
                    saveAndCommit(entry, git, user, bibtexkey, commitMessage);
                    result = 0;
                } else {
                    // commit change request
                    // find id for suggestion for modification
                    List<Ref> refs = git.branchList().call();
                    int branchId = 0;
                    String branch = bibtexkey + "/";
                    Ref ref = null;
                    do {
                        branchId++;
                        ref = existsBranch(refs, branch + branchId);
                    } while (ref != null);
                    branch += branchId;

                    // create branch
                    git.branchCreate().setName(branch).call();

                    // checkout branch
                    git.checkout().setName(branch).call();

                    // save modified reference file and commit it
                    String commitMessage = "Edit reference with bibtexkey '" + bibtexkey + "'.";
                    saveAndCommit(entry, git, user, bibtexkey, commitMessage);
                    result = branchId;

                    // switch to master branch
                    git.checkout().setName("master").call();
                }
            } catch (GitAPIException e) {
                e.printStackTrace();
            } finally {
                getLock().writeLock().unlock();
                git.close();
            }
        } else {
            throw new NullPointerException("Git repository is null");
        }
        return result;
    }

    static public boolean commitSuggestion(User user, BibTeXEntry entry, int id) {
        Git git = getGit();
        if (git != null) {
            String bibtexkey = entry.getKey().getValue();
            // get write lock before writing file or switching branch
            getLock().writeLock().lock();
            try {
                    // commit changed suggestion
                    if (existsSuggestionPrivate(bibtexkey, id)) {
                        // checkout branch
                        String branch = bibtexkey + "/" + id;
                        git.checkout().setName(branch).call();

                        // save modified reference file and commit it
                        String commitMessage = "Edit suggestion '" + id + "' of reference with bibtexkey '" + bibtexkey + "'.";
                        saveAndCommit(entry, git, user, bibtexkey, commitMessage);

                        return true;
                    }

            } catch (GitAPIException e) {
                e.printStackTrace();
            } finally {
                try {
                    // switch to master branch
                    git.checkout().setName("master").call();
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }

                getLock().writeLock().unlock();
                git.close();
            }
        } else {
            throw new NullPointerException("Git repository is null");
        }
        return false;
    }

    static private BibTeXEntry saveAndCommit(BibTeXEntry entry, Git git, User user, String bibtexkey, String commitMessage) throws GitAPIException {
        // save BibTeXEntry
        BibTeXEntry result = BibService.saveBib(entry);
        // add to stage
        DirCache index = git.add().addFilepattern(bibtexkey + ".bib").call();
        // perform commit
        commit(git, user, commitMessage);

        return result;
    }

    static private void commit(Git git, User user, String commitMessage) throws GitAPIException {
        // commit file
        CommitCommand command = git.commit();
        command.setMessage(commitMessage);
        command.setAuthor(user.getName(), user.getEmail());
        command.setCommitter(CLOUD_REF);
        RevCommit commit = command.call();
    }

    /**
     * Commit the entries of a imported .bib file.
     *
     * @param insertedKeys the bibtexkeys of the references in the .bib file.
     * @param user         the user who imported the .bib file.
     */
    static public void commitImportedBibFile(List<String> insertedKeys, User user) {
        Git git = getGit();
        if (git != null) {
            try {
                // commit files
                String commitMessage = "Add references with bibtexkeys:";
                for (String bibtexkey : insertedKeys) {
                    DirCache index = git.add().addFilepattern(bibtexkey + ".bib").call();
                    commitMessage += "\n - " + bibtexkey;
                }
                commit(git, user, commitMessage);
            } catch (GitAPIException e) {
                e.printStackTrace();
            } finally {
                git.close();
            }
        }
    }

    /**
     * Check if a suggestion exists with the given ID for a BibTeX-key.
     *
     * @param bibtexkey the bibtexkey of the reference.
     * @param id        the ID of the suggestion.
     * @return true if it exists and is not merged, false otherwise.
     */
    static public boolean existsSuggestion(String bibtexkey, int id) {
        getLock().readLock().lock();
        try {
            return existsSuggestionPrivate(bibtexkey, id);
        } finally {
            getLock().readLock().unlock();
        }
    }

    /**
     * Intern method to check if a suggestion exists. Lock has to be acquired before.
     *
     * @param bibtexkey the bibtexkey of the reference.
     * @param id        the ID of the suggestion.
     * @return true if it exists and is not merged, false otherwise.
     */
    static private boolean existsSuggestionPrivate(String bibtexkey, int id) {
        Git git = getGit();
        if (git != null) {
            try {
                // check if branch exists
                List<Ref> refs = git.branchList().call();
                Ref branch = existsBranch(refs, bibtexkey + "/" + id);
                if (branch != null) {
                    // check if branch was already merged
                    if (!isBranchMergedIntoMaster(git, branch)) {
                        return true;
                    }
                }
            } catch (GitAPIException e) {
                e.printStackTrace();
            } catch (IncorrectObjectTypeException e) {
                e.printStackTrace();
            } catch (MissingObjectException e) {
                e.printStackTrace();
            } catch (AmbiguousObjectException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Check if a branch was already merged into master branch.
     *
     * @param git    the Git repository.
     * @param branch the branch of the repository.
     * @return true if merged into master, false otherwise.
     * @throws IOException
     */
    static private boolean isBranchMergedIntoMaster(Git git, Ref branch) throws IOException {
        if (git != null && branch != null) {
            Repository repository = git.getRepository();
            if (repository != null) {
                RevWalk rev = new RevWalk(repository);

                if (rev != null) {
                    RevCommit tip = rev.parseCommit(repository.resolve("refs/heads/master"));
                    RevCommit base = rev.parseCommit(repository.resolve(branch.getName()));

                    if (tip != null && base != null) {
                        if (rev.isMergedInto(base, tip)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    static private Ref existsBranch(List<Ref> refs, String branchName) {
        Ref branch = null;
        for (Ref ref : refs) {
            if (ref.getName().equals("refs/heads/" + branchName)) {
                branch = ref;
                break;
            }
        }
        return branch;
    }

    /**
     * Merge a suggestion for modification into the master branch of the repository.
     *
     * @param accepted true if the suggestion is accepted by the users, false otherwise.
     * @param bibtexkey the bibtexkey of the reference which is changed in the suggestion.
     * @param id the identifier of the suggestion.
     * @param simulate true if the merge should not be committed but the changes withdrawn, false otherwise.
     * @return BibTeXEntry - the result of the merge.
     */
    static public BibTeXEntry mergeSuggestion(boolean accepted, String bibtexkey, int id, boolean simulate) {
        return mergeSuggestion(accepted, bibtexkey, id, simulate, null);
    }

    /**
     * Merge a suggestion for modification into the master branch of the repository.
     *
     * @param accepted true if the suggestion is accepted by the users, false otherwise.
     * @param bibtexkey the bibtexkey of the reference which is changed in the suggestion.
     * @param id the identifier of the suggestion.
     * @param simulate true if the merge should not be committed but the changes withdrawn, false otherwise.
     * @param user the User who initiates the merge.
     * @return BibTeXEntry - the result of the merge.
     */
    static public BibTeXEntry mergeSuggestion(boolean accepted, String bibtexkey, int id, boolean simulate, User user) {

        Git git = getGit();
        if (git != null && bibtexkey != null) {
            getLock().writeLock().lock();
            try {
                // check if suggestion exists before merging
                if (existsSuggestionPrivate(bibtexkey, id)) {

                    // check if branch exists
                    List<Ref> refs = git.branchList().call();
                    String branchName = bibtexkey + "/" + id;
                    Ref branch = existsBranch(refs, branchName);
                    if (branch == null) {
                        // suggestion identifier does not exist for bibtexkey
                        return null;
                    }

                    // merge branch into master
                    MergeCommand merge = git.merge();
                    merge.include(branch);
                    merge.setMessage("Merge suggestion " + id + " for reference '" + bibtexkey + "'.");
                    merge.setCommit(false);
                    merge.setFastForward(MergeCommand.FastForwardMode.NO_FF);
                    // set merge strategy
                    if (accepted) {
                        merge.setStrategy(MergeStrategy.RECURSIVE);
                    } else {
                        merge.setStrategy(MergeStrategy.OURS);
                    }
                    // perform merge
                    MergeResult res = merge.call();

                    if (res.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                        // resolve conflicts
                        for (String path : res.getConflicts().keySet()) {
                            System.out.println("CONFLICTS IN FILE '" + path + "':");
                            resolveConflicts(getRepositoryDirectory() + path, bibtexkey, id);
                        }

                        // Stage all files in the repo including new files
                        git.add().addFilepattern(".").call();

                    }

                    BibTeXEntry result = ReferencesService.getReference(bibtexkey, false);

                    // commit or delete changes of merge depending if simulation or not
                    if (!simulate) {
                        // commit merge
                        CommitCommand command = git.commit();
                        String commitMessage = "Merge suggestion " + id + " for reference '" + bibtexkey + "'.";
                        command.setMessage(commitMessage);
                        command.setCommitter(CLOUD_REF);
                        if (user != null) {
                            command.setAuthor(user.getName(), user.getEmail());
                        } else {
                            command.setAuthor(CLOUD_REF);
                        }
                        RevCommit commit = command.call();
                        System.out.println("COMMIT");

                    } else {
                        // delete files in stage
                        git.reset().setMode(ResetCommand.ResetType.HARD).call();
                        System.out.println("RESET STAGING AREA");
                    }

                    return result;
                }
            } catch (GitAPIException e) {
                e.printStackTrace();
            } finally {
                getLock().writeLock().unlock();
                git.close();
            }
        }

        return null;
    }

    /**
     * Resolve file conflicts. Use all fields modified by suggestion.
     *
     * @param fileLocation the file which conflicts have to be resolved.
     * @param bibtexkey    the bibtexkey of the reference the suggestion belongs to.
     * @param id           the identifier of the suggestion.
     */
    private static void resolveConflicts(String fileLocation, String bibtexkey, int id) {
        BufferedReader in = null;
        BufferedWriter out = null;

        try {
            File file = new File(fileLocation);

            FileReader reader = new FileReader(file);
            in = new BufferedReader(reader);
            String line;
            List<String> result = new ArrayList<>();

            // get the field names which were deleted at the branch of the suggestion
            Set<String> changedFieldsOfBranch = getChangedFieldsOfBranch(bibtexkey, id);

            Region region = Region.NON_CONFLICT;

            System.out.println("START FILE");
            // read file and resolve conflict
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("<<<<<<<")) {
                    // region of master starts
                    region = Region.MASTER;
                } else if (line.startsWith("=======")) {
                    // region of branch starts
                    region = Region.BRANCH;
                } else if (line.startsWith(">>>>>>>")) {
                    // end of conflicting area
                    region = Region.NON_CONFLICT;
                } else {
                    /* line contains some content, e.g.
                        - @<type>{<bibtexkey>,
                        - <fieldname> = {<content>},
                        - }

                        Closing curly bracket is added at the end.
                     */
                    if (!line.matches("\\s*}\\s*")) {

                        // check if line contains field or type
                        String fieldname = null;
                        if (!line.matches("\\s*@.+\\{.+,")) {
                            String[] splitted = line.split("=");
                            if (splitted.length > 1) {
                                fieldname = (splitted[0]).trim();
                            }
                        }

                        switch (region) {
                            case NON_CONFLICT:
                                // no conflicting area -> add to result
                                result.add(line);
                                break;
                            case MASTER:
                                // conflicting area -> resolve
                                if (fieldname != null) {
                                    // check if field was changed on branch
                                    if (!changedFieldsOfBranch.contains(fieldname)) {
                                        result.add(line);
                                    }
                                } else {
                                    // check if type was changed on branch
                                    if (!changedFieldsOfBranch.contains("type")) {
                                        result.add(line);
                                    }
                                }
                                break;
                            case BRANCH:
                                // conflicting area -> resolve
                                if (fieldname != null) {
                                    // check if field was changed on branch
                                    if (changedFieldsOfBranch.contains(fieldname)) {
                                        result.add(line);
                                    }
                                } else {
                                    // check if type was changed on branch
                                    if (changedFieldsOfBranch.contains("type")) {
                                        result.add(line);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
            System.out.println("END FILE");
            // close input
            in.close();

            // add closing curly bracket to result
            result.add("}");

            // write changed file to disk
            FileWriter writer = new FileWriter(file);
            out = new BufferedWriter(writer);
            int i = 0;
            for (String s : result) {
                // append line separator if last line is not reached
                if (i < result.size() - 1) {
                    if (i == result.size() - 2) {
                        // remove comma at line end
                        if (!s.endsWith(",")) {
                            out.write(s + LINE_SEPARATOR);
                        } else {
                            out.write(s.substring(0, s.length() - 1) + LINE_SEPARATOR);
                        }
                    } else {
                        // add missing comma at line end
                        if (!s.endsWith(",")) {
                            out.write(s + "," + LINE_SEPARATOR);
                        } else {
                            out.write(s + LINE_SEPARATOR);
                        }
                    }
                } else {
                    // write last line of file
                    out.write(s);
                }
                i++;
            }
            out.flush();
            // close output
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close readers/writers if not already done
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    enum Region {
        NON_CONFLICT, MASTER, BRANCH
    }

    /**
     * Get all available suggestions for a bibtexkey.
     *
     * @param bibtexkey the bibtexkey of the reference.
     * @return all suggestions with that bibtexkey which are not already merged. Key of map is ID of suggestion.
     */
    public static List<Integer> getSuggestionIDs(String bibtexkey) {
        List<Integer> result = new ArrayList<>();
        Git git = getGit();
        if (git != null) {
            getLock().readLock().lock();
            try {
                List<Ref> refs = git.branchList().call();
                if (refs.isEmpty()) {
                    return result;
                }
                // find all branches of suggestions
                Repository repository = git.getRepository();
                for (Ref ref : refs) {
                    if (ref.getName().startsWith("refs/heads/" + bibtexkey + "/")) {
                        // check if branch is already merged into master
                        if (!isBranchMergedIntoMaster(git, ref)) {
                            // get branch name
                            String name = ref.getName().substring(11);

                            // get suggestion id from branch name
                            Integer identifier = Integer.valueOf(name.substring(name.indexOf("/") + 1));
                            result.add(identifier);
                        }
                    }
                }

                return result;

            } catch (GitAPIException | IOException e) {
                e.printStackTrace();
            } finally {
                getLock().readLock().unlock();
                git.close();
            }
        }

        return result;
    }

    /**
     * Get all field names which are modified on the branch of the suggestion.
     *
     * @param bibtexkey the bibtexkey of the reference.
     * @param id        the identifier of the suggestion.
     * @return Set<String> which contains all field names which are modified on the branch of the suggestion.
     */
    private static Set<String> getChangedFieldsOfBranch(String bibtexkey, int id) {
        Set<String> result = new HashSet<>();

        // get diff branch HEAD and parent at master
        String diff = getDiffOfBranchHEADAndParentAtMaster(bibtexkey, id);

        // get single lines of diff
        List<String> lines = Arrays.asList(diff.split("[\\r\\n]+"));

        /* line contains some content, e.g.
                @<type>{<bibtexkey>,
                    <fieldname> = {<content>},
                    <fieldname> = {<content>}
                }
        */
        for (String line : lines) {
            // find changed fields
            if (line.matches("(\\+|-)\\s*@.+\\{.+,")) {
                // line contains type and bibtexkey
                result.add("type");
            } else if (line.matches("(\\+|-)\\s*.+=\\s*\\{.+},?")) {
                // line contains field

                // check if only comma at line end changed
                String start = "";
                switch (line.substring(0, 1)) {
                    case "-":
                        start = "+";
                        break;
                    case "+":
                        start = "-";
                        break;
                }

                if ( (line.endsWith(",") && !lines.contains(start + line.substring(1, line.length() - 1)) ) ||
                        (!line.endsWith(",") && !lines.contains(start + line.substring(1, line.length()) + ",")) ) {
                    // line changed at branch
                    String fieldname = line.substring(1, line.indexOf("=")).trim();
                    result.add(fieldname);

                }
            }
        }

        return result;
    }

    /**
     * Find parent commit of branch at master.
     *
     * @param walk
     * @param commit
     * @return
     */
    private static RevCommit findParentAtMasterBranch(RevWalk walk, RevCommit commit) throws IOException {
        Git git = getGit();
        Repository repository;
        if (git != null && (repository = git.getRepository()) != null) {
            try {
                boolean parentFound = false;
                while (commit != null && !parentFound) {
                    RevCommit[] parents = commit.getParents();
                    if (parents != null && parents.length > 0) {
                        commit = walk.parseCommit(parents[0]);

                        for (Map.Entry<String, Ref> e : repository.getAllRefs().entrySet())
                            if (e.getKey().startsWith(Constants.R_HEADS)) {
                                if (walk.isMergedInto(commit,
                                        walk.parseCommit(e.getValue().getObjectId()))) {
                                    if (e.getValue().getName().equals("refs/heads/master")) {
                                        System.out.println("PARENT: " + commit);
                                        parentFound = true;
                                        break;
                                    }
                                }
                            }
                    } else {
                        commit = null;
                    }
                }
            } finally {
                git.close();
            }
        } else {
            return null;
        }

        return commit;
    }

    /**
     * Get diff of branch HEAD and parent at master.
     *
     * @param bibtexkey the bibtexkey of the reference.
     * @param id        the identifier of the suggestion.
     * @return String which contains the diff of the two commits.
     */
    private static String getDiffOfBranchHEADAndParentAtMaster(String bibtexkey, int id) {
        Git git = getGit();
        try {
            String branch = bibtexkey + "/" + id;
            Repository repository = git.getRepository();

            // get parent commit of branch at master
            Ref headBranch = repository.exactRef("refs/heads/" + branch);
            RevCommit commitBranch;
            RevCommit commit;

            RevWalk walk = new RevWalk(repository);
            commitBranch = walk.parseCommit(headBranch.getObjectId());
            commit = findParentAtMasterBranch(walk, commitBranch);
            walk.dispose();

            // get diff of branch head and parent on master
            ObjectReader reader = repository.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, commit.getTree().getId());
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, commitBranch.getTree().getId());

            OutputStream outputStream = new ByteArrayOutputStream();
            // get diff of reference only
            git.diff().setOldTree(oldTreeIter)
                    .setNewTree(newTreeIter)
                    .setOutputStream(outputStream)
                    .setPathFilter(PathFilter.create(bibtexkey + ".bib")).call();

            System.out.println(outputStream.toString());
            return outputStream.toString();

        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        } finally {
            git.close();
        }

        return null;
    }

    /**
     * Get a suggestion for modification of a reference.
     * @param bibtexkey - the BibTeX key of the reference.
     * @param id - the identifier of the suggestion.
     * @return BibTeXEntry - the suggestion.
     */
    public static BibTeXEntry getSuggestion(String bibtexkey, int id) {
        BibTeXEntry result = null;

        if (bibtexkey != null && id > 0) {
            getLock().writeLock().lock();
            Git git = getGit();
            try {
                // check if suggestion exists
                if (git != null && existsSuggestionPrivate(bibtexkey, id)) {
                    // switch branch to get access to suggestion
                    String branch = bibtexkey + "/" + id;
                    git.checkout().setName(branch).call();

                    // get suggestion
                    result = BibService.loadBib(bibtexkey, false);
                }
            } catch (GitAPIException e) {
                e.printStackTrace();
            } finally {
                getLock().writeLock().unlock();
                if (git != null) {
                    try {
                        // switch to master branch
                        git.checkout().setName("master").call();
                    } catch (GitAPIException e) {
                        e.printStackTrace();
                    }
                    git.close();
                }
            }
        }
        return result;
    }
}
