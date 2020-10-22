package automation.execution.repositories;

import automation.configuration.ProjectConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import automation.datasources.FileManager;
import automation.datasources.JSONConverter;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handling of locators for Keywords<br>
 *
 */
public class LocatorsRepository {

    private static final Logger logger = LoggerFactory.getLogger(LocatorsRepository.class);

    private static String LOCATORS_DIR = "src/test/automation/resources/locators/" + ProjectConfiguration.getConfigProperty("EnvType") + "/";
    private static String MOBILE_MARKER = "_mobile";
    private static String GENERAL_LOCATORS_DIR = "src/test/automation/resources/locators/" + ProjectConfiguration.getConfigProperty("EnvType") + (ProjectConfiguration.getConfigProperty("LocatorsDir").contains(MOBILE_MARKER) ? "/general_mobile/" : "/general/");

    private static String LOCATOR_REPLACEMENT_PLACEHOLDER = "$PARAMETER";
    private static String PARAMETER_DELIMITER = ":";

    private TestDataRepository testDataRepository;

    public LocatorsRepository(TestDataRepository testDataRepository) {
        this.testDataRepository = testDataRepository;
    }

    private static String TEST_LOCATORS_DIR = LOCATORS_DIR + ProjectConfiguration.getConfigProperty("LocatorsDir");

    //locators specific to Environment and Subsidiary
    private static Map<String, Object> TEST_LOCATORS = loadAllLocators();
    //locators general for all Subsidiaries
    private static Map<String, Object> TEST_LOCATORS_GENERAL = loadGeneralLocators();

    /**
     * Load all locators stored in Local Locators files
     * @return map of locators
     */
    private static Map<String, Object> loadAllLocators() {
        TreeMap<String, Object> result = new TreeMap<String, Object>();
        Collection<File> files = FileUtils.listFiles(new File(TEST_LOCATORS_DIR),
                new String[]{"prop"}, true);

        for (File file : files) {
            String fileKey = file.getName().replace(".prop", "").toLowerCase();
            Map<String, String> propertiesFromFile = FileManager.loadProperties(file);
            for (Map.Entry<String, String> item : propertiesFromFile.entrySet()) {
                result.put(fileKey + "." + item.getKey().toLowerCase(), item.getValue());
            }
        }
        return result;
    }

    /**
     * Load all locators stored in General Locators files
     * @return map of locators
     */
    private static Map<String, Object> loadGeneralLocators() {
        TreeMap<String, Object> result = new TreeMap<String, Object>();
        Collection<File> files = FileUtils.listFiles(new File(GENERAL_LOCATORS_DIR),
                new String[]{"prop"}, true);

        for (File file : files) {
            String fileKey = file.getName().replace(".prop", "").toLowerCase();
            Map<String, String> propertiesFromFile = FileManager.loadProperties(file);
            for (Map.Entry<String, String> item : propertiesFromFile.entrySet()) {
                result.put(fileKey + "." + item.getKey().toLowerCase(), item.getValue());
            }
        }

        return result;
    }

    /**
     * Get Locator field value. <br> Locator field format [file.]field
     *
     * @param locatorField string that represents locator value
     * @return locator (url or xpath) or null
     */
    public String getTarget(String locatorField) throws Exception {
        logger.info("Get locator:" + locatorField);

        if (checkThatLocatorFieldShouldBeReturnedAsIs(locatorField))
            return ProjectConfiguration.replaceGlobalVariables(locatorField);

        if (checkThatLocatorFieldIsNull(locatorField))
            return null;

        // replace final result with configuration parameters
        return  getValueForLocatorsField(locatorField);

    }

    /**
     * Check if locatorField equals null
     *
     * @param locatorField string that represents locator value
     * @return is locator is null
     */
    private boolean checkThatLocatorFieldIsNull(String locatorField) {
        if (locatorField == null) {
            logger.info("Locator: null");
            return true;
        }
        if (!locatorField.matches("^[a-zA-Z]+(\\w)*\\.(\\w)*.*")) {
            logger.info("Locator: do not match expected pattern - null");
            return true;
        }
        return false;
    }

