package automation.tools;

import automation.annotations.KeywordRegexp;
import automation.datasources.FileManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Class that prepares HTML file with list of available Actions/Keywords<br>
 *     Generates two files in current directory: Actions.html and Keywords.html
 */
public class DocumentationHelper {

    public static final String keywordsDir = "src/main/java/automation/keyword/";

    private static final String ACTIONS_REFERENCE_FILE_NAME = "Actions.html";
    private static final String KEYWORDS_REFERENCE_FILE_NAME = "Keywords.html";
    private static final String RESULT_FILE_NAME = "ActionsAndKeywords.html";

    static public void main(String[] args) throws IOException {

        documentActionsAndKeywords(RESULT_FILE_NAME);
        //documentActions(REFERENCE_FILE_NAME);
        //documentKeywords(REFERENCE_FILE_NAME);
    }

    private static void documentActionsAndKeywords(String resultFile) throws IOException {

        File document = new File(resultFile);
        FileUtils.writeStringToFile(document, totalHeader, false);

        //add all actions
        Collection<File> files = FileManager.getFilesFromActionsDir();
        for(File file : files){
            String fileName = file.getName();
            String dirName = file.getParent().replace(FileManager.TEST_ACTIONS, "");
            String description = getDescriptionFromAction(file) ;
            String line = String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", fileName, dirName, description);
            FileUtils.writeStringToFile(document, line, true);
        }

        //add all keywords
        files = FileManager.getFilesFromDir(keywordsDir, new String[]{"java"});
        for(File file : files){
            String fileName = file.getName();
            if(!fileName.contains("Keyword.java") || fileName.contains("Abstract")){
                continue;
            }
            String dirName = file.getParent().replace(keywordsDir, "");
            List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
            String keywordName = getKeywordNameFromKeywordFile(lines) ;
            String description = "<b>Keyword:</b>\n" + getDesriptionFromKeyword(lines) ;
            String line = String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", keywordName, dirName, description);
            FileUtils.writeStringToFile(document, line.replace("\n", "<br>"), true);
        }

        FileUtils.writeStringToFile(document, totalFooter, true);
    }


    /**
     * Prepare file with Actions
     * @throws IOException possible exception
     */
    private static void documentActions() throws IOException {
        String resultFile = ACTIONS_REFERENCE_FILE_NAME;
        File document = new File(resultFile);
        FileUtils.writeStringToFile(document, actionsHeader, false);

        Collection<File> files = FileManager.getFilesFromActionsDir();
        for(File file : files){
            String fileName = file.getName();
            String dirName = file.getParent().replace(FileManager.TEST_ACTIONS, "");
            String description = getDescriptionFromAction(file) ;
            String line = String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", fileName, dirName, description);
            FileUtils.writeStringToFile(document, line, true);
        }

        FileUtils.writeStringToFile(document, actionFooter, true);
    }

    /**
     * Prepare file with Keywords
     * @throws IOException possible exception
     */
    private static void documentKeywords() throws IOException {
        String resultFile = KEYWORDS_REFERENCE_FILE_NAME;
        File document = new File(resultFile);
        FileUtils.writeStringToFile(document, keywordsHeader, false);

        Collection<File> files = FileManager.getFilesFromDir(keywordsDir, new String[]{"java"});
        for(File file : files){
            String fileName = file.getName();
            if(!fileName.contains("Keyword.java") || fileName.contains("Abstract")){
                continue;
            }
            String dirName = file.getParent().replace(keywordsDir, "");
            List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
            String keywordName = getKeywordNameFromKeywordFile(lines) ;
            String description = getDesriptionFromKeyword(lines) ;
            String line = String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", keywordName, dirName, description);
            FileUtils.writeStringToFile(document, line.replace("\n", "<br>"), true);
        }

        FileUtils.writeStringToFile(document, keywordsFooter, true);
    }

    /**
     * Prepare and Add file with Keywords
     * @throws IOException possible exception
     */
    private static void documentKeywords(String resultFile) throws IOException {
        File document = new File(resultFile);
        FileManager.appendToFile(resultFile, keywordsHeader);

        Collection<File> files = FileManager.getFilesFromDir(keywordsDir, new String[]{"java"});
        for(File file : files){
            String fileName = file.getName();
            if(!fileName.contains("Keyword.java") || fileName.contains("Abstract")){
                continue;
            }
            String dirName = file.getParent().replace(keywordsDir, "");
            List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
            String keywordName = getKeywordNameFromKeywordFile(lines) ;
            String description = getDesriptionFromKeyword(lines) ;
            String line = String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", keywordName, dirName, description);
            FileUtils.writeStringToFile(document, line.replace("\n", "<br>"), true);
        }

        FileManager.appendToFile(resultFile, keywordsFooter);
    }

