exports.defineAutoTests = function() {
    var ocf = cordova.require('cordova/plugin/ocf'),
        channel = cordova.require('cordova/channel');

    describe('OCF Plugin test suite', function() {
        it('ocf is defined', function() {
            expect(ocf).toBeDefined();
        });

        it('setting invalid backend works', function(done) {
            expect(ocf.setBackend).toBeDefined();
            ocf.setBackend("foo").then(function() {
                done(new Error("Promise should not be resolved"));
            }, function() {
                // Promise is rejected
                done();
            });
        });

        it('setting valid backend works', function(done) {
            expect(ocf.setBackend).toBeDefined();
            ocf.setBackend("mock").then(function() {
                done();
            }, function() {
                // Promise is rejected
                done(new Error("Promise should be resolved"));
            });
        });

        it('setting onresourcefound works', function() {
            expect(ocf.onresourcefound).toBeDefined();
            ocf.onresourcefound = function(event) { return "foo"; }
            expect(ocf.onresourcefound()).toBe("foo");
        });

        it('findResources works', function(done) {
            ocf.resources = [];
            expect(ocf.findResources).toBeDefined();
            ocf.setBackend("mock").then(function() {
                ocf.onresourcefound = function(event) {
                    done();
                }
                ocf.findResources().then(function success() {
                    expect(true).toBe(true);
                });
            });
        });

        it('findResources with options works', function(done) {
            var options = {
                deviceId: "127.0.0.1",
                resourcePath: "/findResources-test",
                resourceTypes: ["test1"]
            };

            ocf.resources = [];
            expect(ocf.findResources).toBeDefined();
            ocf.setBackend("mock").then(function() {
                ocf.onresourcefound = function(event) {
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
                ocf.findResources(options).then(function success() {
                    expect(true).toBe(true);
                });
            });
        });

        it('setting ondevicefound works', function() {
            expect(ocf.ondevicefound).toBeDefined();
            ocf.ondevicefound = function(event) { return "foo"; }
            expect(ocf.ondevicefound()).toBe("foo");
        });

        it('findDevices works', function(done) {
            expect(ocf.findDevices).toBeDefined();
            ocf.setBackend("mock").then(function() {
                ocf.ondevicefound = function(event) {
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
                ocf.findDevices().then(function success() {
                    expect(true).toBe(true);
                });
            });
        });

        it('update works', function(done) {
            ocf.resources = [];
            ocf.setBackend("mock").then(function() {
                ocf.onresourcefound = function(event) {
                    event.resource.onupdate = function(event) {
                        expect(event.updates).toBeDefined();
                        expect(event.updates.length).toBe(1);
                        expect(event.updates[0]["127.0.0.1/update-test"].foo).toBe("bar");
                        done();
                    };

                    ocf.update({
                        "id": {
                            "deviceId": "127.0.0.1",
                            "resourcePath": "/update-test"
                        },
                        "properties": {
                            "foo": "bar"
                        }
                    }).then(function success(event) {
                        expect(true).toBe(true);
                    });
                };

                ocf.findResources({
                    deviceId: "127.0.0.1",
                    resourcePath: "/update-test",
                    resourceTypes: ["test1"]
                });
            });
        });
    });
};
