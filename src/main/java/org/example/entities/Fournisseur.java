package org.example.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/** Fournisseur — sous-classe de {@link User} (table {@code fournisseurs}). */
@Entity
@Table(name = "fournisseurs")
public class Fournisseur extends User {

    /** Adresse du fournisseur. */
    @Column(name = "adresse")
    private String adresse;

    /** Constructeur vide exigé par JPA. */
    public Fournisseur() {}

    public Fournisseur(String nom, String adresse) {
        super(nom);
        this.adresse = adresse;
    }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    @Override
    public String toString() {
        return "Fournisseur{id=" + getId() + ", nom='" + getNom() + "', adresse='" + adresse + "'}";
    }
}