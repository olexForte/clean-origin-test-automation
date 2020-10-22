package automation.datasources;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.DriveScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Support of Google drive <br>
 * https://console.developers.google.com/apis/credentials?showWizardSurvey=true&amp;project=auto <br>
 * Based on https://developers.google.com/drive/api/v3/
 */

public class GoogleDriveHelper {
    private static final String APPLICATION_NAME = "Automation";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final String CREDENTIALS_FOLDER = "src/main/resources/credentials/googledrive"; // Directory to store user credentials.

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CLIENT_CREDENTIALS_FILE = "";//credentials/googledrive/googledrive.json";//client_secret.json";

    public static Logger LOGGER = LoggerFactory.getLogger(GoogleDriveHelper.class);

    public static GoogleDriveHelper instance;

    Drive SERVICE;

    public static GoogleDriveHelper getInstance() {
        if (instance == null) {
            LOGGER.info("Create Proxy");
            try {
                instance = new GoogleDriveHelper();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                return null;
            }
        }
        return instance;
    }

    GoogleDriveHelper() throws IOException, GeneralSecurityException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(CREDENTIALS_FOLDER + "/Auto.json"))
                .createScoped(SCOPES);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        SERVICE = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

//    /**
//     * Creates an authorized Credential object.
//     * @param HTTP_TRANSPORT The network HTTP Transport.
//     * @return An authorized Credential object.
//     * @throws IOException If the credentials.json file cannot be found.
//     */
//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        // Load client secrets.
//        InputStream in = new FileInputStream(FileManager.getFileFromMainResources(CLIENT_CREDENTIALS_FILE));
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CLIENT_CREDENTIALS_FILE);
//        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }
//        // Build a new authorized API client service.
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();



    public static void main(String[] args) throws IOException, GeneralSecurityException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(CREDENTIALS_FOLDER + "/Auto.json"))
                .createScoped(SCOPES);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

                Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        deleteAll(service);

        initialSetupForImageComparison(service);

        showList(service);

//        File dir = createGoogleFolder(service, null, "ToReview");
//
//        File newFile = createNewFile(service, "Keywords.html", dir.getId());

//        List<File> googleRootFolders = getGoogleRootFolders(service);
//        for (File folder : googleRootFolders) {
//
//            LOGGER.info("Folder ID: " + folder.getId() + " --- Name: " + folder.getName());
//        }

      // showList(service);

//        List<File> files = getGoogleFilesByName(service, "Actions");
//        if(files.size()>0) {
//            LOGGER.info("WebContentLink: " + files.get(0).getWebContentLink());
//            LOGGER.info("WebViewLink: " + files.get(0).getWebViewLink());
//        }

//        shareFileWith(service, dir.getId(), "oleksandr.diachuk@fortegrp.com");
//        shareFileWith(service, newFile.getId(), "oleksandr.diachuk@fortegrp.com");
//        LOGGER.info(dir.getWebViewLink());
//        LOGGER.info(newFile.getWebViewLink());

