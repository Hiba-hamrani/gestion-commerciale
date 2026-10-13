package org.example.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Map;

/**
 * Fournit une <b>seule</b> {@link EntityManagerFactory} pour toute l'application (singleton).
 *
 * <p>Elle est créée au premier appel à partir de l'unité {@code defaultPU} (MySQL).
 * Les tests appellent {@link #init(Map)} pour la remplacer par une base H2 en mémoire.
 */
public final class JpaUtil {

    private static final String UNITE = "defaultPU";
    private static EntityManagerFactory emf;

    private JpaUtil() {}

    /** @return l'usine partagée, créée à la demande */
    public static synchronized EntityManagerFactory getEmf() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(UNITE);
        }
        return emf;
    }

    /**
     * Recrée l'usine en remplaçant certaines propriétés de {@code persistence.xml}
     * (par exemple l'URL de la base, pour les tests).
     *
     * @param proprietes propriétés qui remplacent celles du fichier
     */
    public static synchronized void init(Map<String, String> proprietes) {
        shutdown();
        emf = Persistence.createEntityManagerFactory(UNITE, proprietes);
    }

    /** Ferme l'usine et libère les connexions. À appeler à l'arrêt de l'application. */
    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}