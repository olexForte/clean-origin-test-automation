console.log("Injected");

TEST_NAME = "Test1"

VALIDATION_TIME = false
VERIFICATION_VALUE_TIME = false
SAVE_VALUE_TIME = false

function createXPathFromElement(elm) {
        var allNodes = document.getElementsByTagName('*');
        for (var segs = []; elm && elm.nodeType == 1; elm = elm.parentNode)
        {
            if (elm.hasAttribute('id')) {
                    var uniqueIdCount = 0;
                    for (var n=0;n < allNodes.length;n++) { // check that id is unique
                        if (allNodes[n].hasAttribute('id') && allNodes[n].id == elm.id) uniqueIdCount++;
                        if (uniqueIdCount > 1) break;
                    };
                    if ( uniqueIdCount == 1) {
                        segs.unshift(elm.localName.toLowerCase() + '[@id="' + elm.getAttribute('id') + '"]');
                        return '//' + segs.join('/');
                    } else {
                        segs.unshift(elm.localName.toLowerCase() + '[@id="' + elm.getAttribute('id') + '"]');
                    }
            } else if (elm.hasAttribute('class')) {
                segs.unshift(elm.localName.toLowerCase() + '[@class="' + elm.getAttribute('class') + '"]');
            } else {
                for (i = 1, sib = elm.previousSibling; sib; sib = sib.previousSibling) {
                    if (sib.localName == elm.localName)  i++; };
                    segs.unshift(elm.localName.toLowerCase() + '[' + i + ']');
            };
        };
        return segs.length ? '//' + segs.join('/') : null;
    };

function lookupElementByXPath(path) {
        var evaluator = new XPathEvaluator();
        var result = evaluator.evaluate(path, document.documentElement, null,XPathResult.FIRST_ORDERED_NODE_TYPE, null);
        return  result.singleNodeValue;
    }

function addRecorderPanel() {

        var panel = document.createElement("div");
        panel.setAttribute("id", "recorderPanel")
        panel.style = "position: fixed; bottom: 0;width: 100%; height:10%; font-family: 'Arial';font-size: 40px;font-style: normal;background-color: #666; z-index:5";

        var addValidationButton = document.createElement("button");
        addValidationButton.onclick = addValidation;
        addValidationButton.innerText = "Add validation"
        addValidationButton.setAttribute("id", "addValidation");
        addValidationButton.setAttribute("invisibleForRecorder", "true")
        addValidationButton.style = "color:#05FF33;width:20%; height:100%;"
        panel.appendChild(addValidationButton);

        var saveValueButton = document.createElement("button");
        saveValueButton.onclick = saveValue;
        saveValueButton.innerText = "Save value"
        saveValueButton.setAttribute("id", "saveValue");
        saveValueButton.setAttribute("invisibleForRecorder", "true")
        saveValueButton.style = "color:#33B8FF;width:20%; height:100%;"
        panel.appendChild(saveValueButton);

        var addValueValidationButton = document.createElement("button");
        addValueValidationButton.onclick = addValueValidation;
        addValueValidationButton.innerText = "Validate value"
        addValueValidationButton.setAttribute("id", "addValueValidationButton");
        addValueValidationButton.setAttribute("invisibleForRecorder", "true")
        addValueValidationButton.style = "color:#05FF33;width:20%; height:100%;"
        panel.appendChild(addValueValidationButton);

        var saveButton = document.createElement("button");
        saveButton.onclick = downloadResults;
        saveButton.innerText = "Save results"
        saveButton.setAttribute("id", "saveResults");
        saveButton.setAttribute("invisibleForRecorder", "true")
        saveButton.style = "color:#33B8FF;width:20%; height:100%;"
        panel.appendChild(saveButton);

        document.getElementsByTagName("body")[0].appendChild(panel);

    }

function addValidation(){

    VALIDATION_TIME = true
    document.body.style.cursor = "help"

    document.getElementById('addValidation').style.color = '#440044'


//document.getElementById('id').style.pointerEvents = 'none';
//// To re-enable:
//document.getElementById('id').style.pointerEvents = 'auto';
}

function saveValue(){
    SAVE_VALUE_TIME = true
    document.body.style.cursor = "help"
    document.getElementById('saveValue').style.color = '#440044'

}

function addValueValidation(){
    VERIFICATION_VALUE_TIME = true;
    document.body.style.cursor = "help"
    document.getElementById('addValueValidationButton').style.color = '#440044'
}

function resetRecordingState(){
    VERIFICATION_VALUE_TIME = false
    SAVE_VALUE_TIME = false
    VALIDATION_TIME = false
    document.body.style.cursor = "auto"
    document.getElementById('addValidation').style.color = "#B5FF33"
    document.getElementById('saveValue').style.color = "#33B8FF"
    document.getElementById('addValueValidationButton').style.color = "#B5FF33"
}

