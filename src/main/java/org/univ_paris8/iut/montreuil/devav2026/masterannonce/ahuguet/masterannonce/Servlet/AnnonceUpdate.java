package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.AnnonceService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "AnnonceUpdate", value = "/annonce-update")
public class AnnonceUpdate extends HttpServlet {

    private AnnonceService service = new AnnonceService();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        if(idStr != null) {
            Long id = Long.parseLong(idStr);
            Annonce a = service.getAnnonce(id);

            if (a == null) {
                response.sendRedirect("annonce-list");
            } else {
                request.setAttribute("annonce", a);
                request.setAttribute("categories", service.getAllCategories());
                this.getServletContext().getRequestDispatcher("/Annonce/AnnonceUpdate.jsp").forward(request, response);
            }
        } else {
            response.sendRedirect("annonce-list");
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Long.parseLong(request.getParameter("id"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");
        Long categoryId = Long.parseLong(request.getParameter("categoryId"));

        Annonce a = new Annonce(title, description, adress, mail);

        service.updateAnnonce(id, a, categoryId);

        response.sendRedirect("annonce-list");
    }
}