package com.intel.cordova.plugin.ocf;

class OcfInvalidBackendException extends Exception {
    public OcfInvalidBackendException(String backendType) {
        super("Invalid backend: " + backendType);
    }
}
