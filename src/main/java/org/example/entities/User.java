package org.example.entities;

import javax.persistence.*;

/**
 * Utilisateur (classe mère abstraite) — héritage <b>JOINED</b>.
 *
 * <p>La table {@code users} contient les colonnes communes ; {@code clients} et
 * {@code fournisseurs} contiennent leurs colonnes propres, avec un {@code id}
 * qui est aussi une clé étrangère vers {@code users.id}.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    /** Clé primaire, partagée avec la table de la sous-classe. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom obligatoire. */
    @Column(nullable = false, length = 100)
    private String nom;

    /** Constructeur vide exigé par JPA. */
    protected User() {}

    protected User(String nom) {
        this.nom = nom;
    }

    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}