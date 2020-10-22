console.log("Injected");

TEST_NAME = "Test1"
INDEX_OF_ELEMENT = 1
var curElColor = ""

// get XPATH for current element
function createXPathFromElement(originalElement) {
        var allNodes = document.getElementsByTagName('*');
        var curElement = originalElement;
        for (var pathParts = []; curElement && curElement.nodeType == 1; curElement = curElement.parentNode)
        {
            if (curElement.hasAttribute('id')) {
                pathParts.unshift(curElement.localName.toLowerCase() + '[@id="' + curElement.getAttribute('id') + '"]');
                break;
            }
            if (curElement.hasAttribute('name')) {
                pathParts.unshift(curElement.localName.toLowerCase() + '[@name="' + curElement.getAttribute('name') + '"]');
                break;
            }
            if (curElement.innerText != "" && curElement.textContent.trim().length < 50) {
                console.log("Text")
                pathParts.unshift(curElement.localName.toLowerCase() + '[contains(text(),"' + curElement.textContent.trim() + '")]');
                if(lookupElementByXPath('//' + pathParts.join('/')) == 0){
                    pathParts.shift(); // remove first item - it was bad
                    console.log("Bad Text")

                    if (curElement.hasAttribute('class')) {
                            console.log("Class after Text")
                          pathParts.unshift(curElement.localName.toLowerCase() + '[@class="' + curElement.getAttribute('class') + '"]');
                    } else {
                        console.log("Index after Text")
                          for (i = 1, sib = curElement.previousSibling; sib; sib = sib.previousSibling) {
                               if (sib.localName == curElement.localName)  i++;
                          };
                          pathParts.unshift(curElement.localName.toLowerCase() + '[' + i + ']');
                    };
                }
            } else
            if (curElement.hasAttribute('class')) {
                console.log("Class")
                pathParts.unshift(curElement.localName.toLowerCase() + '[@class="' + curElement.getAttribute('class') + '"]');
            } else {
                console.log("Index")
                for (i = 1, sib = curElement.previousSibling; sib; sib = sib.previousSibling) {
                    if (sib.localName == curElement.localName)  i++;
                };
                pathParts.unshift(curElement.localName.toLowerCase() + '[' + i + ']');
            };
            if(lookupElementByXPath('//' + pathParts.join('/')) == 1){
                return '//' + pathParts.join('/')
            }
        };
        // final processing (adding index if more than 1 was found)
        if(lookupElementByXPath('//' + pathParts.join('/')) == 1)
            return pathParts.length ? '//' + pathParts.join('/') : null;
        else
            for(var i = 1; i < lookupElementByXPath('//' + pathParts.join('/')) + 1 ; i++){
                var xpathWithIndex = '(//' + pathParts.join('/') + ')[' + i + ']'
                if(getElementByXPath(xpathWithIndex) == originalElement)
                    return xpathWithIndex;
            }
    };

