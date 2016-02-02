cordova.define("cordova/plugin/oic", function(require, exports, module) {
    var exec = require("cordova/exec");

    /**************************************************************************
    *  OicPlugin                                                              *
    *  The Cordova plugin.                                                    *
    **************************************************************************/
    var OicPlugin = function() {
        this.backend = "iotivity";
    }

    OicPlugin.prototype.setBackend = function(backend) {
        var self = this;

        return new Promise(function(resolve, reject) {
            function successCallback() {
                self.backend = backend;
                resolve();
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OicPlugin", "setBackend",
                 [backend]);
        });
    }

    OicPlugin.prototype.findResources = function(options) {
        var self = this;

        if (options === undefined) {
            options = {};
        }

        return new Promise(function(resolve, reject) {
            function successCallback(event) {
                if (event === "OK") {
                    // No event: this is just the native call completing.
                    resolve();
                } else {
                    // Event passed: this is a "resource found" callback.
                    self.onresourcefound(event);
                }
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OicPlugin", "findResources",
                 [options]);
        });
    };

    OicPlugin.prototype.onresourcefound = function(event) {};

    var oic = new OicPlugin();
    module.exports = oic;
});
