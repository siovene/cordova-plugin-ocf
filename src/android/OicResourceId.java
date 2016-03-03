package com.intel.cordova.plugin.oic;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OicResourceId implements OicObjectInterface {
    private String deviceId;
    private String resourcePath;

    public OicResourceId() {}

    public OicResourceId(String deviceId, String resourcePath) {
        this.deviceId = deviceId;
        this.resourcePath = resourcePath;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("deviceId", this.deviceId);
        o.put("resourcePath", this.resourcePath);

        return o;
    }

    public static OicResourceId fromJSON(JSONObject obj) throws JSONException {
        return new OicResourceId(
            obj.optString("deviceId"), obj.optString("resourcePath"));
    }
}
