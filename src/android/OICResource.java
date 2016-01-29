package com.intel.cordova.plugin.oic;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OICResource implements OICObjectInterface {
    public String deviceId;
    public String resourcePath;
    public String resourceType;

    public OICResource(String deviceId, String resourcePath, String resourceType) {
        this.deviceId = deviceId;
        this.resourcePath = resourcePath;
        this.resourceType = resourceType;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("deviceId", this.deviceId);
        o.put("resourcePath", this.resourcePath);
        o.put("resourceType", this.resourceType);

        return o;
    }
}
