package automation.keyword.complex;

import automation.annotations.KeywordRegexp;
import automation.entities.application.RingFilter;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

    /**
     *
     */
    public class GetItemsFromAdminByRequestKeyword extends AbstractKeyword {

        @KeywordRegexp("Get list of items from Admin by request 'requestData' to 'saved.DATA_FROM_ADMIN'")
        static String LABEL = "get list of items from admin by request";

        @Override
        public AbstractKeyword generateFromLine(String line) {
            if (prepareLine(line).toLowerCase().matches(LABEL.toLowerCase() + ".*")) {
                GetItemsFromAdminByRequestKeyword result = (GetItemsFromAdminByRequestKeyword) super.generateFromLine(line);

                Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
                Matcher matcher = p.matcher(line);
                matcher.find();
                result.data = line.substring(matcher.start() + 1, matcher.end() - 1);
                matcher.find();// number of pages
                result.target = line.substring(matcher.start() + 1, matcher.end() - 1);
                return result;
            }
            return null;
        }

        @Override
        public boolean execute(TestStepsExecutor executor) throws Exception {
            HashMap<String, String> allFields = executor.testDataRepository.getComplexData(data);
            Map<String,String> authorizedCookies = executor.api.loginToAdminAndGetCookies(executor.locatorsRepository.getTarget("apiEndpoints.ADMIN_URL"));
            String jsonDataFromAdmin = getItemsFromAPI(executor, authorizedCookies, allFields);
            executor.testDataRepository.setData(target, jsonDataFromAdmin);
            return true;
        }

        private String getItemsFromAPI(TestStepsExecutor executor, Map<String,String> authorizedCookies, HashMap<String, String>  filter) throws Exception {

            String parameters = getParametersString(filter);
            String url = executor.locatorsRepository.getTarget("apiEndpoints.ADMIN_PRODUCT_SEARCH_URL:" + parameters);

            Response response = executor.api.getRequest( url, null, authorizedCookies);
            if(response == null || response.asString().equals("")) {
                LOGGER.error("Incorrect response from URL: " + url);
                return null;
            }
            String allData = response.asString().replace("\n","").replaceAll(".*\"items\":\\[(.*?)\\]},\"update_url\".*", "$1");
            if(allData.equals("")) {
                LOGGER.error("Item was not found for filter: " + filter);
                return null;
            }
            return allData;
        }

        private String getParametersString(HashMap<String, String> filter) {
            // ?namespace=product_listing&search=&filters[placeholder]=true&filters[entity_id][from]=$PARAMETER1&filters[entity_id][to]=$PARAMETER1&paging[pageSize]=20&paging[current]=1&sorting[field]=created_at&sorting[direction]=desc&isAjax=true
            //                        filters[entity_id][from]
            //                        filters[entity_id][to]
            //filters[attribute_set_id]=9 кштпі 10
            // &filters[qty][from]=1
            // &filters[qty][to]=1
            // &filters[created_at][from]=11%2F10%2F2020
            // &filters[created_at][to]=11%2F10%2F2020
            // &filters[status]=1
            // &search=

            String allParameters = "";
            for (Map.Entry<String, String> entry : filter.entrySet()) {
                 allParameters = allParameters + "&" + entry.getKey() + "=" + entry.getValue();
            }
            String parameters = "?namespace=product_listing&filters[placeholder]=true" + allParameters + "&paging[pageSize]=100&paging[current]=1&sorting[field]=created_at&sorting[direction]=desc&isAjax=true";
            return parameters;
        }

    }