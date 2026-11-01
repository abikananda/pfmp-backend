package com.techconsulting.pfmp.security;

import com.techconsulting.pfmp.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    JwtHelper jwtHelper;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = getJwtToken(request);
            if (token != null) {
                log.debug("JWT token found in request from path: {}", request.getRequestURI());
                String username = jwtHelper.getUsernameFromToken(token);
                if (StringUtils.isNotEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("Username extracted from token: {}", username);
                    //fetch user detail from username
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    Boolean isTokenValid = this.jwtHelper.validateJwtToken(token, userDetails);
                    if (isTokenValid) {
                        //set the authentication
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("User {} authenticated successfully", username);
                    } else {
                        log.error("Invalid JWT Token for user: {}", username);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                        return;
                    }
                } else {
                    log.debug("Username is empty or authentication already set");
                }
            } else {
                log.debug("No JWT token found in request");
            }
        } catch (Exception e) {
            log.error("Can NOT set user authentication -> Message: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("JWT token found in Authorization header");
            return authHeader.replace("Bearer ", "");
        }
        log.debug("No Authorization header found or invalid format");
        return null;
    }
}
