package org.example.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Produit vendu, rattaché à une {@link Categorie}.
 *
 * <p>La catégorie est chargée en {@code LAZY} : après la fermeture de l'EntityManager,
 * {@code getCategorie().getNom()} lève une {@code LazyInitializationException}.
 * Pour en avoir besoin, utiliser une requête avec {@code JOIN FETCH}.
 */
@Entity
@Table(name = "produits")
@NamedQueries({
        @NamedQuery(name = "Produit.findAll",
                query = "SELECT p FROM Produit p ORDER BY p.id"),
        @NamedQuery(name = "Produit.findByCategorie",
                query = "SELECT p FROM Produit p WHERE p.categorie.id = :catId ORDER BY p.libelle"),
        @NamedQuery(name = "Produit.searchByLibelle",
                query = "SELECT p FROM Produit p WHERE LOWER(p.libelle) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY p.libelle")
})
public class Produit {

    /** Clé primaire auto-incrémentée. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Libellé obligatoire. */
    @Column(nullable = false, length = 150)
    private String libelle;

    /** Prix en BigDecimal (pas de double : pas d'erreurs d'arrondi sur de l'argent). */
    @Column(precision = 10, scale = 2)
    private BigDecimal prix;

    /** Date de création, remplie automatiquement par le constructeur. */
    @Column(name = "date_creation")
    private LocalDate dateCreation;

    /** Catégorie obligatoire (côté propriétaire, clé étrangère categorie_id). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    /** Constructeur vide exigé par JPA. */
    public Produit() {}

    /**
     * @param libelle   libellé du produit
     * @param prix      prix unitaire
     * @param categorie catégorie du produit
     */
    public Produit(String libelle, BigDecimal prix, Categorie categorie) {
        this.libelle = libelle;
        this.prix = prix;
        this.categorie = categorie;
        this.dateCreation = LocalDate.now();
    }

    public Long getId() { return id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    @Override
    public String toString() {
        // Pas de categorie.getNom() ici : elle peut ne pas être chargée (LAZY)
        return "Produit{id=" + id + ", libelle='" + libelle + "', prix=" + prix + "}";
    }
}