package automation.web.visual;

import automation.configuration.ProjectConfiguration;
import automation.configuration.SessionManager;
import automation.datasources.FileManager;
import automation.datasources.GoogleDriveHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

//in progress
public class GoogleDriveImageProvider {

    static String BASELINED_DIR = "VisualTesting/Base";
    static String TO_REVIEW_DIR = "VisualTesting/toReview";

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveImageProvider.class);

    /**
     * Get Baselined image (local path for comparison)
     * @param target
     * @return
     */
    public static String getBaselinedImage(String target) {
        String fileLocation  = BASELINED_DIR + File.separator + ProjectConfiguration.CONFIG_FILE + File.separator + target;
        String localFileLocation  = FileManager.OUTPUT_DIR + File.separator + "BASE_" + target;

        if(!GoogleDriveHelper.getInstance().downloadFile(fileLocation, localFileLocation)) {
            logger.error("Baselined image was not found: " + fileLocation);
            return null;
        }
        return localFileLocation;
    }

    /**
     * Add image to Review dir
     * @param currentImage
     */
    public static String addImageToReview(String currentImage) {
        String parentDirOnDrive = "";
        String baselinedDirOnDrive = "";

        String dir = TO_REVIEW_DIR + File.separator + SessionManager.getSessionID() + File.separator + ProjectConfiguration.CONFIG_FILE;
        String baselineddir = BASELINED_DIR + File.separator + ProjectConfiguration.CONFIG_FILE;

        try{
            baselinedDirOnDrive = GoogleDriveHelper.getInstance().createFullDir(baselineddir);
            parentDirOnDrive = GoogleDriveHelper.getInstance().createFullDir(dir);
        } catch (Exception e){
            logger.error("Dir creation failed: " + e.getMessage());
        }

        String fileID = "";
        try{
            fileID = GoogleDriveHelper.getInstance().copyLocalFileToDrive(parentDirOnDrive, currentImage).getId();
        } catch (Exception e){
            logger.error("File copying failed: " + e.getMessage());
        }

        String[] listOfEmails = ProjectConfiguration.getConfigProperty("DriveListOfUsers").split(";");
        try{
            GoogleDriveHelper.getInstance().shareFileWithListOfUsers(baselinedDirOnDrive, listOfEmails);
            GoogleDriveHelper.getInstance().shareFileWithListOfUsers(fileID, listOfEmails);
        } catch (Exception e){
            logger.error("File sharing failed: " + e.getMessage());
        }

        return GoogleDriveHelper.getFolderPathForFileID(parentDirOnDrive); //TO_REVIEW_DIR + File.separator + FileManager.getFileNameFromPath(currentImage);
    }
}
