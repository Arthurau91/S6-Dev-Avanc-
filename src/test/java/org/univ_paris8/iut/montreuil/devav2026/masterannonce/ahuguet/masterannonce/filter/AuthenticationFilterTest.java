package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    HttpSession session;
    @Mock
    FilterChain chain;

    @Test
    void testFilterRefuseAccessWhenNotLoggedIn() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/MasterAnnonce");

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/MasterAnnonce/login");

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testFilterAllowsAccessWhenLoggedIn() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new User());

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);

        verify(response, never()).sendRedirect(anyString());
    }
}