//        addFile(service, new File("Actions.html"));


    }

    /**
     * Create Baselined dir and share it, Create toReviewDir and share it
     */
    private static void initialSetupForImageComparison(Drive service) throws IOException {
        File dir = createGoogleFolder(service, null, "VisualTesting");
        shareFileWith(service, dir.getId(),"oleksandr.diachuk@fortegrp.com");
        shareFileWith(service, dir.getId(),"nazar.dovhoshyya@fortegrp.com");
        createGoogleFolder(service, dir.getId(), "Base");
        shareFileWith(service, dir.getId(),"oleksandr.diachuk@fortegrp.com");
        shareFileWith(service, dir.getId(),"nazar.dovhoshyya@fortegrp.com");
        dir = createGoogleFolder(service ,  dir.getId(), "toReview" );
        shareFileWith(service, dir.getId(),"oleksandr.diachuk@fortegrp.com");
        shareFileWith(service, dir.getId(),"nazar.dovhoshyya@fortegrp.com");
    }


    /**
     * Get list of Root Drive folders
     * @param driveService
     * @return
     * @throws IOException exception
     */
    public static final List<File> getGoogleRootFolders(Drive driveService) throws IOException {
        return getGoogleSubFolders(driveService, null, null);
    }

    /**
     * Get list of folders (by name and/or parent folder)
     * @param driveService
     * @param nameOfFolder
     * @param googleFolderIdParent
     * @return
     * @throws IOException exception
     */
    public static final List<File> getGoogleSubFolders(Drive driveService, String nameOfFolder, String googleFolderIdParent) throws IOException {

        LOGGER.info("Show folders: " + nameOfFolder + " " + googleFolderIdParent);

        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String nameOfSubfolderInQuery = "";
        if(nameOfFolder != null)
            nameOfSubfolderInQuery = " and name contains '" + nameOfFolder + "'" ;

        String query = null;
        if (googleFolderIdParent == null) {
            query = " mimeType = 'application/vnd.google-apps.folder' " //
                    + " and 'root' in parents" + nameOfSubfolderInQuery;
        } else {
            query = " mimeType = 'application/vnd.google-apps.folder' " //
                    + " and '" + googleFolderIdParent + "' in parents" + nameOfSubfolderInQuery;
        }

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    .setFields("nextPageToken, files(id, name, webViewLink)")//
                    .setPageToken(pageToken).execute();
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        //
        return list;
    }

    /**
     * Show ALL files and Folders in Drive
     * @param service
     * @throws IOException exception
     */
    private static void showList(Drive service) throws IOException {
        LOGGER.info("Show all files");
        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, webViewLink )")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            LOGGER.info("No files found.");
        } else {
            LOGGER.info("Files:");
            for (File file : files){
                LOGGER.info(file.getWebViewLink());
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

    /**
     * Create dir on Drive
     * @param driveService
     * @param folderIdParent
     * @param folderName
     * @return
     * @throws IOException exception
     */
    public static final File createGoogleFolder(Drive driveService, String folderIdParent, String folderName) throws IOException {
        LOGGER.info("Folder creation: " + folderName);
        File fileMetadata = new File();

        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        if (folderIdParent != null) {
            List<String> parents = Arrays.asList(folderIdParent);
            fileMetadata.setParents(parents);
        }

        // Create a Folder.
        // Returns File object with id & name fields will be assigned values
        File file = driveService.files().create(fileMetadata).setFields("id, name, webViewLink").execute();

        LOGGER.info("Folder created: " + folderName);
        return file;
    }


    /**
     * Get files by Name and Dir (https://developers.google.com/drive/api/v3/search-files)
     * @param driveService
     * @param fileNameLike
     * @param dir
     * @return
     * @throws IOException exception
     */
    public static final List<File> getGoogleFilesByNameAndDir(Drive driveService, String fileNameLike, String dir) throws IOException {

        String[] folders = dir.split(java.io.File.separator);
        String parent = null;
        for(int i = 0; i < folders.length; i++){
            List<File> foundFolders = getGoogleSubFolders(driveService, folders[i], parent);
            parent = foundFolders.get(0).getId();
        }

        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String query = " name contains '" + fileNameLike + "' " //
                + " and mimeType != 'application/vnd.google-apps.folder' and '" + parent + "' in parents" ;

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    // Fields will be assigned values: id, name, createdTime, mimeType
                    .setFields("nextPageToken, files(id, name, createdTime, mimeType, webContentLink, webViewLink, parents)")//
                    .setPageToken(pageToken).execute();
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        //
        return list;
    }

    /**
     * Get liost of Drive Files by name
     * @param driveService
     * @param fileNameLike
     * @return
     * @throws IOException exception
     */
    public static final List<File> getGoogleFilesByName(Drive driveService, String fileNameLike) throws IOException {

        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String query = " name contains '" + fileNameLike + "' " //
                + " and mimeType != 'application/vnd.google-apps.folder' ";

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    // Fields will be assigned values: id, name, createdTime, mimeType
                    .setFields("nextPageToken, files(id, name, createdTime, mimeType, webContentLink, webViewLink, parents)")//
                    .setPageToken(pageToken).execute();
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        //
        return list;
    }

    private static File _createGoogleFile(Drive driveService, String googleFolderIdParent, String contentType, //
                                          String customFileName, AbstractInputStreamContent uploadStreamContent) throws IOException {

        File fileMetadata = new File();
        fileMetadata.setName(customFileName);

        List<String> parents = Arrays.asList(googleFolderIdParent);
        fileMetadata.setParents(parents);

        File file = driveService.files().create(fileMetadata, uploadStreamContent)
                .setFields("id, webContentLink, webViewLink, parents").execute();

        return file;
    }

    // Create Google File from byte[]
    public static File createGoogleFile(Drive driveService, String googleFolderIdParent, String contentType, //
                                        String customFileName, byte[] uploadData) throws IOException {
        //
        AbstractInputStreamContent uploadStreamContent = new ByteArrayContent(contentType, uploadData);
        //
        return _createGoogleFile(driveService, googleFolderIdParent, contentType, customFileName, uploadStreamContent);
    }

    // Create Google File from java.io.File
    public static File createGoogleFile(Drive driveService, String googleFolderIdParent, String contentType, //
                                        String customFileName, java.io.File uploadFile) throws IOException {

        //
        AbstractInputStreamContent uploadStreamContent = new FileContent(contentType, uploadFile);
        //
        return _createGoogleFile(driveService, googleFolderIdParent, contentType, customFileName, uploadStreamContent);
    }

    // Create Google File from InputStream
    public static File createGoogleFile(Drive driveService, String googleFolderIdParent, String contentType, //
                                        String customFileName, InputStream inputStream) throws IOException {

        //
        AbstractInputStreamContent uploadStreamContent = new InputStreamContent(contentType, inputStream);
        //
        return _createGoogleFile(driveService, googleFolderIdParent, contentType, customFileName, uploadStreamContent);
    }

    /**
     * Create Drive file from Local file
     * @param driveService
     * @param localFilePath full path to local file
     * @param googleFolderID id of parent folder
     * @return file object
     * @throws IOException  possible exception
     */
    public static File createNewFile (Drive driveService, String localFilePath, String googleFolderID) throws IOException {

        java.io.File uploadFile = new java.io.File(localFilePath);

        // Create Google File:

        File googleFile = createGoogleFile( driveService, googleFolderID, "image/png", FileManager.getFileNameFromPath(localFilePath), uploadFile);

        LOGGER.info("Created Google file!");
        LOGGER.info("WebContentLink: " + googleFile.getWebContentLink() );
        LOGGER.info("WebViewLink: " + googleFile.getWebViewLink() );

        LOGGER.info("Done!");
        return googleFile;
    }



    /**
     * Delete file By ID
     * @param service
     * @param id
     * @throws IOException possible exception
     */
    public static void deleteFile(Drive service, String id) throws IOException {
        service.files().delete(id).execute();
    }

    /**
     * Delete ALL files from Drive USE WITH CAUTION!!
     * @param service
     * @throws IOException possible exception
     */
    public static void deleteAll(Drive service) throws IOException {
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, webViewLink )")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            LOGGER.info("No files found.");
        } else {
            LOGGER.info("Deleting Files:");
            for (File file : files){
                deleteFile(service, file.getId());
            }
        }
    }

    /**
     * Create permision for file sharing
     * @param driveService
     * @param googleFileId
     * @param googleEmail
     * @return
     * @throws IOException possible exception
     */
    public static Permission createPermissionForEmail(Drive driveService, String googleFileId, String googleEmail) throws IOException {
        // All values: user - group - domain - anyone
        String permissionType = "user"; // Valid: user, group
        // organizer - owner - writer - commenter - reader
        String permissionRole = "writer";

        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        newPermission.setEmailAddress(googleEmail);

        return driveService.permissions().create(googleFileId, newPermission).execute();
    }

    // Public a Google File/Folder.
