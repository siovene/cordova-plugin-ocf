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
    });
};
