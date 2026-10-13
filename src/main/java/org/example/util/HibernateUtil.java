package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Accès <b>Hibernate natif</b> ({@link SessionFactory}) configuré par {@code hibernate.cfg.xml}.
 *
 * <p>Fourni pour l'énoncé. L'application elle-même passe par JPA ({@link JpaUtil}),
 * car le {@code GenericDAO} demandé utilise {@code EntityManager}. La {@code SessionFactory}
 * n'est construite qu'au premier appel, pour ne pas ouvrir une seconde connexion inutile.
 */
public final class HibernateUtil {

    private static SessionFactory sessionFactory;

    private HibernateUtil() {}

    /** @return la SessionFactory, créée au premier appel à partir de hibernate.cfg.xml */
    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                sessionFactory = new Configuration().configure().buildSessionFactory();
            } catch (Throwable ex) {
                System.err.println("Initial SessionFactory creation failed. " + ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
        return sessionFactory;
    }

    /** Ferme la SessionFactory si elle a été créée. */
    public static synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}