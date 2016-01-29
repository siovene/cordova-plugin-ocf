package com.intel.cordova.plugin.oic;

// Cordova
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

// Android
import android.util.Log;

// Third party
import org.json.JSONArray;
import org.json.JSONException;


public class OICBackendMock implements OICBackendInterface {
    public void findResources(JSONArray args, CallbackContext cc)
            throws JSONException {
        String deviceId;
        String resourcePath;
        String resourceType;

        try {
            deviceId = args.getJSONObject(0).getString("deviceId");
            resourcePath = args.getJSONObject(0).getString("resourcePath");
            resourceType = args.getJSONObject(0).getString("resourceType");
        } catch (JSONException ex) {
            deviceId = "";
            resourcePath = "";
            resourceType = "";
        }

        // Create dummy resource event
        OICResource res = new OICResource(deviceId, resourcePath, resourceType);
        OICResourceEvent ev = new OICResourceEvent(res);
        PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }
}
