package com.shadowengineer.api.auth;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    public static final String AUTH_TOKEN_ATTRIBUTE = "shadowEngineerAuthToken";
    public static final String AUTH_USER_ID_ATTRIBUTE = "shadowEngineerAuthUserId";

    private final SessionStore sessionStore;

    public AuthTokenFilter(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            sessionStore.find(token).ifPresent(session -> {
                request.setAttribute(AUTH_TOKEN_ATTRIBUTE, token);
                request.setAttribute(AUTH_USER_ID_ATTRIBUTE, session.userId());
            });
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("shadow_engineer_session".equals(cookie.getName())) return cookie.getValue();
            }
        }
        return null;
    }
}
