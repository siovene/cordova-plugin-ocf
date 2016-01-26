package com.intel.cordova.plugin.oic;

// Cordova
import org.apache.cordova.CallbackContext;

// Third party
import org.json.JSONArray;


public interface OICBackendInterface {
    public void findResources(JSONArray args, CallbackContext cc);
}
