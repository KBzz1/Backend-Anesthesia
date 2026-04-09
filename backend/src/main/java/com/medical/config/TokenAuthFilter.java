package com.medical.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.medical.service.auth.TokenValidator;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple, reusable token filter. Enable via auth.enabled=true and set auth.shared-secret.
 * Paths in auth.ignore-paths will bypass validation. Header defaults to Authorization: Bearer <token>.
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final TokenValidator tokenValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${auth.enabled:false}")
    private boolean authEnabled;

    @Value("${auth.header:Authorization}")
    private String headerName;

    @Value("${auth.bearer-prefix:Bearer }")
    private String bearerPrefix;

    @Value("${auth.ignore-paths:/error,/actuator/**}")
    private String ignorePaths;

    public TokenAuthFilter(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!authEnabled) {
            return true;
        }
        String path = request.getRequestURI();
        List<String> ignores = List.of(ignorePaths.split(","));
        return ignores.stream().anyMatch(pattern -> pathMatcher.match(pattern.trim(), path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!authEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawHeader = request.getHeader(headerName);
        if (!StringUtils.hasText(rawHeader)) {
            reject(response, "missing token header" + (StringUtils.hasText(headerName) ? (": " + headerName) : ""));
            return;
        }

        String token = rawHeader;
        if (StringUtils.hasText(bearerPrefix) && rawHeader.startsWith(bearerPrefix)) {
            token = rawHeader.substring(bearerPrefix.length());
        }

        if (!tokenValidator.isValid(token)) {
            reject(response, "invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":0,\"msg\":\"" + message + "\"}");
    }
}
