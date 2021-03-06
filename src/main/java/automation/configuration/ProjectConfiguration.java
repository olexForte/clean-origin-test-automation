package automation.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import automation.reporting.ReporterManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project configuration and
 * Interaction with properties
 */
public class ProjectConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ProjectConfiguration.class);

    static public String CONFIG_FILE = System.getProperty("config");
    static String PROPERTIES_FILE = "src/test/automation/resources/config/" + (( CONFIG_FILE == null ) ? "default" : CONFIG_FILE) + ".properties";
    static private Properties localProps = loadProperties();

    static public ThreadLocal<Properties> threadProperties = new ThreadLocal<Properties>();

    public static boolean isUsingEtalon = isUsingEtalon();

    private static boolean isUsingEtalon(){
        return  getConfigProperty("EtalonLocatorsDir" ) != null;
    }

    public static boolean isPerformanceProfilingRequired = isPropertySet("ProfilingRequired");

    static Pattern globalParameterInStringPattern =  Pattern.compile("\\$\\{(.*?)}"); // ${ ... }

    /**
     * Load main config file
     * @return
     */
    public static Properties loadProperties(){
        Properties result = new Properties();

        try {

            //open file
            File file = new File(PROPERTIES_FILE);
            //open input stream to read file
            FileInputStream fileInput = new FileInputStream(PROPERTIES_FILE); //ProjectConfiguration.class.getResourceAsStream(PROPERTIES_FILE));
            //create Properties object
            result = new Properties();
            //load properties from file
            result.load(fileInput);
            //close file
            fileInput.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ReporterManager.Instance.fail("Config was not found");
        } catch (IOException e) {
            e.printStackTrace();
            ReporterManager.Instance.fail("Config was not opened");
        } catch (Exception e){
            e.printStackTrace();
            ReporterManager.Instance.fail("Field was not found: " + PROPERTIES_FILE);
        }
        return result;
    }

    /**
     * Check if Property was specified
     * @param property
     * @return
     */
    public static boolean isPropertySet(String property){
        String valueFromProperties = getConfigProperty(property);
        if(valueFromProperties != null)
            return Boolean.parseBoolean(valueFromProperties);
        return false;
    }

    /**
     * Get Configuration property (from file/command line or local thread)
     * @param fieldName
     * @return
     */
    public static String getConfigProperty(String fieldName){
        String result   = null;

        if(System.getProperty(fieldName) != null)
            return System.getProperty(fieldName);

        if(localProps.getProperty(fieldName) != null)
            return localProps.getProperty(fieldName);

        if(threadProperties.get() != null && threadProperties.get().getProperty(fieldName) != null)
            return threadProperties.get().getProperty(fieldName);

        return result;
    }

    /**
     * Set config property
     * @param fieldName
     * @param value
     */
    public static void setConfigProperty(String fieldName, String value) {
        localProps.setProperty(fieldName, value);
    }

    /**
     * Set config property for Local thread
     * @param fieldName
     * @param value
     */
    public static void setLocalThreadConfigProperty(String fieldName, String value) {
        threadProperties.get().setProperty(fieldName, value);
    }

    /**
     * Replace global parameters in line
     * @param line
     * @return line with replaced parameters
     */
    public static String replaceGlobalVariables(String line) {

        Matcher m = globalParameterInStringPattern.matcher(line);
        while(m.find()){
            if(getConfigProperty(m.group(1)) != null)
                line = line.replace("${"+m.group(1)+"}", getConfigProperty(m.group(1)));
            else
                logger.warn("Parameter was not found: " + m.group(1));
        }
        return line;
    }
}