function removeRecorderPanel(){
    document.getElementById("recorderPanel").remove();
}

function downloadResults() {
    console.log("Download")
    var contents;
        //contents = document.getElementById('recordingResults').innerText.replace(/\n/g, "\r\n");
                chrome.storage.local.get('scriptSteps',  function(result) {
                     contents = result.scriptSteps;
                     console.log(contents);
                     downloadTextAsAFile(TEST_NAME + '.txt', contents);
                });



         //contents = document.getElementById('recordingLocators').innerText.replace(/\n/g, "\r\n");
                chrome.storage.local.get('locatorSteps',  function(result) {
                    contents = result.locatorSteps;
                    console.log(contents);
                    downloadTextAsAFile(TEST_NAME + '_Locators.txt', contents);
                         });


         //contents = document.getElementById('recordingData').innerText.replace(/\n/g, "\r\n");
                chrome.storage.local.get('dataSteps',  function(result) {
                    contents = result.dataSteps;
                    console.log(contents);
                    downloadTextAsAFile(TEST_NAME + '_Data.txt', contents);
                });

}

function downloadTextAsAFile(fileName, text){

        ///
        window.URL = window.URL;
        var blob = new Blob([text], { type: "text/plain" });
        //var blob = new Blob([contents], { type: "text/plain" });
        var a = document.createElement('a');
        a.href = window.URL.createObjectURL(blob);
        a.download = fileName ; // set the file name // getTimestamp() + fileName
        a.setAttribute("invisibleForRecorder", true)
        a.style.display = 'none';
        document.body.appendChild(a);
        a.click(); //this is probably the key - simulatating a click on a download link
        delete a;// we don't need this anymore
}


function appendToScripts(text) {

                var oldSteps = "";
                var newSteps = "";

                chrome.storage.local.get('scriptSteps',  function(result) {
                     oldSteps = result.scriptSteps;
                     if(!oldSteps)
                        oldSteps = ""
                     newSteps = oldSteps + "\n" + text;
                                     chrome.storage.local.set({"scriptSteps" : newSteps}, function(result) {
                                          //document.getElementById('scriptSteps').value = newSteps;
                                     });
                });

}

function appendToData(text) {
                var oldSteps = "";
                var newSteps = "";

                chrome.storage.local.get('dataSteps',  function(result) {
                     oldSteps = result.dataSteps;
                     if(!oldSteps)
                        oldSteps = ""
                     newSteps = oldSteps + "\n" + text;
                                     chrome.storage.local.set({"dataSteps" : newSteps}, function(result) {
                                          //document.getElementById('scriptSteps').value = newSteps;
                                     });
                });
    }

function appendToLocators(text) {

                var oldSteps = "";
                var newSteps = "";

                chrome.storage.local.get('locatorSteps',  function(result) {
                     oldSteps = result.locatorSteps;
                     if(!oldSteps)
                        oldSteps = ""
                     newSteps = oldSteps + "\n" + text;
                                     chrome.storage.local.set({"locatorSteps" : newSteps}, function(result) {
                                          //document.getElementById('scriptSteps').value = newSteps;
                                     });
                });
    }

function changeColour(element){

        var color = element.style.backgroundColor;
        element.style.backgroundColor = "#FDFF47";

        setTimeout( function(){ element.style.backgroundColor = "#FDFF47"; console.log("Set")}, 0);
        setTimeout( function(){ element.style.backgroundColor = color; console.log("Reset") }, 500);
    }

function hoverListener(e) {
        var target = e.target;

        if(target.getAttribute("invisibleForRecorder") || target.style.backgroundColor == "#FDFF47")
            return;

        changeColour(target);
}

function noClicksEnable(e){
    e.style.pointerEvents = 'none';

    //document.getElementById('id').style.pointerEvents = 'auto'; 
}

