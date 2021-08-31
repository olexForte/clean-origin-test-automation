package automation.tools;

public class ComparatorTool {

    /**
     * Check if Values looks like Price
     * @param value
     * @return
     */
    public static boolean looksLikePrice(String value) {
        return value.replace("CA$", "").replace("$", "").matches("[0-9\\.,]+");
    }

    /**
     * Input -> String with a float number with any number of decimal places.
     * Output -> float value with max of two decimal places in a form of String.
     *
     * @param floatValue
     * @return
     */
    public static String getFloatValue(String floatValue) {
        String expectedValue = floatValue
                .replace("CA$", "")
                .replace("$", "")
                .replace(",","")
                .trim();
        return expectedValue;
    }

    /**
     * Remove special chars
     * @param data
     * @return
     */
    public static String cleanValue(String data) {
        return data.replaceAll("[\n\r\t ]","");
    }

}
