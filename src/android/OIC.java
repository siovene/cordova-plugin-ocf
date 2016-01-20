package com.intel.cordova.plugin.oic;

// Cordova
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

// Android
import android.util.Log;

// Third party
import org.json.JSONArray;


public class OIC extends CordovaPlugin {
    static final String TAG = "OIC";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext cc) {
        Log.d(TAG, "Executing Cordova action: " + action);
        if ("__initDevice".equals(action)) {
            cc.success(1);
            return true;
        }

        Log.e(TAG, "Unknown action");
        return false; // MethodNotFound
    }
}
