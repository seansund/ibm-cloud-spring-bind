package com.ibm.cloud.spring.env.values;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudFoundryValue implements ValueResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudFoundryValue.class);

    private String target;

    public CloudFoundryValue(String target) {
        this.target = extractTarget(target);
    }

    static String extractTarget(String pattern) {
        if (!pattern.contains(":")) {
            return pattern;
        }

        return pattern.split(":")[1];
    }

    @Override
    public String get() {
        if (!target.startsWith("$")) {
            return null;
        }

        LOGGER.debug("Getting value from " + target);

        return ValueResolver.getJsonValue(
                target,
                ValueResolver.getEnvironmentValue(VCAP_SERVICES));
    }
}
