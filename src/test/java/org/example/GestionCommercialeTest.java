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
import org.example.service.VenteParCategorie;
import org.example.util.JpaUtil;
import org.hibernate.LazyInitializationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests de la gestion commerciale sur une base H2 en mémoire (MySQL n'est pas nécessaire).
 *
 * <p>Une seule EntityManagerFactory pour toute la classe (@BeforeClass),
 * et les tables sont vidées après chaque test (@After).
 */
public class GestionCommercialeTest {

    private CrudService<Categorie, Long> catService;
    private ProduitService produitService;
    private CommandeService commandeService;
    private CrudService<User, Long> userService;

    @BeforeClass
    public static void initBase() {
        Map<String, String> h2 = new HashMap<>();
        h2.put("javax.persistence.jdbc.driver", "org.h2.Driver");
        h2.put("javax.persistence.jdbc.url", "jdbc:h2:mem:gc;DB_CLOSE_DELAY=-1;MODE=MySQL");
        h2.put("javax.persistence.jdbc.user", "sa");
        h2.put("javax.persistence.jdbc.password", "");
        h2.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        h2.put("hibernate.hbm2ddl.auto", "create-drop");
        h2.put("hibernate.show_sql", "false");
        JpaUtil.init(h2);
    }

    @AfterClass
    public static void fermerBase() {
        JpaUtil.shutdown();
    }

    @Before
    public void setUp() {
        catService = new CrudService<>(Categorie.class);
        produitService = new ProduitService();
        commandeService = new CommandeService();
        userService = new CrudService<>(User.class);
    }

    @After
    public void viderTables() {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            for (String table : new String[]{"commande_produit", "commandes", "produits",
                    "categories", "clients", "fournisseurs", "users"}) {
                em.createNativeQuery("DELETE FROM " + table).executeUpdate();
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // ------------------------------------------------------------ outils

    private Categorie categorie(String nom) {
        Categorie c = new Categorie(nom);
        catService.create(c);
        return c;
    }

    private Produit produit(String libelle, String prix, Categorie c) {
        Produit p = new Produit(libelle, new BigDecimal(prix), c);
        produitService.create(p);
        return p;
    }

    private long compter(String entite) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(e) FROM " + entite + " e", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    // ------------------------------------------------------------ CRUD catégorie

    @Test
    public void creerCategorie_genereUnId() {
        Categorie c = categorie("Électronique");
        assertNotNull(c.getId());
        assertEquals("Électronique", catService.getById(c.getId()).getNom());
    }

    @Test
    public void nomDeCategorieEnDouble_rollbackEtRienEnregistre() {
        categorie("Informatique");
        assertThrows(PersistenceException.class, () -> categorie("Informatique"));
        assertEquals(1, compter("Categorie"));
    }

    @Test
    public void creerEntiteNulle_refuse() {
        assertThrows(IllegalArgumentException.class, () -> catService.create(null));
        assertThrows(IllegalArgumentException.class, () -> catService.getById(null));
    }

    @Test
    public void modifierCategorie_parMerge() {
        Categorie c = categorie("Ancien nom");
        c.setNom("Nouveau nom");
        catService.update(c);
        assertEquals("Nouveau nom", catService.getById(c.getId()).getNom());
    }

    @Test
    public void supprimerCategorie_supprimeAussiSesProduits() {
        Categorie c = categorie("Temporaire");
        produit("P1", "10", c);
        catService.delete(catService.getById(c.getId()));
        assertNull(catService.getById(c.getId()));
        assertEquals(0, compter("Produit"));
    }

    // ------------------------------------------------------------ Produits

    @Test
    public void produitAvecPrixNegatif_refuseEtRienEnregistre() {
        Categorie c = categorie("Informatique");
        assertThrows(IllegalArgumentException.class, () -> produit("Erreur", "-5", c));
        assertThrows(IllegalArgumentException.class,
                () -> produitService.create(new Produit("Sans catégorie", BigDecimal.ONE, null)));
        assertEquals(0, compter("Produit"));
    }

    @Test
    public void lazy_categorieInaccessibleApresFermeture_maisOkAvecJoinFetch() {
        Categorie c = categorie("Informatique");
        Produit p = produit("Clavier", "49.99", c);

        // Code du TP : la catégorie est LAZY et l'EntityManager est déjà fermé
        Produit simple = produitService.getById(p.getId());
        assertThrows(LazyInitializationException.class, () -> simple.getCategorie().getNom());

        // Correction : JOIN FETCH charge la catégorie dans la même requête
        Produit complet = produitService.getByIdAvecCategorie(p.getId());
        assertEquals("Informatique", complet.getCategorie().getNom());
        assertNull(produitService.getByIdAvecCategorie(999_999L));
    }

    @Test
    public void namedQueries_parCategorieEtParLibelle() {
        Categorie info = categorie("Informatique");
        Categorie elec = categorie("Électronique");
        produit("Souris", "19.90", info);
        produit("Clavier", "49.99", info);
        produit("Casque", "89.50", elec);

        List<Produit> deInfo = produitService.findByCategorie(info.getId());
        assertEquals(2, deInfo.size());
        assertEquals("Clavier", deInfo.get(0).getLibelle()); // trié par libellé

        // Recherche insensible à la casse
        assertEquals(1, produitService.searchByLibelle("CLA").size());
        assertEquals(2, produitService.searchByLibelle("s").size()); // Souris, Casque
    }

    @Test
    public void criteria_filtresCombines() {
        Categorie c = categorie("Informatique");
        produit("Souris", "19.90", c);
        produit("Clavier", "49.99", c);
        produit("Écran", "229.00", c);

        assertEquals(3, produitService.search(null, null, null).size());
        assertEquals(2, produitService.search(null, new BigDecimal("20"), null).size());
        assertEquals(2, produitService.search(null, null, new BigDecimal("100")).size());

        List<Produit> entre = produitService.search(null, new BigDecimal("20"), new BigDecimal("100"));
        assertEquals(1, entre.size());
        assertEquals("Clavier", entre.get(0).getLibelle());

        assertEquals(1, produitService.search("cran", null, null).size());
        assertTrue(produitService.search("souris", new BigDecimal("100"), null).isEmpty());
    }

    @Test
    public void pagination_pagesDansLOrdre() {
        Categorie c = categorie("Informatique");
        Produit[] p = new Produit[5];
        for (int i = 0; i < 5; i++) p[i] = produit("P" + i, "10", c);

        List<Produit> page1 = produitService.findPage(1, 2);
        List<Produit> page3 = produitService.findPage(3, 2);
        assertEquals(2, page1.size());
        assertEquals(p[0].getId(), page1.get(0).getId());
        assertEquals(1, page3.size());
        assertEquals(p[4].getId(), page3.get(0).getId());
        assertTrue(produitService.findPage(4, 2).isEmpty());

        assertThrows(IllegalArgumentException.class, () -> produitService.findPage(0, 2));
        assertThrows(IllegalArgumentException.class, () -> produitService.findPage(1, 0));
    }

    @Test
    public void orphanRemoval_retirerUnProduitLeSupprime() {
        Categorie c = categorie("Informatique");
        produit("Souris", "19.90", c);
        produit("Clavier", "49.99", c);

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            Categorie geree = em.find(Categorie.class, c.getId());
            geree.removeProduit(geree.getProduits().get(0));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        assertEquals(1, compter("Produit"));
    }

    // ------------------------------------------------------------ Héritage JOINED

    @Test
    public void heritageJoined_clientEtFournisseur() {
        userService.create(new Client("Hiba", LocalDate.of(2026, 9, 1)));
        userService.create(new Fournisseur("TechDistrib", "Marrakech"));

        List<User> users = userService.getAll();
        assertEquals(2, users.size());
        assertEquals(1, users.stream().filter(u -> u instanceof Client).count());
        assertEquals(1, users.stream().filter(u -> u instanceof Fournisseur).count());
        assertEquals(1, compter("Client"));
    }

    // ------------------------------------------------------------ Commandes

    @Test
    public void passerCommande_creeLesLignes() {
        Categorie c = categorie("Informatique");
        Produit souris = produit("Souris", "19.90", c);
        Produit clavier = produit("Clavier", "49.99", c);

        Map<Long, Integer> lignes = new LinkedHashMap<>();
        lignes.put(souris.getId(), 2);
        lignes.put(clavier.getId(), 1);
        Commande cmd = commandeService.passerCommande(LocalDate.now(), lignes);

        assertNotNull(cmd.getId());
        assertEquals(2, commandeService.nombreDeLignes(cmd.getId()));
    }

    @Test
    public void commandeAvecProduitInconnu_rollbackComplet() {
        Categorie c = categorie("Informatique");
        Produit souris = produit("Souris", "19.90", c);

        Map<Long, Integer> lignes = new LinkedHashMap<>();
        lignes.put(souris.getId(), 1);
        lignes.put(999_999L, 1);

        assertThrows(IllegalArgumentException.class, () -> commandeService.passerCommande(LocalDate.now(), lignes));
        assertEquals(0, compter("Commande"));
        assertEquals(0, compter("CommandeProduit"));
    }

    @Test
    public void commandeInvalide_refusee() {
        Categorie c = categorie("Informatique");
        Produit souris = produit("Souris", "19.90", c);
        Map<Long, Integer> zero = new HashMap<>();
        zero.put(souris.getId(), 0);

        assertThrows(IllegalArgumentException.class, () -> commandeService.passerCommande(LocalDate.now(), zero));
        assertThrows(IllegalArgumentException.class, () -> commandeService.passerCommande(LocalDate.now(), new HashMap<>()));
        assertThrows(IllegalArgumentException.class, () -> commandeService.passerCommande(null, zero));
        assertEquals(0, compter("Commande"));
    }

    // ------------------------------------------------------------ Rapport de ventes

    @Test
    public void rapportVentes_totalParCategorieDuMois() {
        Categorie info = categorie("Informatique");
        Categorie elec = categorie("Électronique");
        Produit clavier = produit("Clavier", "50.00", info);
        Produit souris = produit("Souris", "20.00", info);
        Produit casque = produit("Casque", "90.00", elec);

        // Septembre 2026 : Informatique = 3×50 + 1×20 = 170 ; Électronique = 2×90 = 180
        Map<Long, Integer> c1 = new LinkedHashMap<>();
        c1.put(clavier.getId(), 3);
        c1.put(casque.getId(), 2);
        commandeService.passerCommande(LocalDate.of(2026, 9, 5), c1);

        Map<Long, Integer> c2 = new LinkedHashMap<>();
        c2.put(souris.getId(), 1);
        commandeService.passerCommande(LocalDate.of(2026, 9, 30), c2); // dernier jour inclus

        // Octobre : ne doit pas compter dans septembre
        Map<Long, Integer> c3 = new LinkedHashMap<>();
        c3.put(clavier.getId(), 10);
        commandeService.passerCommande(LocalDate.of(2026, 10, 1), c3);

        List<VenteParCategorie> rapport = new RapportService().rapportVentes(YearMonth.of(2026, 9));
        assertEquals(2, rapport.size());

        Map<String, BigDecimal> parNom = new HashMap<>();
        rapport.forEach(v -> parNom.put(v.getCategorie(), v.getChiffreAffaires()));
        assertEquals(new BigDecimal("170.00"), parNom.get("Informatique"));
        assertEquals(new BigDecimal("180.00"), parNom.get("Électronique"));
    }

    @Test
    public void rapportVentes_moisSansVente_listeVide() {
        assertTrue(new RapportService().rapportVentes(YearMonth.of(2020, 1)).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new RapportService().rapportVentes(null));
    }
}