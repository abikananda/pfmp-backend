package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.FeatureToggle;
import com.techconsulting.pfmp.repository.FeatureToggleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FeatureToggleService {
    @Autowired
    private FeatureToggleRepository featureToggleRepository;

    public boolean isFeatureEnabled(String featureName, Long userId) {
        log.debug("Checking if feature {} is enabled for user {}", featureName, userId);
        try {
            boolean enabled = featureToggleRepository.findByFeatureNameAndUserId(featureName, userId)
                    .map(FeatureToggle::isEnabled)
                    .orElse(false); // Default to disabled if not found
            log.debug("Feature {} enabled status for user {}: {}", featureName, userId, enabled);
            return enabled;
        } catch (Exception e) {
            log.error("Error checking feature {} for user {}", featureName, userId, e);
            return false;
        }
    }

    public void enableFeatureForUser(String featureName, Long userId) {
        log.debug("Enabling feature {} for user {}", featureName, userId);
        try {
            FeatureToggle toggle = featureToggleRepository.findByFeatureNameAndUserId(featureName, userId)
                    .orElse(new FeatureToggle());
            toggle.setFeatureName(featureName);
            toggle.setUserId(userId);
            toggle.setEnabled(true);
            featureToggleRepository.save(toggle);
            log.info("Feature {} enabled for user {}", featureName, userId);
        } catch (Exception e) {
            log.error("Error enabling feature {} for user {}", featureName, userId, e);
            throw e;
        }
    }

    public void disableFeatureForUser(String featureName, Long userId) {
        log.debug("Disabling feature {} for user {}", featureName, userId);
        try {
            featureToggleRepository.findByFeatureNameAndUserId(featureName, userId)
                    .ifPresent(toggle -> {
                        toggle.setEnabled(false);
                        featureToggleRepository.save(toggle);
                        log.info("Feature {} disabled for user {}", featureName, userId);
                    });
        } catch (Exception e) {
            log.error("Error disabling feature {} for user {}", featureName, userId, e);
            throw e;
        }
    }

    public List<String> getEnabledFeatures(Long userId) {
        log.debug("Fetching enabled features for user {}", userId);
        try {
            List<String> features = featureToggleRepository.findByUserIdAndEnabledTrue(userId)
                    .stream()
                    .map(FeatureToggle::getFeatureName)
                    .collect(Collectors.toList());
            log.debug("Retrieved {} enabled features for user {}", features.size(), userId);
            return features;
        } catch (Exception e) {
            log.error("Error fetching enabled features for user {}", userId, e);
            throw e;
        }
    }

}


