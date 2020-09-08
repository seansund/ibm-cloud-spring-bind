package com.ibm.cloud.spring.env.values;

import com.ibm.cloud.spring.env.exceptions.CloudServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class EnvironmentValue implements ValueResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentValue.class);

    private String key;
    private String pattern;

    public EnvironmentValue(String pattern) {
        this(parseSearchPattern(pattern));
    }

    public EnvironmentValue(String[] values) {
        this.key = values[0];
        this.pattern = values[1];
    }

    static String[] parseSearchPattern(String pattern) {
        if (pattern.startsWith("env:")) {
            pattern = pattern.substring("env:".length());
        }

        if (!pattern.contains(":")) {
            return new String[] {pattern, ""};
        }

        return pattern.split(":");
    }

    @Override
    public String get() {

        if (StringUtils.isEmpty(pattern)) {
            LOGGER.debug("Getting environment value: " + key);
            return sanitiseString(ValueResolver.getEnvironmentValue(key));
        }

        if (!pattern.startsWith("$")) {
            // TODO should this be an error?
            return null;
        }

        LOGGER.debug("Extracting JSON value from environment value: " + key + ", " + pattern);
        final String value = ValueResolver.getJsonValue(pattern, ValueResolver.getEnvironmentValue(key));

        return sanitiseString(value);
    }

    String sanitiseString(String data) throws CloudServicesException {
        if (StringUtils.isEmpty(data)) {
            return null;
        }

        char first = data.charAt(0);
        char last = data.charAt(data.length() - 1);
        if ((first == '"' || first == '\'') && (first == last)) {
            return data.substring(1, data.length() - 1);
        }

        return data;
    }
}
