cordova.define("cordova/plugin/oic", function(require, exports, module) {
    var exec = require("cordova/exec");

    var OIC = function() {}

    OIC.prototype.OCInit = function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "OIC", "OCInit", []);
    };

    var oic = new OIC();
    module.exports = oic;
});
