package com.intel.cordova.plugin.ocf;

// Cordova
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

// Android
import android.content.Context;
import android.util.Log;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OcfPlugin extends CordovaPlugin {
    static final String TAG = "OcfPlugin";
    private OcfBackendInterface backend;

    private void setBackend(JSONArray args)
        throws JSONException, OcfInvalidBackendException
    {
        String type = args.getString(0);
        if (type.equals("mock")) {
            this.backend = new OcfBackendMock(null);
        } else if (type.equals("iotivity")) {
            this.backend = new OcfBackendIotivity(this);
        } else {
            throw new OcfInvalidBackendException(type);
        }
    }

    private void findResources(final JSONArray args, final CallbackContext cc) {
        final OcfPlugin self = this;
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    self.backend.findResources(args, cc);
                } catch (JSONException e) {
                    cc.error("Error parsing arguments: " + e.getMessage());
                }
            }
        });
    }

    private void findDevices(final CallbackContext cc) {
        final OcfPlugin self = this;
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                self.backend.findDevices(cc);
            }
        });
    }

    private void updateResource(final JSONArray args, final CallbackContext cc) {
        final OcfPlugin self = this;
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    self.backend.updateResource(args, cc);
                } catch (JSONException e) {
                    cc.error("Error parsing arguments: " + e.getMessage());
                }
            }
        });
    }

    private JSONArray getResourceUpdates() throws JSONException {
        return this.backend.getResourceUpdates();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext cc) {
        Log.d(TAG, "Executing Cordova action: " + action);

        try {
            if ("setBackend".equals(action)) {
                try {
                    this.setBackend(args);
                    cc.success();
                } catch (OcfInvalidBackendException e) {
                    cc.error(e.getMessage());
                }
            } else if ("findResources".equals(action)) {
                this.findResources(args, cc);
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                result.setKeepCallback(true);
                cc.sendPluginResult(result);
            } else if ("findDevices".equals(action)) {
                this.findDevices(cc);
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                result.setKeepCallback(true);
                cc.sendPluginResult(result);
            } else if ("updateResource".equals(action)) {
                this.updateResource(args, cc);
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                result.setKeepCallback(true);
                cc.sendPluginResult(result);
            } else if ("getResourceUpdates".equals(action)) {
                JSONArray updates = this.getResourceUpdates();
                cc.success(updates);
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
