package org.example.entities;

import javax.persistence.*;

/**
 * Ligne de commande : un produit, dans une commande, avec une quantité.
 *
 * <p>Entité associative de la relation N:N Commande ↔ Produit.
 * Sa clé ({@link CommandeProduitPK}) est remplie par {@code @MapsId} à partir
 * des deux relations : il ne faut pas lire {@code commande.getId()} dans le
 * constructeur, car il vaut {@code null} tant que la commande n'est pas enregistrée.
 */
@Entity
@Table(name = "commande_produit")
public class CommandeProduit {

    /** Clé composée, initialisée vide : Hibernate la remplit au persist. */
    @EmbeddedId
    private CommandeProduitPK id = new CommandeProduitPK();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commandeId")
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("produitId")
    @JoinColumn(name = "produit_id")
    private Produit produit;

    /** Quantité commandée, obligatoire. */
    @Column(nullable = false)
    private int quantite;

    /** Constructeur vide exigé par JPA. */
    public CommandeProduit() {}

    public CommandeProduit(Commande commande, Produit produit, int quantite) {
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
    }

    public CommandeProduitPK getId() { return id; }

    public Commande getCommande() { return commande; }
    public Produit getProduit() { return produit; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
}