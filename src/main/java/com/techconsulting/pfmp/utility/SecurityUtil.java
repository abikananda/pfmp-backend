package com.techconsulting.pfmp.utility;

import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {
    public static UserDetailsImpl getLoggedInUser() {
        log.debug("Retrieving logged-in user from SecurityContextHolder");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                log.error("No authenticated user found!");
                throw new IllegalStateException("No authenticated user found!");
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                log.debug("Retrieved user: {}", userDetails.getUsername());
                return userDetails;
            } else {
                log.error("Unexpected authentication principal type: {}", principal.getClass());
                throw new IllegalStateException("Unexpected authentication principal type: " + principal.getClass());
            }
        } catch (Exception e) {
            log.error("Error retrieving logged-in user", e);
            throw e;
        }
    }
}
