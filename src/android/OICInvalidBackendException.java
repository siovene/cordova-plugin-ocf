package com.intel.cordova.plugin.oic;

class OICInvalidBackendException extends Exception {
    public OICInvalidBackendException(String backendType) {
        super("Invalid backend: " + backendType);
    }
}
