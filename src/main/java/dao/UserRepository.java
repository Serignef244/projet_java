package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.Status;
import model.User;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - composant principal de l'application.
 */
public class UserRepository {
    private final EntityManagerFactory emf;

    public UserRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Optional<User> findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .getResultList();
            return users.stream().findFirst();
        } finally {
            em.close();
        }
    }

    public User save(User user) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            User managed = em.merge(user);
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

    public void updateStatus(String username, Status status) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE User u SET u.status = :status WHERE u.username = :username")
                    .setParameter("status", status)
                    .setParameter("username", username)
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

    public void markAllOffline() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE User u SET u.status = :status")
                    .setParameter("status", Status.OFFLINE)
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

    public List<User> findAllOrderByUsername() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<User> findOnlineOrderByUsername() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.status = :status ORDER BY u.username", User.class)
                    .setParameter("status", Status.ONLINE)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}

