package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/annonce-list",
        "/annonce-add",
        "/annonce-update",
        "/annonce-delete"
})
public class AuthenticationFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);

        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        if (isLoggedIn) {
            chain.doFilter(req, res);
            System.out.println("User " + session.getAttribute("user") + " logged in");
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
            System.out.println("User not logged in");
        }
    }

    public void init(FilterConfig config) {}
    public void destroy() {}
}