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

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    public String getDeviceId() { return this.deviceId; }

    public String getResourcePath() { return this.resourcePath; }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("deviceId", this.deviceId);
        o.put("resourcePath", this.resourcePath);

        return o;
    }

    public static OicResourceId fromJSON(JSONObject obj) throws JSONException {
        OicResourceId id = new OicResourceId();

        if (obj != null) {
            id.deviceId = obj.optString("deviceId");
            id.resourcePath = obj.optString("resourcePath");
        }

        return id;
    }
}
