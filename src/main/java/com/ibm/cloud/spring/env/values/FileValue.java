package com.ibm.cloud.spring.env.values;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.cloud.spring.env.exceptions.CloudServicesException;
import com.ibm.cloud.spring.env.ConfigUtil;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ibm.cloud.spring.env.ConfigUtil.EMPTY_DOCUMENT;

public class FileValue implements ValueResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileValue.class);
    private static final ConcurrentMap<String, DocumentContext> resourceCache = new ConcurrentHashMap<>();    //used to cache resources loaded during processing

    private ApplicationContext appContext;
    private int mappingsVersion;
    private String filename;
    private String pattern;

    public FileValue(ApplicationContext appContext, int mappingsVersion, String pattern) {
        this(appContext, mappingsVersion, parseSearchPattern(pattern));
    }

    public FileValue(ApplicationContext appContext, int mappingsVersion, String[] values) {
        this.appContext = appContext;
        this.mappingsVersion = mappingsVersion;
        this.filename = values[0];
        this.pattern = values[1];
    }

    private static String[] parseSearchPattern(String pattern) {
        if (pattern.startsWith("file:")) {
            pattern = pattern.substring("file:".length());
        }

        if (!pattern.contains(":")) {
            return new String[] {pattern, ""};
        }

        return pattern.split(":");
    }

    @Override
    public String get() {
        String value = null;
        if (!StringUtils.isEmpty(pattern)) {
            if (pattern.startsWith("$")) {
                try {
                    String path = filename;
                    DocumentContext context = resourceCache.computeIfAbsent(path, filePath -> getJsonStringFromFile(filePath));
                    value = context.read(pattern);
                } catch (PathNotFoundException e) {
                    return null;    //no data matching the specified json path
                }
            }
        } else {
            // if no location within the file has been specified then
            // assume that the value == the first line of the file contents
            // Relative path means it's a classpath resource
            String path;
            if (mappingsVersion > 1) {
                path = filename.startsWith("/") ? "file:" + filename.trim() : "classpath:" + filename.trim();
            } else {
                path = filename.startsWith("/server/") ? "classpath:" + filename.substring("/server/".length()) : "file:" + filename.trim();
            }

            LOGGER.debug("Looking for resource : " + path);
            try {
                Resource resource = appContext.getResource(path);
                if (resource.exists()) {
                    InputStream fstream = resource.getInputStream();
                    if (fstream != null) {
                        InputStreamReader isReader = new InputStreamReader(fstream);
                        BufferedReader reader = new BufferedReader(isReader);
                        value = reader.readLine();
                    }
                }
            } catch (IOException e) {
                LOGGER.debug("Unexpected exception getting ObjectMapper for mappings.json: " + e);
                throw new CloudServicesException("Unexpected exception getting ObjectMapper for mappings.json", e);
            }
        }
        return value;
    }

    DocumentContext getJsonStringFromFile(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return EMPTY_DOCUMENT;    //parse an empty object and set that for the context if the file cannot be loaded for some reason
        }

        String json = null;
        // Relative path or /server/ means it's a classpath resource
        if (!filePath.startsWith("/") || filePath.startsWith("/server/")) {
            String path = filePath.startsWith("/server/") ? filePath.substring("/server/".length()) : filePath;
            LOGGER.debug("Looking for classpath resource : " + path);
            JsonNode node = ConfigUtil.getJson(path);
            if (node != null) {
                json = node.toString();
                LOGGER.debug("Class path json : " + json);
            }
        } else {
            // absolute path
            LOGGER.debug("Looking for file: " + filePath);
            try {
                json = new String(Files.readAllBytes(Paths.get(filePath)));
            } catch (Exception e) {
                LOGGER.debug("Unexpected exception reading JSON string from file: " + e);
            }
        }

        if (json == null) {
            return EMPTY_DOCUMENT;    //parse an empty object and set that for the context if the file cannot be loaded for some reason
        }

        return JsonPath.parse(json);
    }
}