// get number of elements described by XPATH
function lookupElementByXPath(path) {
        var evaluator = new XPathEvaluator();
        var result = evaluator.evaluate(path, document.documentElement, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
        console.log(path)
        console.log(result.snapshotLength)
        return  result.snapshotLength;
    }

function getElementByXPath(path) {
        var evaluator = new XPathEvaluator();
        var result = evaluator.evaluate(path, document.documentElement, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
        //console.log(path)
        //console.log(result.singleNodeValue)
        return  result.singleNodeValue;
    }

// download results
function downloadResults() {
    console.log("Download")
    var contents;
        //contents = document.getElementById('recordingResults').innerText.replace(/\n/g, "\r\n");
                chrome.storage.local.get('scriptSteps',  function(result) {
                     contents = result.scriptSteps;
                     console.log(contents);
                     downloadTextAsAFile(TEST_NAME + '.ct', contents);
                });



         //contents = document.getElementById('recordingLocators').innerText.replace(/\n/g, "\r\n");
                chrome.storage.local.get('locatorSteps',  function(result) {
                    contents = result.locatorSteps;
                    console.log(contents);
                    downloadTextAsAFile(TEST_NAME + '_Locators.prop', contents);
                         });


         //contents = document.getElementById('recordingData').innerText.replace(/\n/g, "\r\n");
                chrome.storage.local.get('dataSteps',  function(result) {
                    contents = result.dataSteps;
                    console.log(contents);
                    downloadTextAsAFile(TEST_NAME + '_Data.prop', contents);
                });

}

// download text as text file
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

// append text to Scripts Popup panel
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

// append text to Data Popup panel
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

// append text to Locators Popup panel
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

// change color of element for 1 second
function changeColour(element){

        var color = element.style.backgroundColor;
        element.style.backgroundColor = "#FDFF47";

        setTimeout( function(){ element.style.backgroundColor = "#FDFF47"; console.log("Set")}, 0);
        setTimeout( function(){ element.style.backgroundColor = color; console.log("Reset") }, 1000);
    }

// click event listener
function clickListener(e) {
        var target = e.target;

        if(target.getAttribute("invisibleForRecorder"))
            return;

        var xpath = createXPathFromElement(target)

        console.log(target);
        console.log(xpath);

     // Click

   var dataLine
   var locatorLine
   var action

    if(e.ctrlKey || e.metaKey){


            var locatorName = prompt("Click \n Locator name ", target.tagName + "_" + INDEX_OF_ELEMENT)
            if(!locatorName)
                 return
            action = "Click on '" + TEST_NAME + "_LOCATORS." + locatorName + "'"

            locatorLine = locatorName + " = " + xpath

    } else
    if(e.altKey){

        if(e.shiftKey){
             var locatorName = prompt("Validate values \n Locator name ", target.tagName + "_" + INDEX_OF_ELEMENT)
             if(locatorName){

             var dataFieldName = prompt("Validate values \n Datafield to compare with" , "DATA_" + INDEX_OF_ELEMENT)
             if(dataFieldName){

              action = "Validate value '" + TEST_NAME + "_LOCATORS." + locatorName + "' equals 'save."  + dataFieldName + "'"
              locatorLine = locatorName + " = " + xpath

             }}

         } else {

            var locatorName = prompt("Validate \n Locator name ", target.tagName + "_" + INDEX_OF_ELEMENT)
            if(locatorName){
             action = "Validate element '" + TEST_NAME + "_LOCATORS." + locatorName + "' is visible"
             locatorLine = locatorName + " = " + xpath
            }
        }
    } else
    if(e.shiftKey){

             locatorName = prompt("Save \n Locator name ", target.tagName + "_" + INDEX_OF_ELEMENT)
             if(locatorName){
             var dataFieldName = prompt("Save \n Datafield name", "DATA_" + INDEX_OF_ELEMENT)
             if(dataFieldName){

             //target = document.elementFromPoint(e.x, e.y);

              action = "Save element text '" + TEST_NAME + "_LOCATORS." + locatorName + "' to 'saved."  + dataFieldName + "'"
              locatorLine = locatorName + " = " + xpath
              if(target.innerText != "")
               dataLine = dataFieldName + " = " + target.innerText
               else
               dataLine = dataFieldName + " = " + target.value
            }}

    }
    else {
        changeColour(target);
        return
    }

    if(dataLine)
       appendToData(dataLine)
    if(locatorLine){
        appendToLocators(locatorLine)
        INDEX_OF_ELEMENT = INDEX_OF_ELEMENT + 1
     }
    if(action)
        appendToScripts(action)

    changeColour(target);
    //resetRecordingState()

    console.log("Stop propagation");
    e.stopPropagation();
    e.preventDefault();
}

//Change event listener
function changeListener (e) {
        var target = e.target;

        if(target.getAttribute("invisibleForRecorder"))
            return;

        var xpath = createXPathFromElement(target)

        console.log(target);
        console.log(xpath);

        if(target.tagName == "SELECT"){
             var locatorName = prompt("Select \n How do you want to name Locator? ", target.tagName + "_" + INDEX_OF_ELEMENT)
             if(!locatorName)
                  return
             var dataFieldName = prompt("Select \n How do you want to name Datafield? ", "DATA_" + INDEX_OF_ELEMENT)
             if(!dataFieldName)
                  return
             var action = "Select '" + TEST_NAME + "_DATA." + dataFieldName + "' in '" + TEST_NAME + "_LOCATORS." + locatorName + "'"
             var dataLine = dataFieldName + " = " + target.value
        } else if (target.type == 'radio')  {

            var locatorName = prompt("Click on Radio \n How do you want to name Locator? ", target.tagName + "_" + INDEX_OF_ELEMENT)
            if(!locatorName)
                return
            var action = "Click on item '" + TEST_NAME + "_LOCATORS." + locatorName + "'"
           } else if (target.type == 'checkbox')  {

            var locatorName = prompt("Click on Checkbox \n How do you want to name Locator? ", target.tagName + "_" + INDEX_OF_ELEMENT)
            if(!locatorName)
                   return
            var action = "Click on item '" + TEST_NAME + "_LOCATORS." + locatorName + "'"
           }
               else { // INPUT BOX or TEXT AREA

            var locatorName = prompt("Typing \n How do you want to name Locator? ", target.tagName + "_" + INDEX_OF_ELEMENT)
            if(!locatorName)
                return
            var dataFieldName = prompt("Typing \n How do you want to name Datafield? ", "DATA_" + INDEX_OF_ELEMENT)
            if(!dataFieldName)
                return
            var action = "Type value '" + TEST_NAME + "_DATA." + dataFieldName + "' in '" + TEST_NAME + "_LOCATORS." + locatorName + "'"
            var dataLine = dataFieldName + " = " + target.value
           }

        var locatorLine = locatorName + " = " + xpath

        if(dataLine)
            appendToData(dataLine)
        appendToLocators(locatorLine)
        appendToScripts(action)
        INDEX_OF_ELEMENT = INDEX_OF_ELEMENT + 1

        changeColour(target);

    }

// add listeners to page
function addListeners(){
    console.log("Add listeners");
    document.addEventListener('click', clickListener, true)
    document.addEventListener('change',  changeListener, false)
    //document.addEventListener('mouseenter',  function (e){ element = e.target; curElColor = element.style.backgroundColor; element.style.backgroundColor = "#FDFF47"; console.log("H"+element)  } , false)
    //document.addEventListener('mouseleave',  function (e){ element = e.target;  element.style.backgroundColor = curElColor; console.log(element) }  , false)
    //addRecorderPanel();
}


//remove required listeners from page
function removeListeners() {
    console.log("Remove listeners");
    document.removeEventListener('click', clickListener, true)
    document.removeEventListener('change',  changeListener, false)
    //document.removeEventListener('mouseover',  hoverListener, false)
    //removeRecorderPanel();
}



// add Load event listener (to check if RECORDING was started)
window.addEventListener('load', function() {
console.log("New page opened");
       chrome.storage.local.get(['RECORDING'], function(result) {//
       if(result.RECORDING)
            addListeners();
       });

})

//processing of changes in Extension Storage
chrome.storage.onChanged.addListener(function(changes, namespace) {
        for (var key in changes) {
          var storageChange = changes[key];
          if(key == "RECORDING"){
                console.log("Recording " + storageChange.newValue)
                if(storageChange.newValue){
                    console.log("Add listeners")
                    addListeners();
                } else {
                    console.log("Remove listeners")
                    removeListeners();
                }
          };
          if(key == "TEST_NAME"){
                console.log("Test name " + storageChange.newValue)
                    TEST_NAME = storageChange.newValue;
                    console.log("Test name: " + TEST_NAME);
          };
          if(key == "SAVING"){
                console.log("SAVING")
                if(storageChange.newValue == "true"){
                    downloadResults();
                    chrome.storage.local.set({"SAVING" : "false"}, function(result) {

                    });
                }
          };
      }
   }
);

//TODO adding of Modal window instead of Prompt
//function createModal(){
//        $("#recorderPanel").append(modalDescription);
//}
//
//var modalDescription = "<div class='container'>\n"+
//                                    "<div class='modal fade ' id='clickModal' role='dialog'>\n"+
//                                    "<div class='modal-dialog modal-sm'><div class='modal-content'><div class='modal-header'>\n"+
//                                    "<h4 class='modal-title'>Click</h4></div>\n"+
//                                    "<div class='modal-body'>\n"+
//                                    "<p>Select LOCATOR name: <input id='clickStepLocator'></input></p>\n"+
//                                    "</div>\n"+
//                                    "<div class='modal-footer'>\n"+
//                                    "<button onclick='' type='button' class='btn btn-default' data-dismiss='modal'>Ok</button>\n"+
//                                    "</div></div></div></div>"+
//                                    "<div class='modal fade ' id='saveModal' role='dialog'>\n"+
//                                    "<div class='modal-dialog modal-sm'><div class='modal-content'><div class='modal-header'>\n"+
//                                    "<h4 class='modal-title'>Save</h4></div>\n"+
//                                    "<div class='modal-body'>\n"+
//                                    "<p>Select LOCATOR name: <input id='saveStepLocator'></input></p>\n"+
//                                    "<p>Select DATA FIELD name: <input id='saveStepData'></input></p>\n"+
//                                    "</div>\n"+
//                                    "<div class='modal-footer'>\n"+
//                                    "<button onclick='' type='button' class='btn btn-default' data-dismiss='modal'>Ok</button>\n"+
//                                    "</div></div></div></div>"+
//                                    "</div>"
