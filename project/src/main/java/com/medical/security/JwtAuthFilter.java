package com.medical.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PROTECTED_PATH = "/surgeryArea/anesthesiologist2";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthEntryPoint authEntryPoint;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, JwtAuthEntryPoint authEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authEntryPoint = authEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filtering for non-protected paths and preflight requests
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
            || path == null
            || !path.startsWith(PROTECTED_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            authEntryPoint.commence(request, response,
                new BadCredentialsException("Missing Authorization header"));
            return;
        }

        try {
            jwtTokenProvider.validateToken(token);
            String username = jwtTokenProvider.getUsername(token);
            Long staffId = jwtTokenProvider.getStaffId(token);

            if (staffId == null) {
                authEntryPoint.commence(request, response,
                    new BadCredentialsException("Token missing or invalid staffId claim"));
                return;
            }

            // Attach staffId for downstream controllers/services
            request.setAttribute("auth.staffId", staffId);

            StaffPrincipal principal = new StaffPrincipal(username, staffId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.emptyList()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            authEntryPoint.commence(request, response, new BadCredentialsException("Invalid or expired token", ex));
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
