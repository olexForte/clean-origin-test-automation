package automation.keyword;

import automation.keyword.complex.*;
import automation.keyword.general.*;
import automation.keyword.mailtrap.CheckForEmailKeyword;
import automation.keyword.mailtrap.DownloadAttachmentFromEmailKeyword;
import automation.keyword.mailtrap.GetFieldFromEmailKeyword;
import automation.keyword.proxy.StartProxyRecordingKeyword;
import automation.keyword.proxy.StopProxyRecordingKeyword;
import automation.keyword.rest.*;
import automation.keyword.ssh.RunSSHCommandKeyword;
import automation.keyword.validation.ValidateEqualsKeyword;
import automation.keyword.validation.ValidateListOfMapsKeyword;
import automation.keyword.validation.ValidateTableKeyword;
import automation.keyword.validation.rest.ValidateResponseKeyword;
import automation.keyword.validation.ui.ScreenshotKeyword;
import automation.keyword.validation.ui.ScreenshotValidationKeyword;
import automation.keyword.validation.ui.ValidateEntityOnPageKeyword;
import automation.keyword.validation.ui.ValidateVisibleKeyword;
import automation.keyword.web.*;

/**
 * List of ALL basic keywords
 */
public enum KeywordEnum {

    OPEN{
        public AbstractKeyword getKeyword(){
            return new OpenKeyword();
        }
    },
    OPEN_TAB{
        public AbstractKeyword getKeyword(){
            return new OpenTabKeyword();
        }
    },
    HOVER{
        public AbstractKeyword getKeyword(){
            return new HoverOverKeyword();
        }
    },
    CLICK{
        public AbstractKeyword getKeyword(){
            return new ClickKeyword();
        }
    },
    DO_CLICK{
        public AbstractKeyword getKeyword(){
            return new DoClickUntilKeyword();
        }
    },
    DO_CLICK_OPTIONAL{
        public AbstractKeyword getKeyword(){
            return new DoClickUntilOptionalKeyword();
        }
    },
    TYPE{
        public AbstractKeyword getKeyword(){
            return new TypeKeyword();
        }
    },

    SELECT{
        public AbstractKeyword getKeyword(){
            return new SelectKeyword();
        }
    },
    UPLOAD_FILE{
        public AbstractKeyword getKeyword(){
            return new UploadFileKeyword();
        }
    },
    SAVE_VALUE{
        public AbstractKeyword getKeyword(){
            return new SaveValueKeyword();
        }
    },
    SAVE_URL{
        public AbstractKeyword getKeyword(){ return new GetURLKeyword(); }
    },
    SAVE_ATTRIBUTE{
        public AbstractKeyword getKeyword(){ return new SaveAttributeValueKeyword(); }
    },
    SAVE_ENTITY{
        public AbstractKeyword getKeyword(){ return new SaveEntityFromPageKeyword(); }
    },
    SAVE_TABLE{
        public AbstractKeyword getKeyword(){ return new SaveTableFromPageKeyword(); }
    },
    SCREENSHOT{
        public AbstractKeyword getKeyword(){
            return new ScreenshotKeyword();
        }
    },
    VALIDATE_VISIBLE{
        public AbstractKeyword getKeyword(){
            return new ValidateVisibleKeyword();
        }
    },
    VALIDATE_EQUALS{
        public AbstractKeyword getKeyword(){
            return new ValidateEqualsKeyword();
        }
    },
    VALIDATE_ENTITY{
        public AbstractKeyword getKeyword(){
            return new ValidateEntityOnPageKeyword();
        }
    },
    VALIDATE_TABLE{
        public AbstractKeyword getKeyword(){
            return new ValidateTableKeyword();
        }
    },
    VALIDATE_LIST_OF_MAPS{
        public AbstractKeyword getKeyword(){
            return new ValidateListOfMapsKeyword();
        }
    },

    DEBUG{
        public AbstractKeyword getKeyword(){
            return new DebugKeyword();
        }
    },
    CALCULATE{
        public AbstractKeyword getKeyword(){
            return new CalculateKeyword();
        }
    },
    GENERATE{
        public AbstractKeyword getKeyword(){
            return new GenerateValueKeyword();
        }
    },

