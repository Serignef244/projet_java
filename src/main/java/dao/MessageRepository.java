package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.Message;
import model.MessageStatus;
import model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageRepository - composant principal de l'application.
 */
public class MessageRepository {
    private final EntityManagerFactory emf;

    public MessageRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Message save(Message message) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Message managed = em.merge(message);
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Message> findConversation(String usernameA, String usernameB) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("""
                    SELECT m FROM Message m
                    JOIN FETCH m.sender s
                    JOIN FETCH m.recipient r
                    WHERE (s.username = :a AND r.username = :b)
                       OR (s.username = :b AND r.username = :a)
                    ORDER BY m.timestamp ASC
                    """, Message.class)
                    .setParameter("a", usernameA)
                    .setParameter("b", usernameB)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Message> findPendingForRecipient(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("""
                    SELECT m FROM Message m
                    JOIN FETCH m.sender s
                    JOIN FETCH m.recipient r
                    WHERE r.username = :username AND m.status = :status
                    ORDER BY m.timestamp ASC
                    """, Message.class)
                    .setParameter("username", username)
                    .setParameter("status", MessageStatus.ENVOYE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void markReceived(List<Long> messageIds) {
        if (messageIds.isEmpty()) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("""
                    UPDATE Message m
                    SET m.status = :received
                    WHERE m.id IN :ids AND m.status = :sent
                    """)
                    .setParameter("received", MessageStatus.RECU)
                    .setParameter("sent", MessageStatus.ENVOYE)
                    .setParameter("ids", messageIds)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void markReadForConversation(String recipientUsername, String senderUsername) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("""
                    UPDATE Message m
                    SET m.status = :read
                    WHERE m.status <> :read
                      AND m.recipient IN (SELECT u FROM User u WHERE u.username = :recipient)
                      AND m.sender IN (SELECT u FROM User u WHERE u.username = :sender)
                    """)
                    .setParameter("read", MessageStatus.LU)
                    .setParameter("recipient", recipientUsername)
                    .setParameter("sender", senderUsername)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<User> findConversationPartners(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            List<User> sentTo = em.createQuery("""
                    SELECT DISTINCT r FROM Message m
                    JOIN m.sender s
                    JOIN m.recipient r
                    WHERE s.username = :username
                    """, User.class)
                    .setParameter("username", username)
                    .getResultList();

            List<User> receivedFrom = em.createQuery("""
                    SELECT DISTINCT s FROM Message m
                    JOIN m.sender s
                    JOIN m.recipient r
                    WHERE r.username = :username
                    """, User.class)
                    .setParameter("username", username)
                    .getResultList();

            List<User> all = new ArrayList<>(sentTo);
            all.addAll(receivedFrom);
            return all;
        } finally {
            em.close();
        }
    }
}
