package com.ibm.cloud.spring.env.exceptions;

public class ConfigurationNameNotFound extends RuntimeException {
    public ConfigurationNameNotFound(String configName) {
        super("Configuration name not found: " + configName);
    }
}