//    public static Permission createPublicPermission(Drive driveService, String googleFileId) throws IOException {
//        // All values: user - group - domain - anyone
//        String permissionType = "anyone";
//        // All values: organizer - owner - writer - commenter - reader
//        String permissionRole = "reader";
//
//        Permission newPermission = new Permission();
//        newPermission.setType(permissionType);
//        newPermission.setRole(permissionRole);
//
//        return driveService.permissions().create(googleFileId, newPermission).execute();
//    }

    /**
     * Share file with specified Email
     * @param driveService
     * @param googleFileId
     * @param googleEmail
     * @throws IOException possible exception
     */
    public static void shareFileWith(Drive driveService, String googleFileId, String googleEmail) throws IOException {

        // Share for a User
        createPermissionForEmail(driveService, googleFileId, googleEmail);

        // Share for everyone
        //createPublicPermission( driveService, googleFileId);

        LOGGER.info("File shared: " + googleFileId + " " + googleEmail);
    }

    public static String getFolderPathForFileID(String folderID) {
        return "https://drive.google.com/drive/folders/" + folderID;
    }

    /**
     * Share files with list of Emails
     * @param fileID
     * @param listOfEmails list of email separted by ;
     */
    public void shareFileWithListOfUsers(String fileID, String[] listOfEmails) {
        for(String email : listOfEmails){
            try {
                shareFileWith(SERVICE, fileID, email);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create full directory if it does not exist
     * @param dir path to create on Drive
     */
    public String createFullDir(String dir) throws IOException {
        String[] folders = dir.split(java.io.File.separator);
        String directory = null;
        for(int i = 0; i < folders.length; i++){
            List<File> foundFolders = getGoogleSubFolders(SERVICE, folders[i], directory);
            if(foundFolders.size() > 0)
                directory = foundFolders.get(0).getId();
            else
                directory = createGoogleFolder(SERVICE, directory, folders[i]).getId();
        }
        return directory;
    }

    /**
     * Add local file to Drive
     * @param dirOnDrive dir on Drive
     * @param localFileLocation path to local file
     */
    public File copyLocalFileToDrive(String dirOnDrive,  String localFileLocation) throws IOException {
            return createNewFile(SERVICE, localFileLocation, dirOnDrive);
    }

    /**
     * Download file from Drive to Local filesystem
     * @param discFileLocation
     * @param localFileLocation
     * @return
     */
    public boolean downloadFile(String discFileLocation, String localFileLocation) {

        //find file
        List<File> files = null;
        try {
            files = getGoogleFilesByNameAndDir(SERVICE, FileManager.getFileNameFromPath(discFileLocation), FileManager.getParentFolderNameFromPath(discFileLocation));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //get stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            SERVICE.files().get(files.get(0).getId())
                    .executeMediaAndDownloadTo(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //save file
        try(OutputStream outputFileStream = new FileOutputStream(localFileLocation)) {
            outputStream.writeTo(outputFileStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
