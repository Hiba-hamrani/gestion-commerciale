package org.example.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clé primaire composée de la table {@code commande_produit} : (commande_id, produit_id).
 *
 * <p>Une clé composée doit être {@link Serializable} et redéfinir
 * {@link #equals(Object)} / {@link #hashCode()}, sinon Hibernate ne sait pas
 * comparer deux clés.
 */
@Embeddable
public class CommandeProduitPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "commande_id")
    private Long commandeId;

    @Column(name = "produit_id")
    private Long produitId;

    /** Constructeur vide exigé par JPA. */
    public CommandeProduitPK() {}

    public CommandeProduitPK(Long commandeId, Long produitId) {
        this.commandeId = commandeId;
        this.produitId = produitId;
    }

    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long commandeId) { this.commandeId = commandeId; }

    public Long getProduitId() { return produitId; }
    public void setProduitId(Long produitId) { this.produitId = produitId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandeProduitPK)) return false;
        CommandeProduitPK that = (CommandeProduitPK) o;
        return Objects.equals(commandeId, that.commandeId) && Objects.equals(produitId, that.produitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandeId, produitId);
    }
}