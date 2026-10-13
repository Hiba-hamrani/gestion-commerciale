package org.example.web;

import org.example.service.RapportService;
import org.example.service.VenteParCategorie;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Expose le rapport de ventes : {@code GET /rapport-ventes} (mois précédent)
 * ou {@code GET /rapport-ventes?mois=2026-09}.
 */
@WebServlet("/rapport-ventes")
public class RapportVentesServlet extends HttpServlet {

    private final RapportService rapportService = new RapportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        YearMonth mois;
        try {
            String param = req.getParameter("mois");
            mois = (param == null || param.isBlank()) ? YearMonth.now().minusMonths(1) : YearMonth.parse(param);
        } catch (DateTimeParseException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Format attendu : mois=AAAA-MM");
            return;
        }

        List<VenteParCategorie> rapport = rapportService.rapportVentes(mois);
        BigDecimal total = rapport.stream()
                .map(VenteParCategorie::getChiffreAffaires)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Rapport de ventes</title></head><body>");
        out.println("<h1>Rapport de ventes – " + mois + "</h1>");
        if (rapport.isEmpty()) {
            out.println("<p>Aucune vente ce mois-ci.</p>");
        } else {
            out.println("<table border='1' cellpadding='6'><tr><th>Catégorie</th><th>Chiffre d'affaires</th></tr>");
            for (VenteParCategorie v : rapport) {
                out.println("<tr><td>" + echapper(v.getCategorie()) + "</td><td>" + v.getChiffreAffaires() + "</td></tr>");
            }
            out.println("<tr><th>Total</th><th>" + total + "</th></tr></table>");
        }
        out.println("</body></html>");
    }

    /** Évite d'injecter du HTML si un nom de catégorie contient des caractères spéciaux. */
    private static String echapper(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}