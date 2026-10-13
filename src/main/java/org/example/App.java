package org.example;

import org.example.entities.Categorie;
import org.example.entities.Client;
import org.example.entities.Commande;
import org.example.entities.Fournisseur;
import org.example.entities.Produit;
import org.example.entities.User;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportService;
import org.example.util.JpaUtil;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Démonstration complète sur MySQL : CRUD, requêtes nommées, Criteria,
 * pagination, héritage JOINED, commande N:N et rapport de ventes.
 */
public class App {

    public static void main(String[] args) {
        try {
            viderBase(); // permet de relancer l'App sans erreur de nom de catégorie en double

            CrudService<Categorie, Long> catService = new CrudService<>(Categorie.class);
            ProduitService produitService = new ProduitService();
            CommandeService commandeService = new CommandeService();
            CrudService<User, Long> userService = new CrudService<>(User.class);

            // ---------- Catégories ----------
            titre("Catégories");
            Categorie electronique = new Categorie("Électronique");
            Categorie informatique = new Categorie("Informatique");
            catService.create(electronique);
            catService.create(informatique);
            System.out.println("Catégorie créée avec l'ID : " + electronique.getId());
            System.out.println("Nouvelle catégorie ID = " + informatique.getId());

            // ---------- Produits ----------
            titre("Produits");
            Produit clavier = new Produit("Clavier", new BigDecimal("49.99"), informatique);
            Produit souris = new Produit("Souris", new BigDecimal("19.90"), informatique);
            Produit ecran = new Produit("Écran 27 pouces", new BigDecimal("229.00"), informatique);
            Produit casque = new Produit("Casque audio", new BigDecimal("89.50"), electronique);
            for (Produit p : new Produit[]{clavier, souris, ecran, casque}) {
                produitService.create(p);
                System.out.println("- " + p);
            }

            // ---------- Transaction refusée ----------
            titre("Validation : produit à prix négatif");
            try {
                produitService.create(new Produit("Erreur", new BigDecimal("-5"), informatique));
            } catch (IllegalArgumentException e) {
                System.out.println("Refusé : " + e.getMessage());
            }

            // ---------- Requêtes ----------
            titre("Named Query : produits de la catégorie Informatique");
            produitService.findByCategorie(informatique.getId()).forEach(p -> System.out.println("- " + p));

            titre("Named Query : libellé contenant 'cla'");
            produitService.searchByLibelle("cla").forEach(p -> System.out.println("- " + p));

            titre("Criteria : prix entre 20 et 100");
            produitService.search(null, new BigDecimal("20"), new BigDecimal("100"))
                    .forEach(p -> System.out.println("- " + p));

            titre("Pagination : page 2 (2 produits par page)");
            produitService.findPage(2, 2).forEach(p -> System.out.println("- " + p));

            titre("JOIN FETCH : produit avec sa catégorie");
            Produit relu = produitService.getByIdAvecCategorie(clavier.getId());
            System.out.println(relu.getLibelle() + " -> " + relu.getCategorie().getNom());

            // ---------- Héritage JOINED ----------
            titre("Héritage JOINED : utilisateurs");
            userService.create(new Client("Hiba", LocalDate.now()));
            userService.create(new Fournisseur("TechDistrib", "Marrakech"));
            userService.getAll().forEach(u -> System.out.println("- " + u));

            // ---------- Commandes N:N ----------
            titre("Commandes");
            Map<Long, Integer> lignesMoisDernier = new LinkedHashMap<>();
            lignesMoisDernier.put(clavier.getId(), 3);
            lignesMoisDernier.put(casque.getId(), 2);
            Commande c1 = commandeService.passerCommande(LocalDate.now().minusMonths(1), lignesMoisDernier);
            System.out.println("Commande créée avec ID = " + c1.getId()
                    + " (" + commandeService.nombreDeLignes(c1.getId()) + " lignes, mois dernier)");

            Map<Long, Integer> lignesCeMois = new LinkedHashMap<>();
            lignesCeMois.put(souris.getId(), 5);
            Commande c2 = commandeService.passerCommande(LocalDate.now(), lignesCeMois);
            System.out.println("Commande créée avec ID = " + c2.getId() + " (ce mois-ci, hors rapport)");

            // ---------- Rapport ----------
            titre("Rapport de ventes du mois précédent");
            new RapportService().rapportMoisPrecedent().forEach(v -> System.out.println("- " + v));

        } finally {
            JpaUtil.shutdown();
        }
    }

    /** Vide les tables dans l'ordre des clés étrangères. */
    private static void viderBase() {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM CommandeProduit").executeUpdate();
            em.createQuery("DELETE FROM Commande").executeUpdate();
            em.createQuery("DELETE FROM Produit").executeUpdate();
            em.createQuery("DELETE FROM Categorie").executeUpdate();
            em.createQuery("DELETE FROM Client").executeUpdate();
            em.createQuery("DELETE FROM Fournisseur").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private static void titre(String t) {
        System.out.println("\n===== " + t + " =====");
    }
}