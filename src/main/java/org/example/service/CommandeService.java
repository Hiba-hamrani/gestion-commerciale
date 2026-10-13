package org.example.service;

import org.example.entities.Commande;
import org.example.entities.Produit;
import org.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.Map;

/**
 * Service des commandes.
 *
 * <p>{@link #passerCommande} fait tout dans <b>une seule transaction</b> : relecture des
 * produits, création de la commande et de ses lignes. Si un produit n'existe pas ou
 * si une quantité est invalide, rien n'est enregistré (rollback).
 */
public class CommandeService extends CrudService<Commande, Long> {

    public CommandeService() {
        super(Commande.class);
    }

    /**
     * Crée une commande.
     *
     * @param date              date de la commande
     * @param quantiteParProduit id du produit → quantité commandée
     * @return la commande enregistrée (avec son id)
     * @throws IllegalArgumentException si la commande est vide, si une quantité est &lt;= 0
     *                                  ou si un produit n'existe pas
     */
    public Commande passerCommande(LocalDate date, Map<Long, Integer> quantiteParProduit) {
        if (date == null) throw new IllegalArgumentException("La date est obligatoire");
        if (quantiteParProduit == null || quantiteParProduit.isEmpty()) {
            throw new IllegalArgumentException("Une commande doit contenir au moins un produit");
        }

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Commande commande = new Commande(date);

            for (Map.Entry<Long, Integer> e : quantiteParProduit.entrySet()) {
                if (e.getValue() == null || e.getValue() <= 0) {
                    throw new IllegalArgumentException("Quantité invalide pour le produit " + e.getKey());
                }
                // Produit relu dans CE contexte de persistance : il est "managed"
                Produit p = em.find(Produit.class, e.getKey());
                if (p == null) {
                    throw new IllegalArgumentException("Produit introuvable : " + e.getKey());
                }
                commande.addProduit(p, e.getValue());
            }

            em.persist(commande); // les lignes suivent (cascade ALL)
            tx.commit();
            return commande;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    /** @return le nombre de lignes de la commande (lu en base) */
    public long nombreDeLignes(Long commandeId) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(l) FROM CommandeProduit l WHERE l.commande.id = :id", Long.class)
                    .setParameter("id", commandeId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}