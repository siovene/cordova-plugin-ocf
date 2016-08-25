package com.intel.cordova.plugin.ocf;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OcfResourceUpdateEvent implements OcfObjectInterface {
    public OcfResource updates;

    public OcfResourceUpdateEvent(OcfResource updates) {
        this.updates = updates;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("updates", this.updates.toJSON());

        return o;
    }
}
