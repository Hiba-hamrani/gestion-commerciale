package org.example.service;

import java.math.BigDecimal;

/** Une ligne du rapport de ventes : une catégorie et son chiffre d'affaires. */
public class VenteParCategorie {

    private final String categorie;
    private final BigDecimal chiffreAffaires;

    public VenteParCategorie(String categorie, BigDecimal chiffreAffaires) {
        this.categorie = categorie;
        this.chiffreAffaires = chiffreAffaires;
    }

    public String getCategorie() { return categorie; }
    public BigDecimal getChiffreAffaires() { return chiffreAffaires; }

    @Override
    public String toString() {
        return categorie + " : " + chiffreAffaires;
    }
}