package com.ibm.cloud.spring.env;

import com.ibm.cloud.spring.env.exceptions.ConfigurationNameNotFound;
import com.ibm.cloud.spring.env.exceptions.ConfigurationValueNotFound;
import com.ibm.cloud.spring.env.model.MappingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

class CloudServicesConfigMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudServicesConfigMap.class);
    private static final String DEFAULT_MAPPINGS_FILE = "/mappings.json";
    private static CloudServicesConfigMap instance;

    private Collection<String> mappingFiles;
    private MappingConfig config;

    @Autowired
    private ApplicationContext appContext;

    public CloudServicesConfigMap(Collection<String> mappingFiles) {
        this.mappingFiles = mappingFiles;
    }

    /**
     * Create a CloudServicesConfigMap from the map file
     *
     * @return the configured service mapper
     */
    static synchronized CloudServicesConfigMap getInstance() {
        if (instance != null) {
            return instance;
        }

        final Collection<String> configFiles = Arrays.asList(DEFAULT_MAPPINGS_FILE);
        configFiles.addAll(ConfigUtil.listFiles("/mappings"));

        return CloudServicesConfigMap.getInstance(configFiles);
    }

    /**
     * Create a CloudServicesConfigMap from the given map file
     *
     * @param mapFile The name of the mapping file
     * @return The configured service mapper
     **/
    static synchronized CloudServicesConfigMap getInstance(String mapFile) {
        if (mapFile == null) {
            mapFile = DEFAULT_MAPPINGS_FILE;
        }

        return getInstance(Arrays.asList(mapFile));
    }

    static synchronized CloudServicesConfigMap getInstance(Collection<String> mapFiles) {
        if (mapFiles == null) {
            mapFiles = Arrays.asList(DEFAULT_MAPPINGS_FILE);
        }

        return instance = new CloudServicesConfigMap(mapFiles);
    }

    MappingConfig parseConfig() {
        MappingConfig config = new MappingConfig(appContext);

        for (String mapFile : mappingFiles) {
            config.addConfig(ConfigUtil.getJson(mapFile));
        }

        return config;
    }

    /**
     * Get the first value found from the provided searchPatterns, which will be
     * processed in the order provided.
     *
     * @param name The name to be extracted from the "searchPatterns" containing
     *             an array of Strings. Each String is a search pattern
     *             with format "src:target"
     * @return The value specified by the "src:target" or null if not found
     */
    String getValue(String name) {
        if (config == null) {
            config = parseConfig();
        }

        try {
            return config.getValue(name);
        } catch (ConfigurationNameNotFound | ConfigurationValueNotFound e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
    }

    public void setAppContext(ConfigurableApplicationContext appContext) {
        this.appContext = appContext;
    }
}

