package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.Servlet;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    private UserService userService = new UserService();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getServletPath();

        if ("/logout".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect("login");
        } else {
            this.getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String u = request.getParameter("username");
        String p = request.getParameter("password");

        User user = userService.login(u, p);

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            response.sendRedirect("annonce-list");
        } else {
            request.setAttribute("error", "Identifiants invalides");
            this.getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}