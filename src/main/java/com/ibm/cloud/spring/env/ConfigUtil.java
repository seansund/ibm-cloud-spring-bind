package com.ibm.cloud.spring.env;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cloud.spring.env.exceptions.CloudServicesException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ConfigUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    public static final DocumentContext EMPTY_DOCUMENT = JsonPath.parse("{}");

    public static JsonNode getJson(String path) {
        LOGGER.debug("getJson() for " + path);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode mappings = null;
        try {
            Resource resource = new ClassPathResource(path);
            if (resource.exists()) {
                InputStream fstream = resource.getInputStream();
                if (fstream != null) {
                    mappings = mapper.readTree(fstream);
                }
            }
        } catch (IOException e) {
            LOGGER.debug("Unexpected exception getting ObjectMapper for mappings.json: " + e);
            throw new CloudServicesException("Unexpected exception getting ObjectMapper for mappings.json", e);
        }

        LOGGER.debug("getMappings() returned: " + mappings);
        if (mappings == null) {
            LOGGER.warn("Mapping resolution failed : No configuration was found at " + path);
        }

        return mappings;
    }

    public static Stream<JsonNode> elementsAsStream(JsonNode node) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        node.elements(),
                        Spliterator.ORDERED),
                false);
    }

    public static Stream<Map.Entry<String, JsonNode>> fieldsAsStream(JsonNode node) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        node.fields(),
                        Spliterator.ORDERED),
                false);
    }

    public static Collection<String> listFiles(String path) {
        Resource resource = new ClassPathResource(path);

        try {
            String directoryPath = resource.getURL().getPath();
            return Stream.of(new File(directoryPath).listFiles((File f, String s) -> s.endsWith("json")))
                    .map(f -> path + "/" + f.getName())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.warn("Unable to list contents of path: " + path);

            return new ArrayList<>();
        }
    }
}
