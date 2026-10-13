package org.example.dao;

import org.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.List;

/**
 * DAO générique JPA : CRUD pour n'importe quelle entité.
 *
 * <p>Chaque méthode ouvre son propre {@link EntityManager} (une unité de travail)
 * et le ferme <b>toujours</b> dans {@code finally}. Les écritures se font dans une
 * transaction, annulée ({@code rollback}) en cas d'erreur.
 *
 * <p>Conséquence : les entités renvoyées sont <b>détachées</b>. Leurs relations
 * {@code LAZY} ne sont plus chargeables (voir {@code ProduitService#getByIdAvecCategorie}).
 *
 * @param <T>  type de l'entité
 * @param <ID> type de sa clé primaire
 */
public class GenericDAO<T, ID extends Serializable> {

    private final Class<T> entityClass;

    /** @param entityClass classe de l'entité gérée */
    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /** Ouvre un EntityManager sur l'usine partagée. */
    protected EntityManager ouvrir() {
        return JpaUtil.getEmf().createEntityManager();
    }

    /** @return l'entité de cette clé, ou {@code null} */
    public T find(ID id) {
        EntityManager em = ouvrir();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    /** @return toutes les entités de ce type (y compris les sous-classes) */
    public List<T> findAll() {
        EntityManager em = ouvrir();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, entityClass).getResultList();
        } finally {
            em.close();
        }
    }

    /** Enregistre une nouvelle entité ; son id est rempli après l'appel. */
    public void create(T entity) {
        EntityManager em = ouvrir();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** @return la copie fusionnée (c'est elle qui reflète l'état en base) */
    public T update(T entity) {
        EntityManager em = ouvrir();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Supprime l'entité ; si elle est détachée, elle est d'abord rattachée par merge. */
    public void delete(T entity) {
        EntityManager em = ouvrir();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T attached = em.contains(entity) ? entity : em.merge(entity);
            em.remove(attached);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}