package eu.cloudref.dal;

import eu.cloudref.db.Comment;
import eu.cloudref.db.CommentKey;
import org.hibernate.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * Service for adding, deleting, and updating comments of a PDF file of a reference.
 */
public class CommentService {

    /**
     * Return all public comments of a reference and the private comments of the user for this reference.
     *
     * @param bibtexkey the identifier of the reference
     * @param username the current user
     * @return List<Comment> with all comments the current user is allowed to see, ordered by the page number.
     */
    @SuppressWarnings("unchecked")
    public static List<Comment> getComments(String bibtexkey, String username) {
        Session session = null;
        Transaction tx = null;
        try {
            session = DBService.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // get all comments of bibtexkey without private comments of other users
            Criterion bibtexComments = Restrictions.like("bibtexkey", bibtexkey);
            Criterion publicComments = Restrictions.like("publish", true);
            Criterion userComments = Restrictions.like("author", username);

            Criteria criteria = session.createCriteria(Comment.class);
            criteria.add(Restrictions.and(Restrictions.or(publicComments, userComments), bibtexComments));
            criteria.addOrder(Order.asc("pageNumber"));
            criteria.addOrder(Order.asc("date"));

            Object result = criteria.list();
            List<Comment> comments = null;
            if (result != null) {
                comments = (List<Comment>) result;
            }
            tx.commit();
            return comments;
        } catch (Exception var11) {
            var11.printStackTrace();
            tx.rollback();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return null;
    }

    /**
     * Update a comment of a reference.
     *
     * @param updatedComment the updated version of the comment.
     * @return Comment - the updated version of the comment, or null if an error occurred.
     */
    public static Comment updateComment(Comment updatedComment) {
        Session session = null;
        Transaction tx = null;

        try {
            session = DBService.getSessionFactory().openSession();

            tx = session.beginTransaction();
            session.saveOrUpdate(updatedComment);
            session.flush();
            tx.commit();
            return updatedComment;
        } catch (Exception var11) {
            var11.printStackTrace();
            tx.rollback();
        } finally {
            if (session != null) {
                session.close();
            }

        }
        return null;
    }

    /**
     * Save a comment of a reference.
     *
     * @param newComment the comment which should be saved.
     * @return Comment - the comment which was saved, or null if an error occurred.
     */
    public static Comment addComment(Comment newComment) {
        Session session = null;
        Transaction tx = null;

        try {
            session = DBService.getSessionFactory().openSession();

            // get highest id where same bibtexkey is used
            Criteria criteria = session.createCriteria(Comment.class);
            criteria.setProjection(Projections.max("id"));
            criteria.add(Restrictions.like("bibtexkey", newComment.getBibtexkey()));

            Object result = criteria.uniqueResult();
            int maxId;
            if (result != null) {
                maxId = (int) result;
            } else {
                maxId = 0;
            }
            // set id of comment
            newComment.setId(maxId + 1);

            tx = session.beginTransaction();
            session.save(newComment);
            session.flush();
            tx.commit();
            return newComment;
        } catch (Exception var11) {
            var11.printStackTrace();
            tx.rollback();
        } finally {
            if (session != null) {
                session.close();
            }

        }
        return null;
    }

    /**
     * Get a comment of a reference by its identifier.
     *
     * @param bibtexkey the BibTeX-key of the reference.
     * @param id the identifier of the comment.
     * @return Comment - the comment, or null if it does not exist or an error occurred.
     */
    public static Comment getComment(String bibtexkey, int id) {
        if (bibtexkey != null && id > 0) {
            Session session = null;
            Comment comment = null;
            Transaction tx = null;
            try {
                session = DBService.getSessionFactory().openSession();
                tx = session.beginTransaction();
                comment = (Comment) session.load(Comment.class, new CommentKey(bibtexkey, id));
                Hibernate.initialize(comment);
                tx.commit();
            } catch (ObjectNotFoundException nf) {
                nf.printStackTrace();
                if (tx != null) {
                    tx.rollback();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                if (tx != null) {
                    tx.rollback();
                }
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
            return comment;
        }
        return null;
    }

    /**
     * Delete a comment of a reference by its identifier.
     *
     * @param bibtexkey the BibTeX-key of the reference.
     * @param id the identifier of the comment.
     * @return true if successfully deleted the comment, false otherwise.
     */
    public static boolean deleteComment(String bibtexkey, int id) {
        if (bibtexkey != null && id > 0) {
            Session session = null;
            Transaction tx = null;
            try {
                session = DBService.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Comment comment = (Comment) session.load(Comment.class, new CommentKey(bibtexkey, id));
                Hibernate.initialize(comment);
                session.delete(comment);
                session.flush();
                tx.commit();

                return true;
            } catch (ObjectNotFoundException nf) {
                nf.printStackTrace();
                if (tx != null) {
                    tx.rollback();
                }
                return false;
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
        return false;
    }
}
