
//// listen for our browerAction to be clicked
//chrome.browserAction.onClicked.addListener(function (tab) {
//
//	chrome.tabs.executeScript(tab.ib, {
//		file: 'inject.js'
//	});
//
//});

//chrome.runtime.onMessage.addListener((msg, sender) => {
//  // First, validate the message's structure.
//  if ((msg.from === 'content') && (msg.subject === 'showPageAction')) {
//    // Enable the page-action for the requesting tab.
//    chrome.pageAction.show(sender.tab.id);
//  }
//});

// processing of Storage changes (if required)
document.addEventListener('DOMContentLoaded', function() {
                console.log("Read RECORDING");
                chrome.storage.local.get(['RECORDING'], function(result) {
                    console.log("Initial value set to: " + result.value)
                });
                                console.log("Read all keys");
                                chrome.storage.local.get(null, function(items) {
                                    var allKeys = Object.keys(items);
                                    var allValues = Object.values(items);
                                    console.log(allKeys);
                                    console.log(allValues);
                                });
})

      chrome.storage.onChanged.addListener(function(changes, namespace) {
        for (var key in changes) {
          var storageChange = changes[key];
          console.log('Storage key "%s" in namespace "%s" changed. ' +
                      'Old value was "%s", new value is "%s".',
                      key,
                      namespace,
                      storageChange.oldValue,
                      storageChange.newValue);
        }
      });