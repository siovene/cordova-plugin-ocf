package com.intel.cordova.plugin.oic;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OicDeviceEvent implements OicObjectInterface {
    public OicDevice device;

    public OicDeviceEvent(OicDevice device) {
        this.device = device;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("device", this.device.toJSON());

        return o;
    }
}
