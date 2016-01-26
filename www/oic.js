cordova.define("cordova/plugin/oic", function(require, exports, module) {
    var exec = require("cordova/exec");

    /**************************************************************************
    *  OICBackend                                                             *
    *  Base class for backend implementations.                                *
    **************************************************************************/
    function OICBackend(type) {
        this.type = type;
    };


    /**************************************************************************
    *  OICBackendMock                                                         *
    *  Backend implementation that mocks things, for unit testing.            *
    **************************************************************************/
    OICBackendMock.prototype = new OICBackend();
    OICBackendMock.prototype.constructor = OICBackendMock;
    function OICBackendMock() {
        this.type = "mock";
    }


    /**************************************************************************
    *  OIC                                                                    *
    *  The Cordova plugin.                                                    *
    **************************************************************************/
    var OIC = function() {
        this.backend = "iotivity";
    }

    OIC.prototype.setBackend = function(backend) {
        var self = this;

        return new Promise(function(resolve, reject) {
            function successCallback() {
                self.backend = backend;
                resolve();
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OIC", "setBackend",
                 [backend]);
        });
    }

    OIC.prototype.findResources = function(options) {
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

            exec(successCallback, errorCallback, "OIC", "findResources",
                 [options]);
        });
    };

    OIC.prototype.onresourcefound = function(event) {};

    var oic = new OIC();
    module.exports = oic;
});
