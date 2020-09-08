package com.ibm.cloud.spring.env.exceptions;

public class ConfigurationValueNotFound extends RuntimeException {
    public ConfigurationValueNotFound(String name) {
        super("Configuration value not found: " + name);
    }
}
