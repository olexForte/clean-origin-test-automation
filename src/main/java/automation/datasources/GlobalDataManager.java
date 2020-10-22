package automation.datasources;

import automation.entities.PersistentDataObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * Handling of Global data from Google sheets
 */

public class GlobalDataManager {

    GoogleDocsHelper googleDocs = null;

    public PersistentDataObject getGlobalField(String key, String parameters) throws GeneralSecurityException, IOException {

        PersistentDataObject dataObject = new PersistentDataObject();
        String type = getObjectTypeFromParameters(parameters);
        dataObject.setType(type);
        getTestDateObjectFromPersistentStorage(dataObject);
        return dataObject;
    }

    /**
     * Get data from persistent storage (Google)
     * @param object PersistentDataObject
     * @throws GeneralSecurityException exception
     * @throws IOException exception
     */
    public void getTestDateObjectFromPersistentStorage(PersistentDataObject object) throws GeneralSecurityException, IOException {

        if(googleDocs == null)
            googleDocs = new GoogleDocsHelper();
        googleDocs.getDataForObject(object);

    }

    /**
     *
     * @param object PersistentDataObject
     * @throws GeneralSecurityException exception
     * @throws IOException possible exception
     */
    public void putTestDataObjectToPersistentStorage (PersistentDataObject object) throws GeneralSecurityException, IOException {
        if(googleDocs == null)
            googleDocs = new GoogleDocsHelper();
        object.setType(getTypeOfDataForObject(object.getData()));
        googleDocs.addDataToSheet(object);
    }

    /**
     * Get test data object type based on Keys from object
     * @param object
     * @return
     */
    private static String getTypeOfDataForObject(Map<String, String> object) {
        String type = "String";
        //TODO
        return type;
    }

    /**
     * Based on key name of object generate Type
     * @param parameters
     * @return get object type
     */
    private String getObjectTypeFromParameters(String parameters) {
        //TODO
        return parameters;
    }


    //TODO future tasks
//    public <T extends BaseEntity> T getTestDateObjectFromPersistentStorage(String id, Class<T> clazz) throws ClassNotFoundException {
//        TestClientEntity client = new TestClientEntity();
//        client.name = "Alex";
//        client.password = "AlexPPP";
//        String json = JSONConverter.objectToJson(client);
//        return JSONConverter.toObjectFromJson(clazz, json);
//
//    }

}
