package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceDAO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "AnnonceAdd", value = "/annonce-add")
public class AnnonceAdd extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.getServletContext().getRequestDispatcher("/Annonce/AnnonceAdd.jsp").forward(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        Annonce annonce = new Annonce(title, description, adress, mail);

        AnnonceDAO dao = new AnnonceDAO();
        boolean success = dao.create(annonce);

        if (success) {
            request.setAttribute("message", "Annonce ajoutée avec succès !");
            request.setAttribute("alertType", "alert-success");
        } else {
            request.setAttribute("message", "Erreur lors de l'enregistrement en base.");
            request.setAttribute("alertType", "alert-danger");
        }

        this.getServletContext().getRequestDispatcher("/Annonce/AnnonceAdd.jsp").forward(request, response);
    }
}