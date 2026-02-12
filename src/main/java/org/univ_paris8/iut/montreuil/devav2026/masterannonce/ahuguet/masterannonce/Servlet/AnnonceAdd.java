package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.AnnonceService;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.ValidationUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AnnonceAdd", value = "/annonce-add")
public class AnnonceAdd extends HttpServlet {

    private AnnonceService service = new AnnonceService();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setAttribute("categories", service.getAllCategories());
        this.getServletContext().getRequestDispatcher("/Annonce/AnnonceAdd.jsp").forward(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        Long categoryId = null;
        try {
            categoryId = Long.parseLong(request.getParameter("categoryId"));
        } catch (NumberFormatException e) {
        }

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        Annonce annonce = new Annonce(title, description, adress, mail);

        List<String> errors = ValidationUtils.validate(annonce);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("categories", service.getAllCategories());

            request.setAttribute("formAnnonce", annonce);
            request.setAttribute("selectedCatId", categoryId);

            this.getServletContext().getRequestDispatcher("/Annonce/AnnonceAdd.jsp").forward(request, response);
        } else {
            service.createAnnonce(annonce, categoryId, user.getId());
            response.sendRedirect("annonce-list");
        }
    }
}