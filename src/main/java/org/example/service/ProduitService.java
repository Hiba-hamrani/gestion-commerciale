package org.example.service;

import org.example.entities.Produit;
import org.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service des produits : CRUD + validation du prix + requêtes avancées
 * (Named Queries, Criteria API, pagination, JOIN FETCH).
 */
public class ProduitService extends CrudService<Produit, Long> {

    public ProduitService() {
        super(Produit.class);
    }

    /** Règles métier : libellé obligatoire, prix positif ou nul, catégorie obligatoire. */
    @Override
    protected void valider(Produit p) {
        if (p.getLibelle() == null || p.getLibelle().isBlank()) {
            throw new IllegalArgumentException("Le libellé est obligatoire");
        }
        if (p.getPrix() == null || p.getPrix().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix doit être positif ou nul");
        }
        if (p.getCategorie() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire");
        }
    }

    /**
     * Produit avec sa catégorie déjà chargée ({@code JOIN FETCH}), utilisable
     * après la fermeture de l'EntityManager sans {@code LazyInitializationException}.
     *
     * @return le produit, ou {@code null} s'il n'existe pas
     */
    public Produit getByIdAvecCategorie(Long id) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Produit p JOIN FETCH p.categorie WHERE p.id = :id", Produit.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /** Named Query {@code Produit.findByCategorie}. */
    public List<Produit> findByCategorie(Long catId) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            return em.createNamedQuery("Produit.findByCategorie", Produit.class)
                    .setParameter("catId", catId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Named Query {@code Produit.searchByLibelle} (insensible à la casse). */
    public List<Produit> searchByLibelle(String term) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            return em.createNamedQuery("Produit.searchByLibelle", Produit.class)
                    .setParameter("term", term)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Recherche dynamique (Criteria API). Un critère {@code null} est ignoré.
     *
     * @param term    partie du libellé, ou {@code null}
     * @param minPrix prix minimum inclus, ou {@code null}
     * @param maxPrix prix maximum inclus, ou {@code null}
     */
    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Produit> cq = cb.createQuery(Produit.class);
            Root<Produit> root = cq.from(Produit.class);

            List<Predicate> predicates = new ArrayList<>();
            if (term != null && !term.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("libelle")), "%" + term.toLowerCase() + "%"));
            }
            if (minPrix != null) {
                predicates.add(cb.ge(root.get("prix"), minPrix));
            }
            if (maxPrix != null) {
                predicates.add(cb.le(root.get("prix"), maxPrix));
            }

            cq.select(root)
                    .where(predicates.toArray(new Predicate[0]))
                    .orderBy(cb.asc(root.get("prix")));
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Une page de produits, triés par id.
     *
     * @param pageNumber numéro de page (commence à 1)
     * @param pageSize   nombre de produits par page
     */
    public List<Produit> findPage(int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1) {
            throw new IllegalArgumentException("La page et la taille doivent être au moins 1");
        }
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produit p ORDER BY p.id", Produit.class)
                    .setFirstResult((pageNumber - 1) * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}