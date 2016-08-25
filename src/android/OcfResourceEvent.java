package com.intel.cordova.plugin.ocf;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OcfResourceEvent implements OcfObjectInterface {
    public OcfResource resource;

    public OcfResourceEvent(OcfResource resource) {
        this.resource = resource;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("resource", this.resource.toJSON());

        return o;
    }
}
