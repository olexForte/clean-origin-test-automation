package automation.datasources;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import automation.entities.PersistentDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Basic class that supports interaction with GoogleSheets
 *
 * Google sheets setup:<br>
 * https://console.developers.google.com/apis/<br>
 * https://developers.google.com/sheets/api/quickstart/java
 *
 */
public class GoogleDocsHelper {

    private static final int ID_COLUMN_INDEX = 0;
    private static final int TYPE_COLUMN_INDEX = 1;
    private static final int DATA_COLUMN_INDEX = 2;
    private static final int DATE_COLUMN_INDEX = 3;
    private static final int STATUS_COLUMN_INDEX = 4;

    static String READY_STATUS = "Ok";
    static String USED_STATUS = "Used";
    static String ID_PATTERN = "yyyyMMdd_HHmm_ssSSS";

    public Logger LOGGER = LoggerFactory.getLogger(GoogleDocsHelper.class);

    private static final String APPLICATION_NAME = "Automation";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FOLDER = "src/main/resources/credentials/googlesheets"; // Directory to store user credentials.

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CLIENT_CREDENTIALS_FILE = "credentials/googlesheets/googlesheets.json";//client_secret.json";

    final String SPREADSHEET_ID = "1Irs-U8Tt-WZSV2cKo_x3X8RHA_73ODER3h-ze-kuTXo"; //Automation
    final String DEFAULT_SHEET_NAME = "SavedData";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        //InputStream in = GmailHelper.class.getResourceAsStream(CLIENT_SECRET_DIR);
        InputStream in = new FileInputStream(FileManager.getFileFromMainResources(CLIENT_CREDENTIALS_FILE));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    String getSheetName(){
        String sheet;
        sheet = DEFAULT_SHEET_NAME;
        return sheet;
    }

    /**
     * Get item by type from persistent storage
     * @param object
     * @throws GeneralSecurityException exception
     * @throws IOException exception
     */
    public void getDataForObject(PersistentDataObject object) throws GeneralSecurityException, IOException {
        String result = "";
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String range = getSheetName() + "!" + "A:Z";

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> valuesFrom = response.getValues();
        if (valuesFrom == null || valuesFrom.isEmpty() || valuesFrom.size() < 2) {
            LOGGER.warn("No data found.");
        } else {

            //String expectedId = object.getId(); //TODO
            String expectedType = object.getType();
            //String expectedStatus = object.getStatus();

            for (int rowIndex = 1; rowIndex < valuesFrom.size(); rowIndex++) {
                if (valuesFrom.get(rowIndex).get(TYPE_COLUMN_INDEX).equals(expectedType) && valuesFrom.get(rowIndex).get(STATUS_COLUMN_INDEX).equals(READY_STATUS)) {
                    object.setDataFromJSON((String) valuesFrom.get(rowIndex).get(DATA_COLUMN_INDEX));
                    object.setId((String) valuesFrom.get(rowIndex).get(ID_COLUMN_INDEX));
                    object.setDate((LocalDateTime) valuesFrom.get(rowIndex).get(DATE_COLUMN_INDEX));
                    synchronized (GoogleDocsHelper.class) {
                        setValue(rowIndex, STATUS_COLUMN_INDEX, USED_STATUS);
                    }
                }
            }
        }
    }

    /**
     * Add data to sheet
     * @param dataObject data object
     * @throws GeneralSecurityException
     * @throws IOException exception
     */
    public void addDataToSheet(PersistentDataObject dataObject) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String range = getSheetName() + "!" + "A:A";

        Sheets googleService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        //values
        LocalDateTime date = LocalDateTime.now();
        String id = date.format(DateTimeFormatter.ofPattern(ID_PATTERN));
        String status = READY_STATUS;
        String type = dataObject.getType();
        String data = dataObject.getDataAsJSONString();

        dataObject.setId(id);
        dataObject.setDate(date);
        dataObject.setStatus(status);

        List<List<Object>> values = Arrays.asList(
                Arrays.asList( id, type, data, date, status )
        );

        ValueRange requestBody = new ValueRange()
                .setValues(values);

        String valueInputOption = "RAW";

