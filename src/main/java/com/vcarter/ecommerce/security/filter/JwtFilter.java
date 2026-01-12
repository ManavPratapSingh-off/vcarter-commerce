package com.vcarter.ecommerce.security.filter;

import com.vcarter.ecommerce.security.util.JwtUtil;
import com.vcarter.ecommerce.service.CustomUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil util;

    @Autowired
    private CustomUserService customUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            logger.info("Authorization header: "+authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.info("No valid Bearer token found in Authorization header.");
                // dump headers to help debugging client requests
                Enumeration<String> names = request.getHeaderNames();
                if (names != null) {
                    String headers = Collections.list(names).stream()
                            .map(name -> name + "=" + request.getHeader(name))
                            .collect(Collectors.joining(", "));
                    logger.info("Request headers: "+ headers);
                }
            }

            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                String masked = token.length() > 10 ? token.substring(0, 8) + "..." : token;
                logger.info("Extracted token (masked): {}"+ masked);
            }

            String username = token != null ? util.extractUsername(token) : null;
            logger.info("Username extracted from token: {}"+ username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = null;
                try {
                    userDetails = customUserService.loadUserByUsername(username);
                    logger.info("Loaded UserDetails for {}"+ username+ " (authorities: {})"+ userDetails.getAuthorities());
                } catch (Exception e) {
                    logger.error("Failed to load user "+username+": "+e.getMessage() );
                }

                boolean valid = token != null && util.isTokenValid(token, username);
                logger.info("Token valid for "+username+": "+valid);

                if (valid) {
                    // Prefer authorities from UserDetails when available
                    List<GrantedAuthority> authorities;
                    if (userDetails != null && userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
                        authorities = userDetails.getAuthorities().stream().map(a -> (GrantedAuthority) a).collect(Collectors.toList());
                    } else {
                        // fallback to role in token
                        String role = util.extractRole(token);
                        logger.info("Role extracted from token: "+role);
                        String authority = (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + role;
                        authorities = List.of(new SimpleGrantedAuthority(authority));
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails != null ? userDetails : username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("SecurityContext set with authentication for "+username);
                } else {
                    logger.info("Token invalid or expired for "+username+", not setting authentication");
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in JwtFilter: "+ex.getMessage()+" | "+ ex);
        }

        filterChain.doFilter(request, response);
    }
}

