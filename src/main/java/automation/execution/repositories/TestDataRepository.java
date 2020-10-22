package automation.execution.repositories;

import automation.configuration.ProjectConfiguration;
import automation.datasources.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Handling of test data for Keywords<br>
 *     supports 4 possible Datasources:
 *     <ol>
 *         <li>file</li>
 *         <li>GLOBAL</li>
 *         <li>SAVED</li>
 *         <li>RANDOM</li>
 *     </ol>
 */
public class TestDataRepository {

    private static final Logger logger = LoggerFactory.getLogger(TestDataRepository.class);

    public static String DATA_RESOURCES = "src/test/automation/resources/data/";
    public static String TEST_DATA_RESOURCES = DATA_RESOURCES + ProjectConfiguration.getConfigProperty("EnvType") + "/" + ProjectConfiguration.getConfigProperty("DataDir");
    private Map<String,Object> TEST_DATA = loadAllData();

    static final String RANDOM_SOURCE_KEY = "random";
    static final String SAVED_SOURCE_KEY = "saved";
    static final String GLOBAL_SOURCE_KEY = "global";

    private static String PARAMETER_DELIMITER = ":";
    private static String DATA_REPLACEMENT_PLACEHOLDER = "$PARAMETER";

    public static String FILE_SEPARATOR_CHAR = "\\|";

    GlobalDataManager globalData = null;

    public TestDataRepository(){
        globalData = new GlobalDataManager();
    }

    /**
     * Load all Data field stored in Data files
     * @return map of data fields
     */
    private Map<String,Object> loadAllData(){
        TreeMap<String,Object> result = new TreeMap<String,Object>();
        Collection<File> files = FileUtils.listFiles(new File(TEST_DATA_RESOURCES),
                new String[]{"prop"}, true);

        for (File file: files) {
            String fileKey = file.getName().replace(".prop", "").toLowerCase();
            Map<String, String> propertiesFromFile = FileManager.loadProperties(file);
            for(Map.Entry<String,String> item : propertiesFromFile.entrySet()) {
                result.put(fileKey + "." + item.getKey().toLowerCase(), item.getValue());
            }
        }
        return result;
    }

    /**
     * Get test data field value. <br> Data field format [file.]field
     * @param dataField
     * @return
     */
    public Object getData(String dataField) throws Exception {
        logger.info("Get field:" + dataField);

        if (checkThatDataFieldIsNull(dataField))
            return null;

        if (checkThatDataFieldShouldBeReturnedAsIs(dataField))
            return ProjectConfiguration.replaceGlobalVariables(dataField); // replace global parameters with values

        return getValueForDataField(dataField);
    }

    /**
     * Get value from Data field
     * @param dataField
     * @return
     * @throws Exception
     */
    private Object getValueForDataField(String dataField) throws Exception {
        String result = "";
        String parameters = "";
        String dataFieldWithoutParameters = "";

        //get parameters and data field
        parameters = getParametersFromDataField(dataField);
        dataFieldWithoutParameters = dataField.replace(PARAMETER_DELIMITER + parameters, "");

        try {
            //get data
            result = getDataFieldValueWithParameters(dataFieldWithoutParameters, parameters);
        } catch (Exception e) {
            logger.error(e.getMessage());
            result = dataField;
        }
        result = ProjectConfiguration.replaceGlobalVariables(result);
        logger.info("Data field: " + result);
        return result;
    }

    /**
     * Get data field and process parameters
     * @param dataField
     * @param parameters
     * @return
     * @throws Exception
     */
    private String getDataFieldValueWithParameters(String dataField, String parameters) throws Exception {
        String result = null;
        String key = "";
        String value = "";
        String valueField = "";

        String[] fields = dataField.split("\\.");
        if(fields.length == 1){
            logger.info("Result Data value:" + dataField);
            result = dataField;
        } else {
            if (fields.length == 2) {
                key = fields[0].toLowerCase();
                value = fields[1].toLowerCase();
            }
            if (fields.length == 3) {
                key = fields[0].toLowerCase();
                value = fields[1].toLowerCase();
                valueField = fields[2];
            }

            String fullKey = key + "." + value;

            switch (key.toLowerCase()) {
                case RANDOM_SOURCE_KEY:
                    result = getRandomData(fullKey, valueField, parameters);
                    break;
//            case GLOBAL_SOURCE_KEY:
//                result = getGlobalData(fullKey, valueField, parameters);
//                break;
                default:
                    result = getDataFieldFromTestData(fullKey, valueField, parameters);

            }
        }
        logger.info("Result Data value: " + result);
        if(result == null){
            logger.warn("Result equals null. Return original value");
            throw new Exception("Result equals null. Return original value");
        }
        return result;
    }

