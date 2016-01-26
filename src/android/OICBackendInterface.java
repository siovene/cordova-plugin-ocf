package com.intel.cordova.plugin.oic;

// Cordova
import org.apache.cordova.CallbackContext;

// Third party
import org.json.JSONArray;
import org.json.JSONException;


public interface OICBackendInterface {
    public void findResources(JSONArray args, CallbackContext cc)
        throws JSONException;
}
