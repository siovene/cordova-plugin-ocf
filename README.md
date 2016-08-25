# cordova-plugin-ocf

A Cordova plugin to expose the [OCF
specification](https://openconnectivity.org/resources/specifications) to cross
platform applications written in JavaScript.


## Purpose

To provide a plugin which allows using the OCF specification across all mobile
platforms without requiring the user to deal with implementing and compiling
the native code for each operating system.

Current platforms:
 * Android

## Building

As this is a Cordova plugin, you will build it as part of an app. For a demo
app, please see the [cordova-plugin-ocf-demo
app](https://github.com/siovene/cordova-plugin-ocf-demo).  Build instructions
are located at [its README
file](https://github.com/siovene/cordova-plugin-ocf-demo/blob/master/README.md).

## Running tests

This plugin contains a test suite that runs as a Cordova app. Pleae find it at
the [cordova-plugin-ocf-tests project
page](https://github.com/siovene/cordova-plugin-ocf-tests).

## Quick start with the API

```javascript
var plugin = cordova.require('cordova/plugin/ocf');

plugin.onresourcefound = function(event) {
    var resource = event.resource;
    // Do something with the resource.

    resource.onupdate = function(event) {
        var updates = event.updates;
        // Update your UI when changes have occurred.
    };
};

plugin.setBackend('iotivity').then(function() {
    plugin.findResources();
});
```

More details can be found at the [OCF JS spec for the Soletta
project](https://github.com/zolkis/soletta/blob/master/doc/js-spec/ocf.md).
