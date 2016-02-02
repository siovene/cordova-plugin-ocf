package com.intel.cordova.plugin.oic;

class OicInvalidBackendException extends Exception {
    public OicInvalidBackendException(String backendType) {
        super("Invalid backend: " + backendType);
    }
}
