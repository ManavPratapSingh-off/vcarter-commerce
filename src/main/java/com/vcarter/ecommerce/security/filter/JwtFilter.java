package com.vcarter.ecommerce.security.filter;

import com.vcarter.ecommerce.security.util.JwtUtil;
import com.vcarter.ecommerce.service.CustomUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil util;

    @Autowired
    private CustomUserService customUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //filteration logic
        String authHeader=request.getHeader("Authorization");
        logger.debug("Authorization Header: " + authHeader);
        String token=null;
        if (authHeader!=null && authHeader.startsWith("Bearer "))    {
            token=authHeader.substring(7);
            logger.debug("Token found (masked): " + (token.length()>8 ? token.substring(0, 8) : token) + "...");
        } else logger.debug("No Bearer Token present in Authorization Header");

        String username = token!=null ? util.extractUsername(token) : null;
        logger.debug("Extracted username: " + username);

        if (username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
            try {
                customUserService.loadUserByUsername(username);
            } catch (Exception e) {
                logger.debug("User lookup failed for "+username+": "+e.getMessage());
            }

            boolean valid = token != null && util.isTokenValid(token, username);
            logger.debug("Token validation result for user "+username+": "+valid);

            if (valid) {
                String role = util.extractRole(token);
                logger.debug("Extracted role from token: "+role);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Authentication set for user "+ username);
            } else {
                logger.debug("Token invalid - not setting authentication for user "+username);
            }
        }
        filterChain.doFilter(request,response);
    }
}
