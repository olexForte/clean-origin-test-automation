package automation.api;

import automation.datasources.JSONConverter;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import automation.reporting.ReporterManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.RedirectConfig.redirectConfig;

/**
 * Basic REST client for API testing <br>
 *     based on RestAssured
 */
public class BaseRestClient {

    ReporterManager reporter = ReporterManager.Instance;

    /**
     * Send GET request
     * @param requestURL request URL
     * @param headers map of headers
     * @return response object
     */
    public Response getRequest(String requestURL, Map<String,String> headers) {
        reporter.info("GET URL: " + requestURL);
        if(headers == null)
            headers = new HashMap<>();
        Response response = given()
                .headers(headers)
                .when()
                .contentType(ContentType.JSON)
                .get(requestURL)
                .then()
                .extract()
                .response();

        return response;
    }

    /**
     * Send POST reques
     * @param requestURL request URL
     * @param body request body
     * @param headers map of headers
     * @return response object
     */
    public Response postRequest(String requestURL, String body, Map<String,String> headers) {
        String finalURL = requestURL;
        if(headers == null)
            headers = new HashMap<>();
//        try {
//            finalURL = requestURL.substring(0, requestURL.indexOf("?")) + "?" + URLEncoder.encode(requestURL.substring(requestURL.indexOf("?")+1), StandardCharsets.UTF_8.toString());
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        reporter.info("POST URL: " + finalURL);
        Response response = given()
                .headers(headers)
                .body(body)
                .when()
                .contentType(ContentType.JSON)
                .config(RestAssured.config().redirect(redirectConfig().followRedirects(false)))
                .post(finalURL)
                .then()
                .extract()
                .response();
        return response;
    }

    /**
     * Send POST request
     * @param requestURL request URL
     * @param body request body
     * @param headers map of headers
     * @param type content type
     * @return response object
     */
    public Response postRequest(String requestURL, String body, Map<String,String> headers, ContentType type) {
        if(headers == null)
            headers = new HashMap<>();
        reporter.info("POST URL: " + requestURL);
        Response response = given()
                .headers(headers)
                .body(body)
                .when()
                .contentType(type)
                .post(requestURL)
                .then()
                .extract()
                .response();
        return response;
    }

    /**
     * Send DELETE request
     * @param requestURL request URL
     * @param body request body
     * @param headers map of headers
     * @return response object
     */
    public Response deleteRequest(String requestURL, String body, Map<String,String> headers) {
        if(headers == null)
            headers = new HashMap<>();
        reporter.info("DELETE URL: " + requestURL);
        Response response = given()
                .headers(headers)
                .body(body)
                .when()
                .contentType(ContentType.JSON)
                .delete(requestURL)
                .then()
                .extract()
                .response();
        return response;
    }

    /**
     * Send post request an process redirections
     * @param initialUrl
     * @param targetUrl
     * @param data
     * @param headers
     * @return
     */
    public Response postWithRedirect(String initialUrl, String targetUrl, String data, Map<String,String> headers){

        if(headers == null)
            headers = new HashMap<>();

        //send initial request
        Response initialResponse = null;
        if(initialUrl != null)
            initialResponse= given()
                .get(initialUrl)
                .then()
                .extract()
                .response();

        //send main request
        Response response;
        if(!isJson(data)) {
            response = given().contentType(ContentType.JSON)
                    .headers(headers)
                    .body(data)
                    .cookies(initialResponse !=null ? initialResponse.cookies() : new HashMap<>())
                    .post(targetUrl)
                    .then()
                    .extract()
                    .response();
        } else {
            HashMap<String, String> parameters = JSONConverter.toHashMapFromJsonString(data);
            response = given().contentType(ContentType.URLENC)
                    .headers(headers)
                    .params(parameters != null ? parameters : new HashMap<>())
                    .cookies(initialResponse !=null ? initialResponse.cookies() : new HashMap<>())
                    .post(targetUrl)
                    .then()
                    .extract()
                    .response();
        }

        //redirect to new location
        Response result = given()
                .cookies(response.cookies())
                .get(response.then().extract().header("Location"))
                .then()
                .extract()
                .response();

        return result;
    }

    /**
     * Upload POST request
     * @param requestURL request URL
     * @param file file
     * @param headers map of headers
     * @return response object
     */
    public Response uploadPostRequest(String requestURL, File file, Map<String,String> headers, Map<String,String> parameters) {
        String finalURL = requestURL;
        if(headers == null)
            headers = new HashMap<>();
        if(parameters == null)
            parameters = new HashMap<>();

        reporter.info("POST URL: " + finalURL);
        Response response = given()
                .formParams(parameters)
                .headers(headers)
                .when()
                .multiPart(file)
                .post(finalURL)
                .then()
                .extract()
                .response();
        return response;
    }

    /**
     * check if request data looks like JSON
     * @param data
     * @return
     */
    private boolean isJson(String data) {
        return data.startsWith("{");
    }

}
