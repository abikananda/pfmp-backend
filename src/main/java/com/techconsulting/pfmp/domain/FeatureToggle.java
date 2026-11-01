package com.techconsulting.pfmp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "feature_toggles", uniqueConstraints = @UniqueConstraint(columnNames = {"feature_name", "user_id"}))
@Getter
@Setter
public class FeatureToggle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String featureName;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Long userId;

}

