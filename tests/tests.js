exports.defineAutoTests = function() {
    var oic = cordova.require('cordova/plugin/oic'),
        channel = cordova.require('cordova/channel');

    describe('OIC Plugin test suite', function() {
        it('oic is defined', function() {
            expect(oic).toBeDefined();
        });

        it('oic has device', function(done) {
            channel.onCordovaOicReady.subscribe(function() {
                expect(oic.device).toBeDefined();
                done();
            });
        });

        it('oic.setBackend works', function() {
            expect(oic.setBackend).toBeDefined();
            expect(function() {oic.setBackend("foo");}).toThrow(
                new Error("Unknown backend"));

            oic.setBackend("mock");
            expect(oic.backend).toBe("mock");

            oic.setBackend("iotivity");
            expect(oic.backend).toBe("iotivity");
        });

        it('oic.findResources works', function(done) {
            var promise;

            expect(oic.findResources).toBeDefined();
            promise = oic.findResources();
            expect(typeof promise).toBe('object');
            promise.then(function success(data) {
                expect(data).toBeDefined();
                done();
            });
        });
    });
};
