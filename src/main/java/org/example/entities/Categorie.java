package org.example.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Catégorie de produits (ex. « Informatique »).
 *
 * <p>Parent de ses produits : {@code cascade = ALL} et {@code orphanRemoval = true},
 * donc un produit retiré de la liste est supprimé de la base.
 */
@Entity
@Table(name = "categories")
public class Categorie {

    /** Clé primaire auto-incrémentée. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom obligatoire et unique. */
    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    /** Produits de la catégorie (côté inverse de {@link Produit#getCategorie()}). */
    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produit> produits = new ArrayList<>();

    /** Constructeur vide exigé par JPA. */
    public Categorie() {}

    /** @param nom nom unique de la catégorie */
    public Categorie(String nom) {
        this.nom = nom;
    }

    /** Ajoute un produit en mettant à jour les deux côtés de la relation. */
    public void addProduit(Produit p) {
        produits.add(p);
        p.setCategorie(this);
    }

    /** Retire un produit des deux côtés ; il sera supprimé au commit (orphanRemoval). */
    public void removeProduit(Produit p) {
        produits.remove(p);
        p.setCategorie(null);
    }

    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public List<Produit> getProduits() { return produits; }

    @Override
    public String toString() {
        return "Categorie{id=" + id + ", nom='" + nom + "'}";
    }
}