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
        if (options === undefined) {
            options = {};
        }


        return new Promise(function(resolve, reject) {
            function successCallback(data) {
                resolve(data);
            }

            function errorCallback(error) {
                reject(error);
            }

            exec(successCallback, errorCallback, "OIC", "findResources",
                 [options]);
        });
    };

    var oic = new OIC();
    module.exports = oic;
});
