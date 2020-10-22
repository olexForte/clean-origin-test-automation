package automation.keyword.validation;

import automation.annotations.KeywordRegexp;
import automation.datasources.JSONConverter;
import automation.execution.TestStepsExecutor;
import automation.execution.TestsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate list of maps map1 contains/equals/etc. map2
 * Optional: ignoring 'listOfKeysToIgnore'
 * Optional: by index 'index'
 * <b>Example:</b>
 *    Validate list of maps 'saved.map' equals 'saved.result';
 *    Validate list of maps 'saved.EMPLOYEES_MAP_FROM_REQUEST' contains 'saved.DATA_FROM_FILE' by index 0;
 *    Validate list of maps 'saved.totalAdminRecordsAfterAdding' contains 'subscriptionData.NEW_INVOICE_DATA_FOR_API' ignoring 'scheduled_at,paid_at_formatted,scheduled_at_formatted' ;
 */
public class ValidateListOfMapsKeyword extends AbstractKeyword {

    public enum ActionType {
        EQUALS(" equals "),
        CONTAINS(" contains "),
        SIMILAR(" similar "),
        NOT_CONTAINS(" does not contain ");

        String marker;
        ActionType(String marker){
            this.marker = marker;
        }

    }

    ActionType action = ActionType.EQUALS;

    @KeywordRegexp("Validate list of maps 'map1' [equals|contains|does not contain|similar] 'map2' [ignoring 'listOfKeysToIgnore'][by index 'index'];")
    static String LABEL = "validate list of maps";

    String listOfKeysToIgnore = "";
    int byIndex = -1;

    String IGNORING_FIELDS_MARKER = "ignoring";
    String BY_INDEX_MARKER = "by index";

