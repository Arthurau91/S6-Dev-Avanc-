package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceDAO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "AnnonceUpdate", value = "/annonce-update")
public class AnnonceUpdate extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        if(idStr != null) {
            int id = Integer.parseInt(idStr);
            AnnonceDAO dao = new AnnonceDAO();
            Annonce a = dao.find(id);

            if (a == null) {
                response.sendRedirect("annonce-list");
            } else {
                request.setAttribute("annonce", a);
                this.getServletContext().getRequestDispatcher("/Annonce/AnnonceUpdate.jsp").forward(request, response);
            }
        } else {
            response.sendRedirect("annonce-list");
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        System.out.println("--- DEBUG UPDATE ---");
        System.out.println("ID reçu : " + id);
        System.out.println("Titre reçu : " + title);

        Annonce a = new Annonce(title, description, adress, mail);
        a.setId(id);

        AnnonceDAO dao = new AnnonceDAO();

        boolean success = dao.update(a);
        System.out.println("Update réussi ? : " + success);
        System.out.println("--------------------");

        response.sendRedirect("annonce-list");
    }
}