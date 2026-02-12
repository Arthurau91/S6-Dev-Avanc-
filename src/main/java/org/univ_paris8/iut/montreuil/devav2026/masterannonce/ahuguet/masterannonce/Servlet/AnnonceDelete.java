package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.AnnonceService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "AnnonceDelete", value = "/annonce-delete")
public class AnnonceDelete extends HttpServlet {

    private AnnonceService service = new AnnonceService();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idStr = request.getParameter("id");
        if(idStr != null && !idStr.isEmpty()) {
            Long id = Long.parseLong(idStr);
            service.deleteAnnonce(id);
        }
        response.sendRedirect("annonce-list");
    }
}