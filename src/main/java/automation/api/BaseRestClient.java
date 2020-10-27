package automation.api;

import automation.datasources.JSONConverter;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import automation.reporting.ReporterManager;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Response getRequest(String requestURL, Object headers, Object cookies) {
        reporter.info("GET URL: " + requestURL);
        if(headers == null)
            headers = new HashMap<>();
        if(cookies == null)
            cookies = new HashMap<>();

        String creds = requestURL.replaceAll("(http|https)://(.*?:.*?)@.*", "$2");

        Response response;
        RequestSpecification responseSpecification;
        if(!creds.equals(requestURL)) // credentials were found
            responseSpecification = given().auth().basic(creds.split(":")[0], creds.split(":")[1]);
        else
            responseSpecification = given();

        if(headers instanceof Headers)
            responseSpecification = responseSpecification.headers((Headers)headers);
        else
            responseSpecification = responseSpecification.headers((HashMap<String,String>)headers);

        if(cookies instanceof Cookies)
            responseSpecification = responseSpecification.cookies((Cookies)cookies);
        else
            responseSpecification = responseSpecification.cookies((HashMap<String,String>)cookies);

        response = responseSpecification.when()
                    .contentType(ContentType.JSON)
                    .get(requestURL)
                    .then()
                    .extract()
                    .response();

            return response;
    }

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
        String creds = requestURL.replaceAll("(http|https)://(.*?:.*?)@.*", "$2");
        if(!creds.equals(requestURL)){ // credentials were found
            Response response = given()
                    .auth().basic(creds.split(":")[0], creds.split(":")[1])
                    .headers(headers)
                    .when()
                    .contentType(ContentType.JSON)
                    .get(requestURL)
                    .then()
                    .extract()
                    .response();

            return response;
        } else {
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
    }

    /**
     * Send GET request
     * @param requestURL request URL
     * @param headers map of headers
     * @return response object
     */
    public Response getRequest(String requestURL, Headers headers) {
        reporter.info("GET URL: " + requestURL);

        String creds = requestURL.replaceAll("(http|https)://(.*?:.*?)@.*", "$2");
        if(!creds.equals(requestURL)){ // credentials were found
            Response response = given()
                    .auth().basic(creds.split(":")[0], creds.split(":")[1])
                    .headers(headers)
                    .when()
                    .contentType(ContentType.JSON)
                    .get(requestURL)
                    .then()
                    .extract()
                    .response();

            return response;
        } else {
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
        if (headers == null)
            headers = new HashMap<>();

        if (isJson(body)){ //json{

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
        } else { // parameters
            reporter.info("POST URL (with parameters): " + finalURL);
            Map<String,String> parameters = new HashMap<>();
            Stream<String> s = Stream.of(body.split(";"));
            parameters = s.collect(Collectors.toMap(it -> it.split("=")[0], it -> it.split("=")[1]));
            Response response = given()
                    .headers(headers)
                    .params(parameters)
                    .when()
                    .contentType(ContentType.JSON)
                    .config(RestAssured.config().redirect(redirectConfig().followRedirects(false)))
                    .post(finalURL)
                    .then()
                    .extract()
                    .response();
            return response;
        }

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
