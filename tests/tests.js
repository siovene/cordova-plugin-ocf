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

        it('findResources with options works', function(done) {
            var options = {
                deviceId: "127.0.0.1",
                resourcePath: "/",
                resourceTypes: ["test1"]
            };

            expect(oic.findResources).toBeDefined();
            oic.setBackend("mock").then(function() {
                oic.onresourcefound = function(event) {
                    expect(event).toBeDefined()
                    expect(event.resource).toBeDefined();
                    expect(event.resource.id.deviceId).toBe(options.deviceId);
                    expect(event.resource.id.resourcePath).toBe(options.resourcePath);
                    expect(event.resource.resourceTypes).toEqual(options.resourceTypes);
                    expect(event.resource.interfaces).toEqual(['iface1', 'iface2']);
                    expect(event.resource.mediaTypes).toEqual(['mediaType1', 'mediaType2']);
                    expect(event.resource.properties).toBeDefined();
                    expect(event.resource.properties.some_int).toBe(1);
                    expect(event.resource.properties.some_string).toBe("s");

                    expect(event.resource.observable).toBe(false);
                    done();
                }
                oic.findResources(options).then(function success() {
                    expect(true).toBe(true);
                });
            });
        });

        it('setting ondevicefound works', function() {
            expect(oic.ondevicefound).toBeDefined();
            oic.ondevicefound = function(event) { return "foo"; }
            expect(oic.ondevicefound()).toBe("foo");
        });

        it('findDevices works', function(done) {
            expect(oic.findDevices).toBeDefined();
            oic.setBackend("mock").then(function() {
                oic.ondevicefound = function(event) {
                    expect(event).toBeDefined();
                    expect(event.device).toBeDefined();
                    expect(event.device.uuid).toBe("1234567890");
                    expect(event.device.url).toBe("http://example.com/");
                    expect(event.device.name).toBe("Device name");
                    expect(event.device.dataModels).toEqual(['data1', 'data2']);
                    expect(event.device.coreSpecVersion).toBe("0.1.0");
                    expect(event.device.role).toBe("server");
                    done();
                }
                oic.findDevices().then(function success() {
                    expect(true).toBe(true);
                });
            });
        });

        it('update works', function(done) {
            expect(oic.update).toBeDefined();
            oic.setBackend("mock").then(function() {
                oic.onupdate = function(event) {
                    expect(event).toBeDefined();
                    expect(event.updates).toBeDefined();
                    expect(event.updates.id.deviceId).toBe("foo");
                    done();
                }
                oic.update({"id": {"deviceId": "foo"}}).then(function success() {
                    expect(true).toBe(true);
                });
            });
        });
    });
};
