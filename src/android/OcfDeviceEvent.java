package com.intel.cordova.plugin.ocf;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OcfDeviceEvent implements OcfObjectInterface {
    public OcfDevice device;

    public OcfDeviceEvent(OcfDevice device) {
        this.device = device;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("device", this.device.toJSON());

        return o;
    }
}
