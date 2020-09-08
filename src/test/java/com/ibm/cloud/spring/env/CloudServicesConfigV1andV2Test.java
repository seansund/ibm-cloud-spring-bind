package com.ibm.cloud.spring.env;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 *  This class directly tests CloudServicesPropertySource and
 *  CloudServicesEnvironmentPostProcessor.
 *
 *  CloudServicesConfigMap is completely, though indirectly,
 *  exercised by these tests.
 */
public class CloudServicesConfigV1andV2Test {

    String VCAP_SERVICES = "{\"cloudantNoSQLDB\":[{\"credentials\":{\"username\":\"VCAP_SERVICES-username\",\"password\":\"VCAP_SERVICES-password\",\"host\":\"VCAP_SERVICES.cloudant.com\",\"port\":999,\"url\":\"https://VCAP_SERVICES.cloudant.com\"},\"syslog_drain_url\":null,\"volume_mounts\":[],\"label\":\"cloudantNoSQLDB\",\"provider\":null,\"plan\":\"Lite\",\"name\":\"VCAP_SERVICES-cloudantno-1234567890\",\"tags\":[\"data_management\",\"ibm_created\",\"lite\",\"ibm_dedicated_public\"]}]}";

    String VCAP_SERVICES_USER_PROVIDED = "{\"user-provided\":[{\"credentials\":{\"password\":\"VCAP_SERVICES-password\",\"url\":\"https://VCAP_SERVICES-url\",\"username\":\"VCAP_SERVICES-username\"},\"label\":\"user-provided\",\"name\":\"cloudant-instance-id\"}]}";

    String CLOUDANT_CONFIG_JSON = "{\"cloudant_username\":\"env-json-username\"}";

    private final CloudServicesEnvironmentPostProcessor initializer =
            new CloudServicesEnvironmentPostProcessor();

    private final ConfigurableApplicationContext appContext =
            new AnnotationConfigApplicationContext();

    @Before
    public void setUp() {
        CloudServicesConfigMap.getInstance(Arrays.asList("/mappings.v1.json", "/mappings.v2.json")).setAppContext(appContext);
        initializer.postProcessEnvironment(this.appContext.getEnvironment(), null);
    }

    @Test
    public void getValueCF_v2() {
        System.setProperty("VCAP_SERVICES", VCAP_SERVICES);
        String userName = appContext.getEnvironment().getProperty("cloudant.username");
        System.clearProperty("VCAP_SERVICES");
        assertEquals("VCAP_SERVICES-username", userName);
    }

    @Test
    public void getValueCFUserProvided_v2() {
        System.setProperty("VCAP_SERVICES", VCAP_SERVICES_USER_PROVIDED);
        String userName = appContext.getEnvironment().getProperty("cloudant.username");
        System.clearProperty("VCAP_SERVICES");
        assertEquals("VCAP_SERVICES-username", userName);
    }

    @Test
    public void getValueEnv_v2() {
        System.setProperty("cloudant_username", "env-username");
        String userName = appContext.getEnvironment().getProperty("cloudant.username");
        System.clearProperty("cloudant_username");
        assertEquals("env-username", userName);
    }

    @Test
    public void getValueEnvJSON_v2() {
        System.setProperty("cloudant_config", CLOUDANT_CONFIG_JSON);
        String userName = appContext.getEnvironment().getProperty("cloudant.username");
        System.clearProperty("cloudant_config");
        assertEquals("env-json-username", userName);
    }

    @Test
    public void getValueFile_v2() {
        String userName = appContext.getEnvironment().getProperty("cloudant.username");
        assertEquals("file-json-username", userName);
    }

    @Test
    public void getValueFileJSON_v2() {
        String url = appContext.getEnvironment().getProperty("cloudant.url");
        assertEquals("https://file-url.cloudant.com", url);
    }

    @Test
    public void getValueApplicationProperties_v2() {
        TestPropertySourceUtils.addPropertiesFilesToEnvironment(appContext, "/application.properties");
        String blah = appContext.getEnvironment().getProperty("blah.blah");
        assertEquals("The quick brown fox jumps over the lazy dog.", blah);
    }

    @Test
    public void getValueCF_v1() {
        System.setProperty("VCAP_SERVICES", VCAP_SERVICES);
        String userName = appContext.getEnvironment().getProperty("cloudant_username");
        System.clearProperty("VCAP_SERVICES");
        assertEquals("VCAP_SERVICES-username", userName);
    }

    @Test
    public void getValueCFUserProvided_v1() {
        System.setProperty("VCAP_SERVICES", VCAP_SERVICES_USER_PROVIDED);
        String userName = appContext.getEnvironment().getProperty("cloudant_username");
        System.clearProperty("VCAP_SERVICES");
        assertEquals("VCAP_SERVICES-username", userName);
    }

    @Test
    public void getValueEnv_v1() {
        System.setProperty("cloudant_username", "env-username");
        String userName = appContext.getEnvironment().getProperty("cloudant_username");
        System.clearProperty("cloudant_username");
        assertEquals("env-username", userName);
    }

    @Test
    public void getValueEnvJSON_v1() {
        System.setProperty("cloudant_config", CLOUDANT_CONFIG_JSON);
        String userName = appContext.getEnvironment().getProperty("cloudant_username");
        System.clearProperty("cloudant_config");
        assertEquals("env-json-username", userName);
    }

    @Test
    public void getValueFile_v1() {
        String userName = appContext.getEnvironment().getProperty("cloudant_username");
        assertEquals("file-json-username", userName);
    }

    @Test
    public void getValueFileJSON_v1() {
        String url = appContext.getEnvironment().getProperty("cloudant_url");
        assertEquals("https://file-url.cloudant.com", url);
    }

    @Test
    public void getValueApplicationProperties_v1() {
        TestPropertySourceUtils.addPropertiesFilesToEnvironment(appContext, "/application.properties");
        String blah = appContext.getEnvironment().getProperty("blah.blah");
        assertEquals("The quick brown fox jumps over the lazy dog.", blah);
    }
}