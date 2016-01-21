cordova.define("cordova/plugin/oic", function(require, exports, module) {
    var exec = require("cordova/exec"),
        channel = require("cordova/channel");

    channel.createSticky("onCordovaOicReady");
    channel.waitForInitialization("onCordovaOicReady");

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
        var self = this;

        self.backend = "iotivity";

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

    OIC.prototype.setBackend = function(backend) {
        if (backend === "iotivity" || backend === "mock")
            this.backend = backend;
        else
            throw new Error("Unknown backend");
    }

    OIC.prototype.__initDevice = function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "OIC", "__initDevice", []);
    };

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
