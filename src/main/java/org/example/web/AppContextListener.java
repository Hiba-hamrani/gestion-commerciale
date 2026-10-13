package org.example.web;

import org.example.util.HibernateUtil;
import org.example.util.JpaUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Ferme les usines JPA / Hibernate quand le serveur arrête l'application,
 * pour libérer les connexions (« ne jamais oublier shutdown() »).
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.shutdown();
        HibernateUtil.shutdown();
    }
}