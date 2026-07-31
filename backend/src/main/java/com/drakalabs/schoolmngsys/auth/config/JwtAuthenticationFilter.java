package com.drakalabs.schoolmngsys.auth.config;

import com.drakalabs.schoolmngsys.auth.service.AccessTokenClaims;
import com.drakalabs.schoolmngsys.auth.service.JwtService;
import com.drakalabs.schoolmngsys.shared.security.AccountAuthenticationDetails;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates a request from its {@code Authorization: Bearer <token>} header. An invalid,
 * missing, or expired token simply leaves the request unauthenticated — {@link SecurityConfig}'s
 * authorization rules (and its entry point) are what turn that into a 401, not this filter.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            try {
                AccessTokenClaims claims = jwtService.parseAccessToken(header.substring(BEARER_PREFIX.length()));

                var authorities = claims.permissions().stream().map(SimpleGrantedAuthority::new).toList();
                var authentication =
                        new UsernamePasswordAuthenticationToken(claims.accountId().toString(), null, authorities);
                authentication.setDetails(new AccountAuthenticationDetails(claims.personType(), claims.personId()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