    /**
     * @param locatorField string that represents locator value
     * @return is locator should be returned as-is
     */
    private boolean checkThatLocatorFieldShouldBeReturnedAsIs(String locatorField) {

        // return "AS IS"
        if (locatorField.startsWith("/") || locatorField.startsWith("(") || locatorField.startsWith("http")) {
            logger.info("Locator AS-IS: " + locatorField);
            return true;
        }
        return false;
    }

    /**
     * Get locator value from TEST_LOCATORS
     *
     * @param locatorField string that represents locator value
     * @return locator value (xpath, url etc.)
     */
    private String getValueForLocatorsField(String locatorField) throws Exception {
        String result = "";
        String parameters = "";
        String locatorFieldWithoutParameters = "";

        parameters = getParametersFromLocatorField(locatorField);
        locatorFieldWithoutParameters = locatorField.replace(PARAMETER_DELIMITER + parameters, "");

        try {
            // get locator
            result = getLocatorValueWithoutParameters(locatorFieldWithoutParameters);
            //parameters processing
            result = replaceParameters(result, parameters);
            if(result != null)
                result = ProjectConfiguration.replaceGlobalVariables(result);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception("Locator was not found: " + locatorField);
        }
        logger.info("Locator: " + result);
        return result;
    }

    /**
     * Get locator values (without parameters processing)
     *
     * @param locatorField string that represents locator value
     * @return locator value (without processed parameters)
     */
    private String getLocatorValueWithoutParameters(String locatorField) {
        String key = "";
        String value = "";
        String valueField = "";

        //get list of fields (key.value.field)
        String[] fields = locatorField.split("\\.");
        key = fields[0].toLowerCase();
        value = fields[1].toLowerCase();

        //object is complex (map) and field is requested
        if (fields.length == 3) {
            valueField = fields[2];
        }

        String fullKey = key + "." + value;

        if (valueField.equals("")) {
            if (TEST_LOCATORS.containsKey(fullKey))
                return (String) TEST_LOCATORS.get(fullKey);
            else // general
                return (String) TEST_LOCATORS_GENERAL.get(fullKey);
        } else {
            if (TEST_LOCATORS.containsKey(fullKey))
                return JSONConverter.toHashMapFromJsonString((String) TEST_LOCATORS.get(fullKey)).get(valueField);
            else // general
                return JSONConverter.toHashMapFromJsonString((String) TEST_LOCATORS_GENERAL.get(fullKey)).get(valueField);
        }
    }

    /**
     * Replace parameters from value
     *
     * @param locatorValue     value
     * @param parametersString parameters
     * @return locator value with replaced parameters
     * @throws Exception exception
     */
    private String replaceParameters(String locatorValue, String parametersString) throws Exception {
        //replace parameters
        if (!parametersString.equals("")) {
            String[] allParameterFields = parametersString.split(PARAMETER_DELIMITER);

            String finalResultOfParametersSubstitution = locatorValue;

            for (int i = 0; i < allParameterFields.length; i++) {
                String dataField = allParameterFields[i];
                finalResultOfParametersSubstitution = finalResultOfParametersSubstitution
                        .replace(
                                LOCATOR_REPLACEMENT_PLACEHOLDER + (i + 1),
                                (String) testDataRepository.getData(dataField)
                        );
            }
            logger.info("Locator: " + finalResultOfParametersSubstitution);
            return finalResultOfParametersSubstitution;
        }
        //no changes required
        return locatorValue;
    }

    /**
     * Get parameters from locatorField string
     *
     * @param locatorField string that represents locator value
     * @return locator value (xpath, url etc.)
     */
    private String getParametersFromLocatorField(String locatorField) {
        if (locatorField.indexOf(PARAMETER_DELIMITER) > 0) {
            //parameters processing
            return locatorField.substring(locatorField.indexOf(PARAMETER_DELIMITER) + 1);
        }
        return "";
    }


    /**
     * Get Target as Map from JSON string
     *
     * @param target field in format key.value
     * @return map of fields from JSON string
     * @throws Exception possible exception
     */
    public Map<String, String> getComplexTarget(String target) throws Exception {
        return JSONConverter.toHashMapFromJsonString(getTarget(target));
    }
}

