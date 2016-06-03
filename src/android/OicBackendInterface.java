package com.intel.cordova.plugin.oic;

// Android
import android.content.Context;

// Cordova
import org.apache.cordova.CallbackContext;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public interface OicBackendInterface {
    public void findResources(JSONArray args, CallbackContext cc)
        throws JSONException;
    public void findDevices(CallbackContext cc) throws JSONException;
    public void updateResource(JSONArray args, CallbackContext cc)
        throws JSONException;
    public JSONObject getResourceUpdates() throws JSONException;
}
