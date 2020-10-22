package automation.web.visual;

import automation.configuration.ProjectConfiguration;
import automation.configuration.SessionManager;
import automation.datasources.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

//in progress
public class FileImageProvider {

    static String BASELINED_DIR = "baselined";
    static String CURRENT_DIR = "toReview";

    private static final Logger logger = LoggerFactory.getLogger(FileImageProvider.class);

    /**
     * Get Baselined image
     * @param target
     * @return
     */
    public static String getBaselinedImage(String target) {
        String fileLocation  = BASELINED_DIR + File.separator + ProjectConfiguration.CONFIG_FILE + File.separator + target;
        if(!FileManager.doesExist(fileLocation)) {
            logger.error("Baselined image was not found: " + BASELINED_DIR + File.separator + ProjectConfiguration.CONFIG_FILE + File.separator + target);
            return null;
        }
        return fileLocation;
    }

    /**
     * Add image to Review dir
     * @param currentImage
     */
    public static void addImageToReview(String currentImage) {
        String dir = "."+ File.separator+CURRENT_DIR + File.separator + SessionManager.getSessionID() + File.separator + ProjectConfiguration.CONFIG_FILE;
        try{
            FileManager.createDir(dir);
        } catch (Exception e){
            logger.error("Dir creation failed: " + e.getMessage());
        }
        String fileLocation  = dir + File.separator + FileManager.getFileNameFromPath(currentImage);
        try{
            FileManager.copyFile(currentImage, fileLocation);
        } catch (Exception e){
            logger.error("File copying failed: " + e.getMessage());
        }
    }
}
