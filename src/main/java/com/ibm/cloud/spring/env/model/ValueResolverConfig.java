package com.ibm.cloud.spring.env.model;

import com.ibm.cloud.spring.env.values.ValueResolver;
import org.springframework.context.ApplicationContext;

public class ValueResolverConfig {
    private ApplicationContext applicationContext;
    private int mappingVersion;
    private String pattern;

    public ValueResolverConfig(ApplicationContext applicationContext, int mappingVersion) {
        this.applicationContext = applicationContext;
        this.mappingVersion = mappingVersion;
    }

    public ValueResolverConfig(ApplicationContext applicationContext, int mappingVersion, String pattern) {
        this(applicationContext, mappingVersion);

        this.pattern = pattern;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public int getMappingVersion() {
        return mappingVersion;
    }

    public void setMappingVersion(int mappingVersion) {
        this.mappingVersion = mappingVersion;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