    /**
     * Get description of Action
     * @param file
     * @return
     * @throws IOException possible exception
     */
    private static String getDescriptionFromAction(File file) throws IOException {
        String PREREQUISITES_LABEL = "Prerequisites:";
        String EXAMPLE_LABEL = "Example:";
        String EXAMPLE_OF_USAGE_LABEL = "Example of usage:";
        String PARAMETERS_LABEL = "Parameters:";
        String ACTION_LABEL = "Action:";

        String content = "";
        content = FileUtils.readFileToString(file, StandardCharsets.UTF_8)
                .replaceAll("(^#|\n#)","\n")
                .replace(ACTION_LABEL, "<b>"+ACTION_LABEL+"</b>")
                .replace(PARAMETERS_LABEL, "<b>"+PARAMETERS_LABEL+"</b>")
                .replace(PREREQUISITES_LABEL, "<b>"+PREREQUISITES_LABEL+"</b>")
                .replace(EXAMPLE_OF_USAGE_LABEL, "<b>"+EXAMPLE_OF_USAGE_LABEL+"</b>")
                .replace(EXAMPLE_LABEL, "<b>"+EXAMPLE_LABEL+"</b>")
                .replace("\n", "<br>");
        return content;
    }

    /**
     * Get description of Keyword
     * @param lines
     * @return
     * @throws IOException possible exception
     */
    private static String getDesriptionFromKeyword(List<String> lines) throws IOException {
        String content = "";

        boolean detailedProcessing = false;
        for (String line : lines) {
            if (line.trim().startsWith("*/") && detailedProcessing) {
                detailedProcessing = false;
                return content;
            }
            if(detailedProcessing){
                content = content + line.replace("*","")  + "\n";
            }
            if (line.trim().startsWith("/**") && content.equals("")) {
                detailedProcessing = true;
            }
        }
        return content;
    }

    /**
     * Get name/label of Keyword
     * @param lines
     * @return
     * @throws IOException possible exception
     */
    private static String getKeywordNameFromKeywordFile(List<String> lines) throws IOException {
        String content = "";
        String keywordDescriptionMarker = "@" + KeywordRegexp.class.getSimpleName();
        //List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);

        boolean detailedProcessing = false;
        for (String line : lines) {
            if(line.contains(keywordDescriptionMarker)){
                content = content + line.substring(line.indexOf(keywordDescriptionMarker) + keywordDescriptionMarker.length()+2, line.length() - 2) + "\n";
            }
        }
        return content;
    }

    // Hardcoded bootstrap configurations of Headers and Footers for reference tables
    static String totalHeader = "<link\n" +
            "href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.5/css/bootstrap.min.css\"\n" +
            "rel=\"stylesheet\"/>\n" +
            "<link\n" +
            "href=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.12/css/dataTables.bootstrap4.min.css\"\n" +
            "rel=\"stylesheet\"/>\n" +
            "<div\n" +
            "class=\"container\">\n" +
            "<h1>Available Actions and Keywords</h1>\n" +
            "<table\n" +
            "cellspacing=\"0\"\n" +
            "class=\"table\n" +
            "table-striped\n" +
            //"table-inverse\n" +
            "table-bordered\n" +
            "table-hover\"\n" +
            "id=\"content\"\n" +
            "width=\"100%\">\n" +
            "<thead>\n" +
            "<tr>\n" +
            "<th width='20%'>Action/Keyword</th>\n" +
            "<th width='10%'>Group(Directory)</th>\n" +
            "<th width='70%'>Description</th>\n" +
            "</tr>"+
            "</thead>\n" +
            "<tbody>";


