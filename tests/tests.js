exports.defineAutoTests = function() {
    var oic = cordova.require('cordova/plugin/oic'),
        channel = cordova.require('cordova/channel');

    describe('OIC Plugin test suite', function() {
        it('oic is defined', function() {
            expect(oic).toBeDefined();
        });

        it('setting invalid backend works', function(done) {
            expect(oic.setBackend).toBeDefined();
            oic.setBackend("foo").then(function() {
                done(new Error("Promise should not be resolved"));
            }, function() {
                // Promise is rejected
                done();
            });
        });

        it('setting valid backend works', function(done) {
            expect(oic.setBackend).toBeDefined();
            oic.setBackend("mock").then(function() {
                done();
            }, function() {
                // Promise is rejected
                done(new Error("Promise should be resolved"));
            });
        });

        it('setting onresourcefound works', function() {
            expect(oic.onresourcefound).toBeDefined();
            oic.onresourcefound = function(event) { return "foo"; }
            expect(oic.onresourcefound()).toBe("foo");
        });

        it('findResources works', function(done) {
            expect(oic.findResources).toBeDefined();
            oic.setBackend("mock").then(function() {
                oic.onresourcefound = function(event) {
                    done();
                }
                oic.findResources().then(function success() {
                    expect(true).toBe(true);
                });
            });
        });
    });
};
