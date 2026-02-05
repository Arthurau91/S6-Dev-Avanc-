package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "AnnonceDelete", value = "/annonce-delete")
public class AnnonceDelete extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idStr = request.getParameter("id");

        if(idStr != null && !idStr.isEmpty()) {
            int id = Integer.parseInt(idStr);
            AnnonceDAO dao = new AnnonceDAO();
            dao.delete(id);
        }

        response.sendRedirect("annonce-list");
    }
}
