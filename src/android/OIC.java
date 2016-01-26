package com.intel.cordova.plugin.oic;

// Cordova
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

// Android
import android.util.Log;

// Third party
import org.json.JSONArray;
import org.json.JSONException;


public class OIC extends CordovaPlugin {
    static final String TAG = "OIC";
    private OICBackendInterface backend = new OICBackendMock();

    private void setBackend(JSONArray args)
        throws JSONException, OICInvalidBackendException
    {
        String type = args.getString(0);
        if (type.equals("mock")) {
            this.backend = new OICBackendMock();
        } else {
            throw new OICInvalidBackendException(type);
        }
    }

    private void findResources(JSONArray args, CallbackContext cc) {
        this.backend.findResources(args, cc);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext cc) {
        Log.d(TAG, "Executing Cordova action: " + action);

        try {
            if ("setBackend".equals(action)) {
                try {
                    this.setBackend(args);
                    cc.success();
                } catch (OICInvalidBackendException e) {
                    cc.error(e.getMessage());
                }
            } else if ("findResources".equals(action)) {
                this.findResources(args, cc);
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                cc.sendPluginResult(result);
            } else {
                Log.e(TAG, "Unknown action: " + action);
                cc.error("Unknown action: " + action);
                return false;
            }
        } catch (JSONException e) {
            cc.error("Error parsing arguments: " + e.getMessage());
        }

        return true;
    }
}
