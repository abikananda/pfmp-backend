package com.techconsulting.pfmp.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeatureEnabled {
    String value(); // Feature name
}

