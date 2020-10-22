package automation.mailtrap;

import automation.configuration.ProjectConfiguration;
import automation.datasources.FileManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.restassured.RestAssured.given;

public class MailTrapClient {

    public static final String LAST_EMAIL_CHECK_TIME = "LAST_EMAIL_CHECK_TIME";

    private static final Logger LOGGER = LoggerFactory.getLogger(MailTrapClient.class);

    String BASIC_MAIL_TRAP = ProjectConfiguration.getConfigProperty("MailTrapURL");

    int maxNumOfAttempts = ProjectConfiguration.getConfigProperty("EmailTimeout") == null ?
            Integer.parseInt(ProjectConfiguration.getConfigProperty("DefaultTimeoutInSeconds")) :
            Integer.parseInt(ProjectConfiguration.getConfigProperty("EmailTimeout"));

    /**
     * Get Email ID for email
     * @param inboxURL
     * @param subject
     * @param email
     * @return
     */
    public String checkForEmail(String inboxURL, String subject, String email){
        return checkForEmail(inboxURL, subject, email, false);
    }

    public String checkForEmail(String inboxURL, String subject, String email, boolean checkEmailTimeStamp) {
        List<String> allSubjects = null;
        List<String> allEmails = null;
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Api-Token", ProjectConfiguration.getConfigProperty("MailDropToken"));
        Response response;

        LOGGER.info("Waiting for email for " + email + " with subject \n" + subject);

        LocalDateTime lastEmailTime = getLastEmailTimestamp(headers, inboxURL);

        for(int i=0; i < maxNumOfAttempts; i++) {
            LOGGER.info("Sending API request: " + inboxURL);
            response = given()
                    .headers(headers)
                    .when()
                    .contentType(ContentType.JSON)
                    .get(inboxURL)
                    .then()
                    .extract()
                    .response();

            allSubjects = response.jsonPath().getList("subject", String.class);
            allEmails = response.jsonPath().getList("to_email", String.class);

            int j = 0;
            for(String subjectString : allSubjects) {
                String currentEmail = allEmails.get(j);
                if ((subject == null || subjectString.equals(subject)) &&
                        (currentEmail == null || currentEmail.equals(email))) {
                    LOGGER.info("ID of email was found in email: " + j);
                    LocalDateTime curEmailTime = LocalDateTime.parse(String.valueOf(response.jsonPath().getList("updated_at").get(j)), DateTimeFormatter.ISO_DATE_TIME);
                    if(checkEmailTimeStamp && (lastEmailTime.isAfter(curEmailTime) || lastEmailTime.equals(curEmailTime)))
                       break;
                    return response.jsonPath().getList("id", String.class).get(j);
                }
                j++;
            }

            LOGGER.info("No emails found");

            //sleep
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.warn("No emails were found for " + email + " with subject: \n" + subject );
        return null;
    }

    /**
     * Get field from MailTrap message body.txt
     * @param fieldRegexp regexp that describes field
     * @param emailURL API endpoint with specified Inbox and Message
     * @return
     */
    public String getEmailBodyValue(String fieldRegexp, String emailURL){
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Api-Token", ProjectConfiguration.getConfigProperty("MailDropToken"));
        Response response;
        response = given()
                .headers(headers)
                .when()
                .contentType(ContentType.JSON)
                .get(emailURL)
                .then()
                .extract()
                .response();
        String body = response.asString();
        //String result = Arrays.stream(body.split("\n")).filter(l->l.matches(fieldRegexp)).findFirst().get().replaceAll(fieldRegexp, "$1").trim();
        String result = body.replace("\n", " ").replaceAll(fieldRegexp, "$1").trim();
        LOGGER.info("Value from email: " + result);
        return result;
    }

    public String getEmailBody(String path){
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Api-Token", ProjectConfiguration.getConfigProperty("MailDropToken"));
        Response response;
            response = given()
                    .headers(headers)
                    .when()
                    .contentType(ContentType.JSON)
                    .get(path)
                    .then()
                    .extract()
                    .response();

        return response.asString();
    }

    /**
     * Download attachemnt for Email
     * @param emailURL
     * @return
     */
    public String downloadAttachments(String emailURL) {

        ArrayList<String> files = new ArrayList<>();
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Api-Token", ProjectConfiguration.getConfigProperty("MailDropToken"));

        Response attachmentsResponse = given()
                .headers(headers)
                .when()
                .contentType(ContentType.JSON)
                .get(emailURL)
                .then()
                .extract().
                response();

        for(HashMap<String,String> attachmentItem : attachmentsResponse.jsonPath().getList(".", HashMap.class)){

            InputStream response;
            response = given()
                    .headers(headers)
                    .when()
                    .contentType(ContentType.JSON)
                    .get(BASIC_MAIL_TRAP + attachmentItem.get("download_path"))
                    .then()
                    .extract().asInputStream();

            File attachmentFile = new File(FileManager.OUTPUT_DIR + File.separator + attachmentItem.get("filename"));

            FileWriter writer;
            BufferedReader br;
            try {
                writer = new FileWriter(attachmentFile);
                br = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8));
                String line = "";
                while((line = br.readLine()) != null){
                    writer.append(line+"\n");
                }
                writer.close();
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            files.add(attachmentItem.get("filename"));
        }
        return files.get(0); //TODO TMP solution
    }

    //TODO TMP solution

    /**
     * Get time of last email in Inbox
     * @param headers
     * @param inboxURL
     * @return
     */
    public LocalDateTime getLastEmailTimestamp(HashMap<String, String> headers, String inboxURL) {
        Response response = given()
                .headers(headers)
                .when()
                .contentType(ContentType.JSON)
                .get(inboxURL)
                .then()
                .extract()
                .response();
        return LocalDateTime.parse(String.valueOf(response.jsonPath().getList("updated_at").get(0)), DateTimeFormatter.ISO_DATE_TIME);
    }
}
