cordova.define("cordova/plugin/ocf", function(require, exports, module) {
    var exec = require("cordova/exec");

    /**************************************************************************
    *  OcfPlugin                                                              *
    *  The Cordova plugin.                                                    *
    **************************************************************************/
    var OcfPlugin = function() {
        this.backend = "iotivity";
        this.resources = [];
    }

    OcfPlugin.prototype.__compareResources__ = function(a, b) {
        var aKey = a.id.deviceId + a.id.resourcePath,
            bKey = b.id.deviceId + b.id.resourcePath;

        return aKey === bKey;
    }

    OcfPlugin.prototype.setBackend = function(backend) {
        var self = this;

        return new Promise(function(resolve, reject) {
            function successCallback() {
                self.backend = backend;
                resolve();
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OcfPlugin", "setBackend",
                 [backend]);
        });
    }

    OcfPlugin.prototype.findResources = function(options) {
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
                    var i, found = false;

                    for (i = 0; i < self.resources.length; i++) {
                        if (self.__compareResources__(self.resources[i], event.resource)) {
                            found = true;
                        }
                    }

                    if (!found) {
                        self.resources.push(event.resource);
                        self.onresourcefound(event);
                    }
                }
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OcfPlugin", "findResources",
                 [options]);
        });
    };

    OcfPlugin.prototype.findDevices = function() {
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

            exec(successCallback, errorCallback, "OcfPlugin", "findDevices", []);
        });
    };

    OcfPlugin.prototype.update = function(resource) {
        var self = this;

        return new Promise(function(resolve, reject) {
            function successCallback(event) {
                resolve();
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OcfPlugin", "updateResource", [resource]);
        });
    }

    OcfPlugin.prototype.onresourcefound = function(event) {};
    OcfPlugin.prototype.ondevicefound = function(event) {};

    /**************************************************************************
    *  Create the plugin and get things moving.                               *
    **************************************************************************/

    var ocf = new OcfPlugin();

    // To get resource update events, we need to poll from the JS side.
    setInterval(function() {
        function successCallback(updates) {
            var i, j, update, resource;

            for (i = 0; i < updates.length; i++) {
                update = updates[i];
                for (j = 0; j < ocf.resources.length; j++) {
                    resource = ocf.resources[j];
                    if (Object.keys(update)[0] === resource.id.deviceId + resource.id.resourcePath) {
                        if (resource.onupdate !== undefined) {
                            resource.onupdate({updates: updates});
                        }
                    }
                }
            }
        }

        function errorCallback(error) {
            console.error(error);
        }

        exec(successCallback, errorCallback, "OcfPlugin", "getResourceUpdates", []);
    }, 2000);

    module.exports = ocf;
});
