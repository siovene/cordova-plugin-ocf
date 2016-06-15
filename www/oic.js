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

    OicPlugin.prototype.findDevices = function() {
        var self = this;

        return new Promise(function(resolve, reject) {
            function successCallback(event) {
                if (event === "OK") {
                    // No event: this is just the native call completing.
                    resolve();
                } else {
                    // Event passed: this is a "device found" callback.
                    self.ondevicefound(event);
                }
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OicPlugin", "findDevices", []);
        });
    };

    OicPlugin.prototype.update = function(resource) {
        var self = this;

        return new Promise(function(resolve, reject) {
            function successCallback(event) {
                if (event === "OK") {
                    // No event, this is just the native call completing.
                    resolve();
                } else {
                    // Event passed: this is an "onupdate" callback.
                    self.onupdate(event);
                }
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OicPlugin", "updateResource", [resource]);
        });
    }

    OicPlugin.prototype.onresourcefound = function(event) {};
    OicPlugin.prototype.ondevicefound = function(event) {};
    OicPlugin.prototype.onupdate = function(event) {};

    /**************************************************************************
    *  Create the plugin and get things moving.                               *
    **************************************************************************/

    var oic = new OicPlugin();

    // To get resource update events, we need to poll from the JS side.
    setInterval(function() {
        function successCallback(updates) {
            if (updates.length > 0) {
                oic.onupdate({updates: updates});
            }
        }

        function errorCallback(error) {
            console.error(error);
        }

        exec(successCallback, errorCallback, "OicPlugin", "getResourceUpdates", []);
    }, 10000);

    module.exports = oic;
});
