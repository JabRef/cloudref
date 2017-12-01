package eu.cloudref.auth;

import eu.cloudref.dal.DBService;
import eu.cloudref.db.User;
import eu.cloudref.models.Role;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.GenericJDBCException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class AuthService {

    static User getUser(String username) {
        Session session = null;
        Transaction tx = null;
        try {
            session = DBService.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // get user by username
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.like("name", username));

            Object result = criteria.uniqueResult();
            tx.commit();
            // return user
            if (result instanceof User) {
                return (User) result;
            } else {
                return null;
            }
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
        return null;
    }

    public static User addUser(User newUser) {
        Session session = null;
        Transaction tx = null;
        try {
            // add role if empty
            if (newUser.getUserRole() == null) {
                newUser.setUserRole(Role.USER);
            }

            // encode password
            byte[] salt = getSalt();
            newUser.setPassword(encodePassword(newUser.getPassword(), salt));
            newUser.setSalt(salt);

            session = DBService.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Object identifier = session.save(newUser);
            if (identifier != null) {
                session.flush();
                tx.commit();
            } else {
                return null;
            }
            return newUser;
        } catch (GenericJDBCException ge) {
            if (tx != null) {
                tx.rollback();
            }
            if (ge.getCause() != null && ge.getCause() instanceof SQLException) {
                SQLException se = (SQLException) ge.getCause();
                if (se.getMessage().contains("[SQLITE_CONSTRAINT]")) {
                    // username already exists
                    return null;
                } else {
                    se.printStackTrace();
                }
            } else {
                ge.printStackTrace();
            }
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
        return null;
    }

    private static final int ITERATIONS = 1000;
    private static final int KEY_LENGTH = 192; // bits
    private static final int SALT_LENGTH = 20; // byte

    static String encodePassword(String password, byte[] salt) {
        String result = password;

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); //hash length of 160 bits -> 20 bytes
            byte[] hashedPassword = key.generateSecret(spec).getEncoded();
            result = String.format("%x", new BigInteger(hashedPassword));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[SALT_LENGTH];
        sr.nextBytes(salt);
        return salt;
    }
}
