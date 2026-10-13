package org.example.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

/** Client — sous-classe de {@link User} (table {@code clients}). */
@Entity
@Table(name = "clients")
public class Client extends User {

    /** Date d'inscription du client. */
    @Column(name = "date_inscription")
    private LocalDate dateInscription;

    /** Constructeur vide exigé par JPA. */
    public Client() {}

    public Client(String nom, LocalDate dateInscription) {
        super(nom);
        this.dateInscription = dateInscription;
    }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    @Override
    public String toString() {
        return "Client{id=" + getId() + ", nom='" + getNom() + "', inscription=" + dateInscription + "}";
    }
}