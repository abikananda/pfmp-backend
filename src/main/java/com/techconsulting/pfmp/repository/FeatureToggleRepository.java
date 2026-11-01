package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.FeatureToggle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureToggleRepository extends JpaRepository<FeatureToggle, Long> {
    Optional<FeatureToggle> findByFeatureNameAndUserId(String featureName, Long userId);
    List<FeatureToggle> findByUserIdAndEnabledTrue(Long userId);

}