    /**
     * Get data field from Test Data storage
     *
     * @param fullKey
     * @param valueField
     * @param parameters
     * @return
     * @throws Exception
     */
    private String getDataFieldFromTestData(String fullKey, String valueField, String parameters) throws Exception {
        String result = "";
        try{
            if(valueField.equals(""))
                result =  (String) TEST_DATA.get(fullKey);
            else
                result =  getDataObjectField(TEST_DATA.get(fullKey), valueField);

            return replaceParameters(result, parameters);
        }catch (Exception e){
            //throw new Exception("Data field was not found: " + dataField);
            logger.warn("Data field was not found: " + fullKey);
            throw new Exception("Problem with generation of random value: " + fullKey);
        }
    }

//    private String getGlobalData(String fullKey, String valueField, String parameters) {
//        //get GLOBAL data
//        try{
//            if(fullKey.toLowerCase().contains(GLOBAL_SOURCE_KEY)){
//
//                if(TEST_DATA.keySet().contains(fullKey)){
//                    logger.info("Result: " + TEST_DATA.get(fullKey));
//                    return (String)TEST_DATA.get(fullKey);
//                }
//
//                //get object and save it (if it is not present)
//                PersistentDataObject globalValues = globalData.getGlobalField(fullKey, parameters);
//                TEST_DATA.put(fullKey, globalValues);
//                if(valueField.equals("")) {
//                    logger.info("Result: " + globalValues.getData());
//                    return globalValues.getData();
//                }else {
//                    logger.info("Result: " + globalValues.getData().get(valueField));
//                    return globalValues.getData().get(valueField);
//                }
//            }
//        }catch (Exception e){
//            throw new Exception("Problem with getting of Persistent value: " + dataField);
//        }
//    }

    /**
     * Get Randomized Data field
     *
     * @param fullKey
     * @param valueField
     * @param parameters
     * @return
     * @throws Exception
     */
    private String getRandomData(String fullKey, String valueField, String parameters) throws Exception {
        //generate random if it was not generated
        try{
                String randomValue = "";
                if (TEST_DATA.keySet().contains(fullKey)) {
                    logger.info("Random field was already generated: " + TEST_DATA.get(fullKey));
                    return (String)TEST_DATA.get(fullKey);
                }

                //generate if not present
                randomValue = RandomDataGenerator.getRandomField(parameters, PARAMETER_DELIMITER);
                logger.info("Save new Random: " + randomValue);
                TEST_DATA.put(fullKey, randomValue);
                return randomValue;
        }catch (Exception e){
            throw new Exception("Problem with generation of random value: " + fullKey + ":" + parameters);
        }
    }

    /**
     * Check that field is not Null
     *
     * @param dataField
     * @return
     */
    private boolean checkThatDataFieldIsNull(String dataField) {
        if(dataField == null) {
            logger.info("Data field: null");
            return true;
        }
        return false;
    }

    /**
     * Check if field should be returned as-is
     *
     * @param dataField
     * @return
     */
    private boolean checkThatDataFieldShouldBeReturnedAsIs(String dataField) {
        if(!dataField.matches("^[a-zA-Z]+(\\w)*\\.(\\w)*.*")) {
            logger.info("Data field returned As-IS: " + dataField);
            return true;
        }
        return false;
    }

    /**
     * Replace parameters from value
     *
     * @param fieldValue
     * @param parametersString
     * @return
     * @throws Exception
     */
    private String replaceParameters(String fieldValue, String parametersString) throws Exception {
        //replace parameters
        if (!parametersString.equals("")) {
            String[] allParameterFields = parametersString.split(PARAMETER_DELIMITER);

            String finalResultOfParametersSubstitution = fieldValue;

            for (int i = 0; i < allParameterFields.length; i++) {
                String dataField = allParameterFields[i];
                finalResultOfParametersSubstitution = finalResultOfParametersSubstitution
                        .replace(
                                DATA_REPLACEMENT_PLACEHOLDER + (i + 1),
                                (String) getData(dataField)
                        );
            }
            logger.info("Data field with replaced parameters: " + finalResultOfParametersSubstitution);
            return finalResultOfParametersSubstitution;
        }
        //no changes required
        return fieldValue;
    }