        try {
            AppendValuesResponse result =
                    googleService.spreadsheets().values().append(SPREADSHEET_ID, range, requestBody)
                            .setValueInputOption(valueInputOption)
                            .execute();
        } catch (Exception e){
            LOGGER.error("Client id was not added to Google Docs");
            e.printStackTrace();
        }
    }




    /**
     * get list of items from column
     * @param rangeLetter letter of column ( Example: 'A' )
     * @return list of items from column
     * @throws GeneralSecurityException exception
     * @throws IOException exception
     */
    public List<String> getListOfItemsInRange (String rangeLetter) throws GeneralSecurityException, IOException {
        List<String> result = new ArrayList<>();

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String range =  DEFAULT_SHEET_NAME + "!" + rangeLetter;

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> valuesFrom = response.getValues();
        if (valuesFrom == null || valuesFrom.isEmpty() || valuesFrom.size() < 1) {
            LOGGER.info("No data found.");
        } else {
            for (List row : valuesFrom) {
                if(row.size() > 0)
                    for(Object value : row)
                        result.add((String) value);
            }
        }
        return result;
    }

    /**
     * Set value by sheet row index and col index
     * @param rowIndex
     * @param columnIndex
     * @param value
     * @throws GeneralSecurityException exception
     * @throws IOException exception
     */
    public void setValue(int rowIndex, int columnIndex, String value) throws GeneralSecurityException, IOException {
        setValue( String.valueOf(rowIndex),  String.valueOf(columnIndex),  value);
    }

    public void setValue(String rowIndex, String columnIndex, String value) throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String range = DEFAULT_SHEET_NAME + "!" + "R" + rowIndex + "C" + columnIndex;

        String valueInputOption = "RAW";

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        //values
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(value)
        );
        ValueRange requestBody = new ValueRange()
                .setValues(values);

        service.spreadsheets().values().update(SPREADSHEET_ID, range, requestBody)
                .setValueInputOption(valueInputOption)
                .execute();

    }



    public String getValue (int row, int col) throws GeneralSecurityException, IOException {
        String result = " ";

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String rangeLetter = "R" + row + "C" + col;

        String range =  DEFAULT_SHEET_NAME + "!" + rangeLetter;

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> valuesFrom = response.getValues();
        if (valuesFrom == null || valuesFrom.isEmpty() || valuesFrom.size() < 1) {
            LOGGER.info("No data found.");
        } else {
            for (List rowValues : valuesFrom) {
                if(rowValues.size() > 0)
                    for(Object value : rowValues)
                        result = (String) value;
            }
        }
        return result;
    }

    public void copyColumn(int oldIndex, int newColumn) throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String originalRange = DEFAULT_SHEET_NAME + "!" + "R1C" + oldIndex + ":C" + oldIndex ;

        String valueInputOption = "RAW";

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, originalRange)
                .execute();

        List<List<Object>> valuesFrom = response.getValues();

        if (valuesFrom == null || valuesFrom.isEmpty()) {
            LOGGER.info("No data found.");
        } else {

            ValueRange requestBody = new ValueRange()
                    .setValues(valuesFrom);

            try {
                String updateRange = DEFAULT_SHEET_NAME + "!" + "R1C" + newColumn + ":C" + newColumn ;
                service.spreadsheets().values().update(SPREADSHEET_ID, updateRange, requestBody)
                        .setValueInputOption(valueInputOption)
                        .execute();
            } catch (Exception e){
                LOGGER.error("Error during update");
            }
        }
    }

    public void clearColumn(int column) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String originalRange = DEFAULT_SHEET_NAME + "!" + "R1C" + column + ":C" + column ;

        String valueInputOption = "RAW";

        String firstColumnRange = DEFAULT_SHEET_NAME + "!" + "R1C1:C1" ;

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, firstColumnRange)
                .execute();

        List<List<Object>> valuesFrom = response.getValues();
        List<List<Object>> blankValues = valuesFrom;
        for(int i = 0; i < valuesFrom.size(); i++)
            for(int j = 0; j < valuesFrom.get(i).size(); j++)
                blankValues.get(i).set(j, "-");

        ValueRange requestBody = new ValueRange()
                .setValues(blankValues);

        try {
            String updateRange = originalRange;
            service.spreadsheets().values().update(SPREADSHEET_ID, updateRange, requestBody)
                    .setValueInputOption(valueInputOption)
                    .execute();
        } catch (Exception e){
            LOGGER.error("Error during update");
        }
    }

}