    @Override
    public AbstractKeyword generateFromLine(String line) {

        isOptional = isStepOptional(line);
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ValidateListOfMapsKeyword result = (ValidateListOfMapsKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            if(line.contains(IGNORING_FIELDS_MARKER))
                result.listOfKeysToIgnore = line.replaceFirst(".*" + IGNORING_FIELDS_MARKER + " [\"'](.*?)[\"'].*","$1");

            if(line.contains(BY_INDEX_MARKER))
                result.byIndex = Integer.valueOf(line.replaceFirst(".*" + BY_INDEX_MARKER + " (\\d+).*","$1"));

            for(ActionType act : ActionType.values())
                if(line.contains(act.marker))
                    result.action = act;

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        boolean result = false;
        //expected
        ArrayList<HashMap<String,String>> map1 = getListAsHashMaps(executor, data);
        //actual
        ArrayList<HashMap<String,String>> map2 = getListAsHashMaps(executor, target);

        int indexOfItem = -1;
        int indexOfItemActual = -1;
        switch (action) {
            case EQUALS:
                result = true;
                for (HashMap<String,String> actualItem: map1) {
                    indexOfItem++;
                    if(byIndex != -1 && indexOfItem != byIndex)
                        continue;
                    boolean wasFound = false; // actual item was found
                    for (HashMap<String,String> expectedItem: map2){
                        boolean valueWasFound = true;
                        for (String key: actualItem.keySet()) {
                            if(listOfKeysToIgnore.contains(key))
                                continue;
                            if(expectedItem.containsKey(key)){
                                if(!actualItem.get(key).equals(expectedItem.get(key))){
                                    valueWasFound = false; // no value in current element
                                    break;
                                }
                            } else {
                                valueWasFound = false;  // no key in current element
                                break;
                            }
                        }
                        if(valueWasFound){ // item was already found in list of expected?
                            wasFound = true;
                           break;
                        }
                    }
                    if(!wasFound){
                        result = false;
                        break;
                    }
                }

                if (result) {
                    executor.reporter.pass("Lists are equal: \n Actual: " + map1 + "\nExpected: " + map2);
                } else {
                    executor.reporter.fail("Lists are not equal: \n Actual: " + map1 + "\nExpected: " + map2);
                    return false;
                }
                break;
            case CONTAINS:
                result = true;
                for (HashMap<String,String> actualItem: map2) {
                    indexOfItem++;
                    LOGGER.info("Map 2 - " + indexOfItem + " processing");
                    if(byIndex != -1 && indexOfItem != byIndex) {
                        LOGGER.info("Skip line: " + indexOfItem);
                        continue;
                    }
                    boolean wasFound = false; // actual item was found
                    for (HashMap<String,String> expectedItem: map1){
                        indexOfItemActual++;
                        LOGGER.info("Map 1 - " + indexOfItemActual + " processing");
                        boolean valueWasFound = true; //
                        for (String key: actualItem.keySet()) {
                            if(listOfKeysToIgnore.contains(key)) {
                                LOGGER.info("Skip key: " + key);
                                continue;
                            }
                            if(expectedItem.containsKey(key)){
                                String value1 = normalizeValue(String.valueOf(actualItem.get(key)));
                                String value2 = normalizeValue(String.valueOf(expectedItem.get(key)));
                                if(! value1.equals(value2)){
                                    valueWasFound = false; // no value in current element
                                    LOGGER.info("Values are not equal: " + key + "\nActual: " + value1 + "\nExpected" + value2);
                                    break;
                                }
                            } else {
                                //valueWasFound = false;  // no key in current element
                                LOGGER.info("No Key found: " + key);
                                //break;
                            }

                        }
                        if(valueWasFound){ // item was already found in list of expected?
                            wasFound = true;
                            break;
                        }
                    }
                    if(!wasFound){
                        result = false;
                        break;
                    }
                }

                if (result) {
                    executor.reporter.pass("List1 contains List2");
                } else {
                    executor.reporter.fail("List1 does not contain List2");
                    return false;
                }
                break;
            case SIMILAR:

                break;
            case NOT_CONTAINS:
                result = true;
                for (HashMap<String,String> actualItem: map2) {
                    LOGGER.info("Map 2 - 1 processing");
                    indexOfItem++;
                    if(byIndex != -1 && indexOfItem != byIndex)
                        continue;
                    boolean wasFound = false; // actual item was found
                    for (HashMap<String,String> expectedItem: map1){
                        LOGGER.info("Map 1 - 1 processing");
                        boolean valueWasFound = true; //
                        for (String key: actualItem.keySet()) {
                            if(listOfKeysToIgnore.contains(key)) {
                                LOGGER.info("Skip key: " + key);
                                continue;
                            }
                            if(expectedItem.containsKey(key)){
                                String value1 = normalizeValue(String.valueOf(actualItem.get(key)));
                                String value2 = normalizeValue(String.valueOf(expectedItem.get(key)));
                                if(! value1.equals(value2)){
                                    valueWasFound = false; // no value in current element
                                    LOGGER.info("Values are not equal: " + key + "\nActual: " + value1 + "\nExpected" + value2);
                                    break;
                                }
                            } else {
                                valueWasFound = false;  // no key in current element
                                LOGGER.info("No Key found: " + key);
                                break;
                            }

                        }
                        if(valueWasFound){ // item was already found in list of expected?
                            wasFound = true;
                            break;
                        }
                    }
                    if(!wasFound){
                        result = false;
                        break;
                    }
                }

                if (result) {
                    executor.reporter.fail("List1 contains List2");
                    return false;
                } else {
                    executor.reporter.pass("List1 does not contain List2");
                }
                break;
        }
        return true;
    }

    private String normalizeValue(String value) {
        //check for null
        String result = value.equals("null") ? "" : value;
        result = cleanValue(result);
        if(looksLikePrice(result))
            result = getFloatValue(result);
        return result.replace("--",""); //TODO make list of replacements
    }

    private ArrayList<HashMap<String, String>> getListAsHashMaps(TestStepsExecutor executor, String target) {
        try {
            return  (ArrayList<HashMap<String, String>>) executor.testDataRepository.getTestDataObject(target);

        } catch (Exception e) {
            try {
                return  (ArrayList<HashMap<String, String>>) JSONConverter.toHashMapList((String)executor.testDataRepository.getTestDataObject(target));
            } catch (Exception e1) {
                return  (ArrayList<HashMap<String, String>>)JSONConverter.toHashMapList("[" + executor.testDataRepository.getTestDataObject(target) + "]");
            }
        }
    }

    /**
     * Check if Values looks like Price
     * @param value
     * @return
     */
    private boolean looksLikePrice(String value) {
        return value.replace("$", "").matches("[0-9\\.,]+");
    }

    /**
     * Get Price/Number
     * @param floatValue price/number
     * @return float with two digits
     */
    private String getFloatValue(String floatValue) {
        String expectedValue = floatValue
                .replace("$", "")
                .replace(",","")
                .trim();
        return Float.valueOf(expectedValue).toString();


//        String expectedFloatValue;
//        if(!expectedValue.contains("."))
//            expectedFloatValue = expectedValue;
//        else {
//            String[] priceParts = expectedValue.split("\\.");
//            if (priceParts[1].length() == 1)
//                expectedFloatValue = expectedValue;
//            else {
//                expectedFloatValue = priceParts[0] + "." + priceParts[1].substring(0, 2);
//            }
//        }
//        return expectedFloatValue;
    }

    private String cleanValue(String data) {
        return data.replaceAll("[\n\r\t ]","");
    }
//
//    {transaction_id=, notes=, type_id=75, , description=, tax=3, tax_rate=0.1, sf_invoice_id=, paid_at=, payment_method_id=1, licenses=5, total=33, status_id=72, rate=6, subtotal=30,, }
//    {notes=, status_code=Due,
//            before=<td><div class="btn-group"><a class="btn btn-xs btn-default" href="http://em-test.cleanOriginmedia.com/admin/companies/99463273/invoices/2293/view">View</a><a class="btn btn-xs btn-default" href="http://em-test.cleanOriginmedia.com/admin/companies/99463273/invoices/2293/edit">Edit</a><a class="btn btn-xs btn-default text-danger" data-toggle="modal" data-target=".js-remove-invoice-modal" href="#" data-id="2293" data-total="33" data-description=""><i class="fa fa-trash-o"></i> Delete</a></td>, contract_id=1094, DT_RowClass=, description=, scheduled_at=<span class="text-muted"><i>Not Set</i></span>,
//            created_at=2020-09-16 16:10:37, tax_rate=0.1, total=$33.00, status_id=72, updated_at=2020-09-16 16:10:37, rate=6, currency=usd, id=2293, after=<td><div class="btn-group"><a class="btn btn-xs btn-primary" href="http://em-test.cleanOriginmedia.com/admin/companies/99463273/invoices/2293/pay"><i class="fa "></i> Pay</a></td>,
//            transaction_id=, type_id=75, status_name=Due, tax=$3.00, sf_invoice_id=, paid_at=null, payment_method_id=1, licenses=5, method_id=null, subtotal=$30.00,  type_code=recurring}
//

}