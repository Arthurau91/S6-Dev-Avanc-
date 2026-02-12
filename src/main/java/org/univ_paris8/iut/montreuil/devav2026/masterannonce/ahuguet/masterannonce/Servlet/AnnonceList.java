package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.AnnonceService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AnnonceList", value = "/annonce-list")
public class AnnonceList extends HttpServlet {

    private final AnnonceService service = new AnnonceService();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String keyword = request.getParameter("keyword");
        String catIdStr = request.getParameter("categoryId");
        String pageStr = request.getParameter("page");

        Long categoryId = (catIdStr != null && !catIdStr.isEmpty()) ? Long.parseLong(catIdStr) : null;
        int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
        int pageSize = 10;

        List<Annonce> annonces = service.getAnnonces(keyword, categoryId, null, page);
        List<Category> categories = service.getAllCategories();

        long totalAnnonces = service.countAnnonces(keyword, categoryId, null);
        int maxPage = (int) Math.ceil((double) totalAnnonces / pageSize);
        if (maxPage < 1) maxPage = 1;

        request.setAttribute("listeAnnonces", annonces);
        request.setAttribute("categories", categories);
        request.setAttribute("currentKeyword", keyword);
        request.setAttribute("currentCategoryId", categoryId);
        request.setAttribute("currentPage", page);

        request.setAttribute("maxPage", maxPage);

        this.getServletContext().getRequestDispatcher("/Annonce/AnnonceList.jsp").forward(request, response);
    }
}