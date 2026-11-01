package com.techconsulting.pfmp.annotation;

import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.service.FeatureToggleService;
import com.techconsulting.pfmp.utility.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Aspect
@Component
public class FeatureToggleAspect {

    @Autowired
    private FeatureToggleService featureToggleService;

    @Around("@annotation(featureToggle)")
    public Object checkFeatureToggle(ProceedingJoinPoint joinPoint, FeatureEnabled featureToggle) throws Throwable {
        // Get the feature name from annotation
        String featureName = featureToggle.value();
        log.debug("Checking feature toggle for: {}", featureName);

        // Retrieve user ID (You can modify this logic based on authentication)
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Long userId = userDetails.getId(); // Implement this method to get logged-in user ID
        log.debug("Feature check for user: {}, feature: {}", userId, featureName);

        // Check if the feature is enabled for the user
        boolean isEnabled = featureToggleService.isFeatureEnabled(featureName, userId);

        if (!isEnabled) {
            log.warn("Feature {} is disabled for user: {}", featureName, userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Feature is disabled for this user.");
        }

        log.debug("Feature {} is enabled for user: {}, proceeding with request", featureName, userId);
        // Proceed with the request if feature is enabled
        return joinPoint.proceed();
    }
}