    /**
     * Get parameters from dataField string
     *
     * @param dataField string that represents data field value
     * @return locator value (xpath, url etc.)
     */
    private String getParametersFromDataField(String dataField) {
        if (dataField.indexOf(PARAMETER_DELIMITER) > 0) {
            //parameters processing
            return dataField.substring(dataField.indexOf(PARAMETER_DELIMITER) + 1);
        }
        return "";
    }

    /**
     * Get field from JSON object specified
     * @param object
     * @param valueField
     * @return
     */
    private String getDataObjectField(Object object, String valueField) throws Exception {
        return getComplexData((String) object).get(valueField); //default HashMap
    }


    /**
     * Save value to data field
     * @param dataField
     * @param text
     */
    public void setData(String dataField, String text) {
        logger.info("Set: " + dataField + "\n" + text);
        text = text.trim(); // Added by Nazar
        TEST_DATA.put(dataField.toLowerCase(), text); // Nazar
    }

    /**
     * Save map as JSON to Saved field
     * @param dataField
     * @param text
     */
    public void setComplexData(String dataField, Map<String, String> text) {
        setData(dataField, JSONConverter.objectToJson(text));
    }

    /**
     * Save map as JSON to Saved field
     * @param dataField
     * @param text
     */
    public void setComplexData(String dataField, List<HashMap<String, String>> text) {
        setData(dataField, JSONConverter.objectToJson(text));
    }

    /**
     * Get Map of saved objects from JSON string saved in dataField
     * @param dataField data field in formt key.value
     * @return map of items
     * @throws Exception possible exceptions
     */
    public HashMap<String, String> getComplexData(String dataField) throws Exception {
        return JSONConverter.toHashMapFromJsonString((String)getData(dataField));
    }

    /**
     * Get Map of saved objects from JSON string saved in dataField
     * @param dataField data field in formt key.value
     * @return map of items
     * @throws Exception possible exceptions
     */
    public List<HashMap<String, String>> getComplexDataList(String dataField) throws Exception {
        return JSONConverter.toHashMapList((String)getData(dataField));
    }

    /**
     * Get Map of saved objects from JSON string saved in file
     * @param fileName filename from TEST_DATA_RESOURCES
     * @return map of items
     * @throws Exception possible exceptions
     */
    public HashMap<String, String> getComplexDataFromFile(String fileName) throws Exception {
        return JSONConverter.toHashMapFromJsonString((String)getData(FileManager.getFileContent(new File(TEST_DATA_RESOURCES+"/"+fileName))));
    }

    /**
     * Get Object saved in TEST_DATA map (with no transformations)
     * @param objectID id of element in format key.value
     * @return Object from test data
     */
    public Object getTestDataObject(String objectID){
        return TEST_DATA.get(objectID.toLowerCase());
    }

    /**
     * Save Object to TestData
     * @param objectID id of element in format key.value
     * @param object reference to object
     */
    public void setTestDataObject(String objectID, Object object){
        TEST_DATA.put(objectID.toLowerCase(), object);
    }


    /**
     * Get list of lists items from file
     * @param fileName name of file in TEST_DATA_RESOURCES
     * @return List of Lists of Strings with all data from file
     * @throws Exception possible exception
     */
    public List<List<String>> getTableDataFromFile(String fileName) throws Exception {
        List<List<String>> result = new LinkedList<>();
        List<String> lineList = FileManager.getFileContentAsListOfLines(new File(TEST_DATA_RESOURCES + "/" + fileName));
        for(String line : lineList){
            List<String> list = Arrays.asList(line.split(FILE_SEPARATOR_CHAR));
            result.add(list);
        }
        return result;
    }

    /**
     * Get test file name based on dataField
     * @param dataField
     * @return file location
     */
    public File getDataFile(String dataField) throws Exception {
        logger.info("Get data file from field:" + dataField);
        String fieldValue = (String) getData(dataField);
        return FileManager.getFileFromDir(fieldValue, TEST_DATA_RESOURCES);
    }

    /**
     * Get test file name based on dataField
     * @param dataField
     * @return file location
     */
    public File getOutputFile(String dataField) throws Exception {
        logger.info("Get output file from field:" + dataField);
        String fieldValue = (String) getData(dataField);
        return FileManager.getFileFromDir(fieldValue, FileManager.OUTPUT_DIR);
    }

}

