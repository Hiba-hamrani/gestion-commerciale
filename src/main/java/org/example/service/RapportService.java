package org.example.service;

import org.example.entities.Categorie;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;
import org.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Exercice de synthèse : rapport de ventes mensuel par catégorie (Criteria API).
 *
 * <p>Pour chaque catégorie : somme de {@code prix × quantité} sur les lignes des
 * commandes passées pendant le mois demandé. Équivalent SQL :
 * <pre>
 * SELECT c.nom, SUM(p.prix * l.quantite)
 * FROM commande_produit l
 *   JOIN produits p   ON l.produit_id = p.id
 *   JOIN categories c ON p.categorie_id = c.id
 *   JOIN commandes cmd ON l.commande_id = cmd.id
 * WHERE cmd.date_commande BETWEEN :debut AND :fin
 * GROUP BY c.nom ORDER BY c.nom
 * </pre>
 */
public class RapportService {

    /** Rapport du mois précédent, comme demandé par l'énoncé. */
    public List<VenteParCategorie> rapportMoisPrecedent() {
        return rapportVentes(YearMonth.now().minusMonths(1));
    }

    /**
     * @param mois mois à analyser (ex. {@code YearMonth.of(2026, 9)})
     * @return le chiffre d'affaires par catégorie, trié par nom ; vide si aucune vente
     */
    public List<VenteParCategorie> rapportVentes(YearMonth mois) {
        if (mois == null) throw new IllegalArgumentException("Le mois est obligatoire");
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

            Root<CommandeProduit> ligne = cq.from(CommandeProduit.class);
            Join<CommandeProduit, Produit> produit = ligne.join("produit");
            Join<Produit, Categorie> categorie = produit.join("categorie");
            Join<CommandeProduit, Commande> commande = ligne.join("commande");

            // prix × quantité, puis somme par catégorie
            Expression<BigDecimal> prix = produit.get("prix");
            Expression<Integer> quantite = ligne.get("quantite");
            Expression<Number> montant = cb.prod(prix, quantite);

            cq.multiselect(categorie.get("nom"), cb.sum(montant))
                    .where(cb.between(commande.<LocalDate>get("dateCommande"), debut, fin))
                    .groupBy(categorie.get("nom"))
                    .orderBy(cb.asc(categorie.get("nom")));

            List<VenteParCategorie> rapport = new ArrayList<>();
            for (Object[] ligneResultat : em.createQuery(cq).getResultList()) {
                String nom = (String) ligneResultat[0];
                // Le type exact de la somme dépend de la base : on le convertit en BigDecimal
                BigDecimal total = new BigDecimal(ligneResultat[1].toString()).setScale(2, RoundingMode.HALF_UP);
                rapport.add(new VenteParCategorie(nom, total));
            }
            return rapport;
        } finally {
            em.close();
        }
    }
}