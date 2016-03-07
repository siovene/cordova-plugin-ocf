package com.intel.cordova.plugin.oic;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OicResourceUpdateEvent implements OicObjectInterface {
    public OicResource updates;

    public OicResourceUpdateEvent(OicResource updates) {
        this.updates = updates;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("updates", this.updates.toJSON());

        return o;
    }
}
