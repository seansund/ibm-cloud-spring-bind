package com.ibm.cloud.spring.env.values;

import com.ibm.cloud.spring.env.model.ValueResolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class ValueResolverFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueResolverFactory.class);

    private static final Map<String, Function<ValueResolverConfig, ValueResolver>> BUILDERS = new WeakHashMap();

    static {
        BUILDERS.put("user-provided", config -> new UserProvidedValue(config.getPattern()));
        BUILDERS.put("cloudfoundry", config -> new CloudFoundryValue(config.getPattern()));
        BUILDERS.put("env", config -> new EnvironmentValue(config.getPattern()));
        BUILDERS.put("file", config -> new FileValue(config.getApplicationContext(), config.getMappingVersion(), config.getPattern()));
    }

    public static ValueResolver getValueResolver(ApplicationContext applicationContext, int mappingVersion, String pattern) {
        final String[] tokens = pattern.split(":");

        Function<ValueResolverConfig, ValueResolver> f = BUILDERS.get(tokens[0]);
        if (f == null) {
            LOGGER.warn("Unable to find a resolver for search pattern: " + tokens[0] + ", " + pattern);
            return null;
        }

        return f.apply(new ValueResolverConfig(applicationContext, mappingVersion, pattern));
    }
}
