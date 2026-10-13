package org.example.service;

import org.example.dao.GenericDAO;

import java.io.Serializable;
import java.util.List;

/**
 * Service CRUD générique : couche métier au-dessus du {@link GenericDAO}.
 * Il centralise les validations, pour ne pas les répéter dans chaque servlet.
 *
 * @param <T>  type de l'entité
 * @param <ID> type de sa clé primaire
 */
public class CrudService<T, ID extends Serializable> {

    protected final GenericDAO<T, ID> dao;

    /** @param entityClass classe de l'entité gérée */
    public CrudService(Class<T> entityClass) {
        this.dao = new GenericDAO<>(entityClass);
    }

    public T getById(ID id) {
        if (id == null) throw new IllegalArgumentException("L'identifiant ne doit pas être nul");
        return dao.find(id);
    }

    public List<T> getAll() {
        return dao.findAll();
    }

    public void create(T entity) {
        verifierNonNul(entity);
        valider(entity);
        dao.create(entity);
    }

    public T update(T entity) {
        verifierNonNul(entity);
        valider(entity);
        return dao.update(entity);
    }

    public void delete(T entity) {
        verifierNonNul(entity);
        dao.delete(entity);
    }

    /**
     * Règles métier propres à l'entité, appelées avant create et update.
     * Ne fait rien par défaut ; les services spécialisés la redéfinissent.
     *
     * @throws IllegalArgumentException si l'entité n'est pas valide
     */
    protected void valider(T entity) {
        // aucune règle par défaut
    }

    private void verifierNonNul(T entity) {
        if (entity == null) throw new IllegalArgumentException("L'entité ne doit pas être nulle");
    }
}