package com.intel.cordova.plugin.oic;

// Cordova
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

// Android
import android.util.Log;

// Third party
import org.json.JSONArray;


public class OICBackendMock implements OICBackendInterface {
    public void findResources(JSONArray args, CallbackContext cc) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "fake event");
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }
}
