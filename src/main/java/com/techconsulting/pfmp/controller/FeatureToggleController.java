package com.techconsulting.pfmp.controller;

import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.service.FeatureToggleService;
import com.techconsulting.pfmp.utility.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/features")
public class FeatureToggleController {

    private final FeatureToggleService featureToggleService;

    public FeatureToggleController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkFeature(@RequestParam String featureName, @RequestParam Long userId) {
        log.debug("Checking feature: {} for user: {}", featureName, userId);
        try {
            boolean isEnabled = featureToggleService.isFeatureEnabled(featureName, userId);
            log.debug("Feature {} is {}", featureName, isEnabled ? "enabled" : "disabled");
            return ResponseEntity.ok(Map.of("featureName", featureName, "enabled", isEnabled));
        } catch (Exception e) {
            log.error("Error checking feature: {} for user: {}", featureName, userId, e);
            throw e;
        }
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enableFeature(@RequestParam String featureName, @RequestParam Long userId) {
        log.debug("Enabling feature: {} for user: {}", featureName, userId);
        try {
            featureToggleService.enableFeatureForUser(featureName, userId);
            log.info("Feature {} enabled for user: {}", featureName, userId);
            return ResponseEntity.ok("Feature enabled for user.");
        } catch (Exception e) {
            log.error("Error enabling feature: {} for user: {}", featureName, userId, e);
            throw e;
        }
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disableFeature(@RequestParam String featureName, @RequestParam Long userId) {
        log.debug("Disabling feature: {} for user: {}", featureName, userId);
        try {
            featureToggleService.disableFeatureForUser(featureName, userId);
            log.info("Feature {} disabled for user: {}", featureName, userId);
            return ResponseEntity.ok("Feature disabled for user.");
        } catch (Exception e) {
            log.error("Error disabling feature: {} for user: {}", featureName, userId, e);
            throw e;
        }
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getEnabledFeaturesForUser() {
        log.debug("Fetching enabled features for user");
        try {
            UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
            List<String> enabledFeatures = featureToggleService.getEnabledFeatures(userDetails.getId());
            log.debug("Retrieved {} enabled features", enabledFeatures.size());
            return ResponseEntity.ok(Map.of("enabledFeatures", enabledFeatures));
        } catch (Exception e) {
            log.error("Error fetching enabled features", e);
            throw e;
        }
    }
}

