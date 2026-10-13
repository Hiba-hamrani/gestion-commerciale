package org.example.entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Commande contenant plusieurs lignes ({@link CommandeProduit}).
 *
 * <p>La relation N:N Commande ↔ Produit passe par l'entité associative
 * {@code CommandeProduit}, car elle porte une information en plus : la quantité.
 */
@Entity
@Table(name = "commandes")
public class Commande {

    /** Clé primaire auto-incrémentée. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date de la commande. */
    @Column(name = "date_commande")
    private LocalDate dateCommande;

    /** Lignes de la commande : elles vivent et meurent avec elle. */
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeProduit> lignes = new ArrayList<>();

    /** Constructeur vide exigé par JPA. */
    public Commande() {}

    /** @param dateCommande date de la commande */
    public Commande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    /**
     * Ajoute une ligne (produit + quantité). La clé composée est remplie par
     * Hibernate au moment du persist, grâce à {@code @MapsId}.
     */
    public void addProduit(Produit p, int quantite) {
        lignes.add(new CommandeProduit(this, p, quantite));
    }

    public Long getId() { return id; }

    public LocalDate getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande = dateCommande; }

    public List<CommandeProduit> getLignes() { return lignes; }

    @Override
    public String toString() {
        return "Commande{id=" + id + ", date=" + dateCommande + ", lignes=" + lignes.size() + "}";
    }
}