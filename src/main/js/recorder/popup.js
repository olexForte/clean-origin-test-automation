var RECORDING;

//window.addEventListener("onload", updateWindow)


function updateWindow() {

     RECORDING = false;
     chrome.storage.local.get('RECORDING', function(result) {
         console.log(result.RECORDING)
         RECORDING =  result.RECORDING;

         startStopRecording(RECORDING);
     });


     chrome.storage.local.get('TEST_NAME', function(result) {
          console.log(result.TEST_NAME)
          if (result.TEST_NAME)
             document.getElementById('testName').value = result.TEST_NAME;
          else
             document.getElementById('testName').value = "Test1"
     });

     chrome.storage.local.get('scriptSteps', function(result) {
          if(result.scriptSteps)
            document.getElementById('scriptSteps').value = result.scriptSteps;
          else
            document.getElementById('scriptSteps').value = ""
     });
     chrome.storage.local.get('locatorSteps', function(result) {
          if(result.locatorSteps)
            document.getElementById('locatorSteps').value = result.locatorSteps;
          else
            document.getElementById('locatorSteps').value = ""
     });
     chrome.storage.local.get('dataSteps', function(result) {
          if(result.dataSteps)
            document.getElementById('dataSteps').value = result.dataSteps;
          else
            document.getElementById('dataSteps').value = ""
     });

     chrome.storage.local.set({"SAVING" : "false"}, function(result) {
     });
}


document.addEventListener('DOMContentLoaded', function() {
console.log("Loaded");

window.onload = function() {
  console.log("onload" + Date())
  updateWindow()
}

// listens to the click of the button into the popup content
document.getElementById("recordButton").addEventListener('click', function() {

       startStopRecording(!RECORDING);
       RECORDING = !RECORDING

       var TEST_NAME = document.getElementById('testName').value;

       console.log("Test name:  " + TEST_NAME);
       chrome.storage.local.set({"TEST_NAME": TEST_NAME}, function() {
             console.log('Value is set to ' + TEST_NAME);
       });

        console.log("Reset to " + RECORDING);
        chrome.storage.local.set({"RECORDING": RECORDING}, function() {
            console.log('Value is set to ' + RECORDING);
       });
});

document.getElementById("clearButton").addEventListener('click', function() {

       var newSteps = ""

       console.log("New Steps:  " + newSteps);
       chrome.storage.local.set({"scriptSteps": newSteps}, function() {
             console.log('Value is set to ' + newSteps);
       });

       newLocators = ""

       console.log("New Locators:  " + newLocators);
       chrome.storage.local.set({"locatorSteps": newLocators}, function() {
             console.log('Value is set to ' + newLocators);
       });

       var newData = "";

       console.log("New Data:  " + newLocators);
       chrome.storage.local.set({"dataSteps": newData}, function() {
             console.log('Value is set to ' + newData);
       });

        updateWindow();
});

document.getElementById("saveButton").addEventListener('click', function() {

        console.log("Saving... ");
        chrome.storage.local.set({"SAVING": "true"}, function() {
            console.log('Saving');
       });
});


document.getElementById("scriptSteps").addEventListener('change', function() {

       var newSteps = document.getElementById('scriptSteps').value;

       console.log("New Steps:  " + newSteps);
       chrome.storage.local.set({"scriptSteps": newSteps}, function() {
             console.log('Value is set to ' + newSteps);
       });

});


document.getElementById("locatorSteps").addEventListener('change', function() {

       var newLocators = document.getElementById('locatorSteps').value;

       console.log("New Locators:  " + newLocators);
       chrome.storage.local.set({"locatorSteps": newLocators}, function() {
             console.log('Value is set to ' + newLocators);
       });

});


document.getElementById("dataSteps").addEventListener('change', function() {

       var newData = document.getElementById('dataSteps').value;

       console.log("New Data:  " + newData);
       chrome.storage.local.set({"dataSteps": newData}, function() {
             console.log('Value is set to ' + newData);
       });

});




});


function startStopRecording(needToStart) {
    console.log("Recording! " + needToStart)
    if (needToStart){
        document.getElementById("recordButton").innerHTML = "Stop Recording"
        var x = document.getElementById("recordingIndicator");
        x.style.display = "block";
    } else {
        document.getElementById("recordButton").innerHTML = "Start Recording"
        var x = document.getElementById("recordingIndicator");
        x.style.display = "none";
    }
}

function getTimeStamp(){
   return new Date().toISOString().replace(/\.(.*)$/,"").replace(/\D/g,"_")
   //"2018_05_07_09_55_04"
}