    START_PROXY{
        public AbstractKeyword getKeyword(){
            return new StartProxyRecordingKeyword();
        }
    },
    STOP_PROXY{
        public AbstractKeyword getKeyword(){
            return new StopProxyRecordingKeyword();
        }
    },
    API_GET{
        public AbstractKeyword getKeyword(){
            return new GETRequestKeyword();
        }
    },
    API_POST{
        public AbstractKeyword getKeyword(){
            return new POSTRequestKeyword();
        }
    },
    API_POST_WITH_REDIRECT{
        public AbstractKeyword getKeyword(){
            return new POSTRequestWithRedirectKeyword();
        }
    },
    API_UPLOAD{
        public AbstractKeyword getKeyword(){
            return new UPLOADRequestKeyword();
        }
    },
    API_DELETE{
        public AbstractKeyword getKeyword(){
            return new DELETERequestKeyword();
        }
    },
    SAVE_FROM_RESPONSE{
        public AbstractKeyword getKeyword(){
            return new SaveValueFromAPIResponseKeyword();
        }
    },
    SAVE_MAP_FROM_RESPONSE{
        public AbstractKeyword getKeyword(){
            return new SaveHashmapFromAPIResponseKeyword();
        }
    },
    SAVE_COOKIE_FROM_RESPONSE{
        public AbstractKeyword getKeyword(){
            return new SaveCookieValueFromAPIResponseKeyword();
        }
    },
    SAVE_HEADER_FROM_RESPONSE{
        public AbstractKeyword getKeyword(){
            return new SaveHeaderValueFromAPIResponseKeyword();
        }
    },
    SAVE_COOKIES_HEADERS_FROM_RESPONSE{
        public AbstractKeyword getKeyword(){
            return new SaveHeadersCookiesFromAPIResponseKeyword();
        }
    },
    RESPONSE_VALIDATE{
        public AbstractKeyword getKeyword(){
            return new ValidateResponseKeyword();
        }
    },
    SCREENSHOT_VALIDATION{
        public AbstractKeyword getKeyword(){
            return new ScreenshotValidationKeyword();
        }
    },

    RANDOMIZE_FILE{
        public AbstractKeyword getKeyword(){
            return new RandomizeFileKeyword();
        }
    },

    //mailtrap
    CHECK_EMAIL{
        public AbstractKeyword getKeyword(){
            return new CheckForEmailKeyword();
        }
    },
    GET_VALUE_FROM_EMAIL{
        public AbstractKeyword getKeyword(){
            return new GetFieldFromEmailKeyword();
        }
    },
    GET_ATTACHMENT_FROM_EMAIL{
        public AbstractKeyword getKeyword(){
            return new DownloadAttachmentFromEmailKeyword();
        }
    },

    RUN_JS_COMMAND{
        public AbstractKeyword getKeyword(){
            return new ExecuteJSKeyword();
        }
    },
    RUN_SSH_COMMAND{
        public AbstractKeyword getKeyword(){
            return new RunSSHCommandKeyword();
        }
    },

    WAIT{
        public AbstractKeyword getKeyword(){
            return new WaitKeyword();
        }
    },

    COMPARE_FILES{
        public AbstractKeyword getKeyword(){
            return new CompareFilesKeyword();
        }
    },
    COMPARE_HASHMAP{
        public AbstractKeyword getKeyword(){
            return new CompareMapListsKeyword();
        }
    },
    READ_FILE{
        public AbstractKeyword getKeyword(){
            return new ReadFileKeyword();
        }
    },
    FILL_DATA_FORM{
        public AbstractKeyword getKeyword(){
            return new FillDataFormKeyword();
        }
    },
    GET_VALUE_FROM_VARIABLE{
        public AbstractKeyword getKeyword(){
            return new GetValueFromSavedKeyword();
        }
    },

    //application specific
    SET_DIAMONDS_FILTER{
        public AbstractKeyword getKeyword(){
            return new SetDiamondFilterKeyword();
        }
    },
    VALIDATE_DIAMONDS_FILTER{
        public AbstractKeyword getKeyword(){
            return new ValidateDiamondFilterKeyword();
        }
    },
    SET_RINGS_FILTER{
        public AbstractKeyword getKeyword(){
            return new SetEngagementFilterKeyword();
        }
    },
    VALIDATE_RINGS_FILTER{
        public AbstractKeyword getKeyword(){
            return new ValidateEngagementFilterKeyword();
        }
    },
    GET_ITEMS_FROM_ADMIN{
        public AbstractKeyword getKeyword(){
            return new GetItemsFromAdminByRequestKeyword();
        }
    }

    ;

    public AbstractKeyword getKeyword(){
        return null;
    }
}
