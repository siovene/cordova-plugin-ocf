cordova.define("cordova/plugin/oic", function(require, exports, module) {
    var exec = require("cordova/exec"),
        channel = require("cordova/channel");

    channel.createSticky("onCordovaOicReady");
    channel.waitForInitialization("onCordovaOicReady");

    var OIC = function() {
        var self = this;

        channel.onCordovaReady.subscribe(function() {
            self.__initDevice(
                function(device) {
                    self.device = device;
                    channel.onCordovaOicReady.fire();
                },
                function(error) {
                    console.error("Error initializing oic: " + error);
                }
            );
        });
    }

    OIC.prototype.__initDevice = function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "OIC", "__initDevice", []);
    };

    var oic = new OIC();
    module.exports = oic;
});
