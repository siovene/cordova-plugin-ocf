package com.intel.cordova.plugin.oic;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OICResourceEvent implements OICObjectInterface {
    public OICResource resource;

    public OICResourceEvent(OICResource resource) {
        this.resource = resource;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("resource", this.resource.toJSON());

        return o;
    }
}
