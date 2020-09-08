package com.ibm.cloud.spring.env.values;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.ibm.cloud.spring.env.ConfigUtil.elementsAsStream;

public class UserProvidedValue implements ValueResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProvidedValue.class);

    private String key;
    private String name;

    public UserProvidedValue(String searchPattern) {
        this(UserProvidedValue.parseSearchPattern(searchPattern));
    }
    public UserProvidedValue(String[] values) {
        key = values[0];
        name = values[1];
    }

    static String[] parseSearchPattern(String values) {
        if (values.startsWith("user-provided:")) {
            values = values.substring("user-provided:".length());
        }

        return values.split(":");
    }

    @Override
    public String get() {
        LOGGER.debug("user-provided entry found:  " + key + ":" + name);

        String vcap_services = ValueResolver.getEnvironmentValue(VCAP_SERVICES);
        if (StringUtils.isEmpty(vcap_services)) {
            LOGGER.debug("No VCAP_SERVICES or no user-provided pattern");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode vs = mapper.readTree(vcap_services);
            JsonNode userProvided = vs.get("user-provided");

            if (!userProvided.isArray()) {
                LOGGER.info("VCAP_SERVICES user-provided field is not an array");
                return null;
            }

            LOGGER.debug("Found user-provided array");
            return elementsAsStream(userProvided)
                    .filter(node -> node.get("name") != null)              // entries with "name" node
                    .filter(node -> key.equals(node.get("name").asText())) // entries with "name" equal key
                    .filter(node -> node.get("credentials") != null)       // entries with "credentials" array
                    .findFirst()
                    .map(node -> (String) JsonPath.parse(node.get("credentials").toString()).read(name))
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.info("Unexpected exception reading VCAP_SERVICES: " + e);
        }

        return null;
    }
}
