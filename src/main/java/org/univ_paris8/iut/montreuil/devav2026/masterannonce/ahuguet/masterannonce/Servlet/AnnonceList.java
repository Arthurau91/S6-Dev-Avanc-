package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceDAO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AnnonceList", value = "/annonce-list")
public class AnnonceList extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        AnnonceDAO dao = new AnnonceDAO();
        List<Annonce> annonces = dao.findAll();

        request.setAttribute("listeAnnonces", annonces);

        this.getServletContext().getRequestDispatcher("/Annonce/AnnonceList.jsp").forward(request, response);
    }
}
