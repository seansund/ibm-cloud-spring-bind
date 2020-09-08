package com.ibm.cloud.spring.env.values;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public interface ValueResolver {
    static final String VCAP_SERVICES = "VCAP_SERVICES";

    String get();

    static String getEnvironmentValue(String key) {
        String value = System.getenv(key);

        if (value == null) {
            value = System.getProperty(key);
        }

        return value;
    }

    static String getJsonValue(String jsonPath, String json) {
        if (jsonPath == null || json == null) {
            return null;
        }

        try {
            return JsonPath.parse(json).read(jsonPath);
        } catch (PathNotFoundException e) {
        }

        return null;
    }

}