    static String actionsHeader = "<link\n" +
            "href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.5/css/bootstrap.min.css\"\n" +
            "rel=\"stylesheet\"/>\n" +
            "<link\n" +
            "href=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.12/css/dataTables.bootstrap4.min.css\"\n" +
            "rel=\"stylesheet\"/>\n" +
            "<div\n" +
            "class=\"container\">\n" +
            "<h1>Available Actions</h1>\n" +
            "<table\n" +
            "cellspacing=\"0\"\n" +
            "class=\"table\n" +
            "table-striped\n" +
            //"table-inverse\n" +
            "table-bordered\n" +
            "table-hover\"\n" +
            "id=\"actionContent\"\n" +
            "width=\"100%\">\n" +
            "<thead>\n" +
            "<tr>\n" +
            "<th width='20%'>Action</th>\n" +
            "<th width='10%'>Directory</th>\n" +
            "<th width='70%'>Description</th>\n" +
            "</tr>"+
            "</thead>\n" +
            "<tfoot>\n" +
            "<tr>\n" +
            "<th>Action</th>\n" +
            "<th>Directory</th>\n" +
            "<th>Description</th>\n" +
            "</tr>"+
            "</tfoot>\n" +
            "<tbody>";

    static String keywordsHeader = "<link\n" +
            "href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.5/css/bootstrap.min.css\"\n" +
            "rel=\"stylesheet\"/>\n" +
            "<link\n" +
            "href=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.12/css/dataTables.bootstrap4.min.css\"\n" +
            "rel=\"stylesheet\"/>\n" +
            "<div\n" +
            "class=\"container\">\n" +
            "<h1>Automation Keywords</h1>\n" +
            "<table\n" +
            "cellspacing=\"0\"\n" +
            "class=\"table\n" +
            "table-striped\n" +
            //"table-inverse\n" +
            "table-bordered\n" +
            "table-hover\"\n" +
            "id=\"keywordsContent\"\n" +
            "width=\"100%\">\n" +
            "<thead>\n" +
            "<tr>\n" +
            "<th width='20%'>Keyword</th>\n" +
            "<th width='10%'>Directory</th>\n" +
            "<th width='70%'>Description</th>\n" +
            "</tr>"+
            "</thead>\n" +
            "<tfoot>\n" +
            "<tr>\n" +
            "<th>Keyword</th>\n" +
            "<th>Directory</th>\n" +
            "<th>Description</th>\n" +
            "</tr>"+
            "</tfoot>\n" +
            "<tbody>";

    static String totalFooter = "</tbody>\n" +
            "</table></div><script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js\"></script><script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.12/js/jquery.dataTables.min.js\"></script>\n" +
            "<script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.13/js/dataTables.bootstrap4.min.js\"></script>\n" +
            "<script>" +
            "    $(document).ready(function() {\n" +
            "    // Setup - add a text input to each footer cell\n" +
            "    $('#content thead th').each( function () {\n" +
            "        var title = $(this).text();\n" +
            "        $(this).html( '<input type=\"text\" placeholder=\"Search '+title+'\" />' );\n" +
            "    } );\n" +
            "\n" +
            "    // DataTable\n" +
            "    var table = $('#content').DataTable({\n" +
            "        initComplete: function () {\n" +
            "            // Apply the search\n" +
            "            this.api().columns().every( function () {\n" +
            "                var that = this;\n" +
            "\n" +
            "                $( 'input', this.header() ).on( 'keyup change clear', function () {\n" +
            "                    if ( that.search() !== this.value ) {\n" +
            "                        that\n" +
            "                            .search( this.value )\n" +
            "                            .draw();\n" +
            "                    }\n" +
            "                } );\n" +
            "            } );\n" +
            "        }\n" +
            "    });\n" +
            "\n" +
            "} );</script>";

    static String actionFooter = "</tbody>\n" +
            "</table></div><script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js\"></script><script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.12/js/jquery.dataTables.min.js\"></script>\n" +
            "<script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.13/js/dataTables.bootstrap4.min.js\"></script>\n" +
            "<script>$(document).ready(function() {\n" +
            "  $('#keywordsContent').DataTable();\n" +
            "});</script>";

    static String keywordsFooter = "</tbody>\n" +
            "</table></div><script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js\"></script><script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.12/js/jquery.dataTables.min.js\"></script>\n" +
            "<script\n" +
            "src=\"https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.13/js/dataTables.bootstrap4.min.js\"></script>\n" +
            "<script>$(document).ready(function() {\n" +
            "  $('#actionContent').DataTable();\n" +
            "});</script>";
}
