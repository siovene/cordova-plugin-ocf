package com.intel.cordova.plugin.oic;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OICResourceId implements OICObjectInterface {
    public String deviceId;
    public String resourcePath;

    public OICResourceId(String deviceId, String resourcePath) {
        this.deviceId = deviceId;
        this.resourcePath = resourcePath;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("deviceId", this.deviceId);
        o.put("resourcePath", this.resourcePath);

        return o;
    }
}
