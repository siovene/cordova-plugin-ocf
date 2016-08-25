package com.intel.cordova.plugin.ocf;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OcfResourceId implements OcfObjectInterface {
    private String deviceId;
    private String resourcePath;

    public OcfResourceId() {}

    public OcfResourceId(String deviceId, String resourcePath) {
        this.deviceId = deviceId;
        this.resourcePath = resourcePath;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    public String getDeviceId() { return this.deviceId; }

    public String getResourcePath() { return this.resourcePath; }

    public String getUniqueKey() { return this.deviceId + this.resourcePath; }

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

    public static OcfResourceId fromJSON(JSONObject obj) throws JSONException {
        OcfResourceId id = new OcfResourceId();

        if (obj != null) {
            id.deviceId = obj.optString("deviceId");
            id.resourcePath = obj.optString("resourcePath");
        }

        return id;
    }
}