function clickListener(e) {
        var target = e.target;

        if(target.getAttribute("invisibleForRecorder"))
            return;

        var xpath = createXPathFromElement(target)

        console.log(target);

     // Click

   var dataLine
   var locatorLine
   var action

    if(VALIDATION_TIME){
            var locatorName = prompt("Validate \n How do you want to name Locator? ")
            if(locatorName){
             action = "Validate element '" + TEST_NAME + "." + locatorName + "' is visible"
             locatorLine = locatorName + " = " + xpath
            }

    } else
    if(VERIFICATION_VALUE_TIME){
             var locatorName = prompt("Validate values \n How do you want to name Locator? ")
             if(locatorName){

             var dataFieldName = prompt("Validate values \n With which Datafield do you want to compare? ")
             if(dataFieldName){

              action = "Validate values are equal '" + TEST_NAME + "." + locatorName + "' and 'save."  + dataFieldName + "'"
              locatorLine = locatorName + " = " + xpath
             }}
    } else
    if(SAVE_VALUE_TIME){

              locatorName = prompt("Save \n How do you want to name Locator? ")
             if(locatorName){
             var dataFieldName = prompt("Save \n How do you want to name Datafield? ")
             if(dataFieldName){

             //target = document.elementFromPoint(e.x, e.y);

              action = "Save element text '" + TEST_NAME + "." + locatorName + "' to 'saved."  + dataFieldName + "'"
              locatorLine = locatorName + " = " + xpath
             //var dataLine = dataFieldName + " = " + target.innerText
            }}

    }
    else {
        var locatorName = prompt("Click \n How do you want to name Locator? ")
        if(!locatorName)
             return
         action = "Click on '" + TEST_NAME + "." + locatorName + "'"
        appendToScripts(action)

         locatorLine = locatorName + " = " + xpath
        appendToLocators(locatorLine)
    }

    if(dataLine)
       appendToData(dataLine)
    if(locatorLine)
        appendToLocators(locatorLine)
    if(action)
        appendToScripts(action)

    //changeColour(target);


    resetRecordingState()

    e.stopPropagation();
    e.preventDefault();
}

function changeListener (e) {
        var target = e.target;

        if(target.getAttribute("invisibleForRecorder"))
            return;

        var xpath = createXPathFromElement(target)

        console.log(target);

        if(target.tagName == "SELECT"){
             var locatorName = prompt("Select \n How do you want to name Locator? ")
             if(!locatorName)
                  return
             var dataFieldName = prompt("Select \n How do you want to name Datafield? ")
             if(!dataFieldName)
                  return
             var action = "Select dropdown value'" + TEST_NAME + "." + dataFieldName + "' in '" + TEST_NAME + "." + locatorName + "'"
             var dataLine = dataFieldName + " = " + target.value
        } else {

            var locatorName = prompt("Typing \n How do you want to name Locator? ")
            if(!locatorName)
                return
            var dataFieldName = prompt("Typing \n How do you want to name Datafield? ")
            if(!dataFieldName)
                return
            var action = "Type value '" + TEST_NAME + "." + dataFieldName + "' in '" + TEST_NAME + "." + locatorName + "'"
            var dataLine = dataFieldName + " = " + target.value
           }

        var locatorLine = locatorName + " = " + xpath

        if(dataLine)
            appendToData(dataLine)
        appendToLocators(locatorLine)
        appendToScripts(action)

        //changeColour(target);

    }

var curElColor = ""

function addListeners() {
    document.addEventListener('click', clickListener, true)
    document.addEventListener('change',  changeListener, false)
    //document.addEventListener('mouseenter',  function (e){ element = e.target; curElColor = element.style.backgroundColor; element.style.backgroundColor = "#FDFF47"; console.log("H"+element)  } , false)
    //document.addEventListener('mouseleave',  function (e){ element = e.target;  element.style.backgroundColor = curElColor; console.log(element) }  , false)
    addRecorderPanel();
}



function removeListeners() {
    document.removeEventListener('click', clickListener, true)
    document.removeEventListener('change',  changeListener, false)
    //document.removeEventListener('mouseover',  hoverListener, false)
    removeRecorderPanel();
}

window.addEventListener('load', function() {
console.log("New page opened");
       chrome.storage.local.get(['RECORDING'], function(result) {
       if(result.RECORDING)
            addListeners();
       });

})


chrome.storage.onChanged.addListener(function(changes, namespace) {
        for (var key in changes) {
          var storageChange = changes[key];
          if(key == "RECORDING"){
                console.log("Recording " + storageChange.newValue)
                if(storageChange.newValue){
                    console.log("Add listeners")
                    addListeners();
                } else {
                    removeListeners();
                }
          };
          if(key == "TEST_NAME"){
                    TEST_NAME = storageChange.newValue;
                    console.log("Test name: " + TEST_NAME);
          };
      }
   }
);

//      chrome.storage.onChanged.addListener(function(changes, namespace) {
//        for (var key in changes) {
//          var storageChange = changes[key];
//          console.log('Storage key "%s" in namespace "%s" changed. ' +
//                      'Old value was "%s", new value is "%s".',
//                      key,
//                      namespace,
//                      storageChange.oldValue,
//                      storageChange.newValue);
//        }
//      });

//// opens a communication port
//chrome.runtime.onConnect.addListener(function(port) {
//
//    // listen for every message passing throw it
//    port.onMessage.addListener(function(o) {
//
//        // if the message comes from the popup
//        if (o.from && o.from === 'popup' && o.start && o.start === 'Y') {
//
//            // inserts a script into your tab content
//            chrome.tabs.executeScript(null, {
//
//                // the script will click the button into the tab content
//                code: "alert('Recording Started');"
//            });
//        }
//    });
//});