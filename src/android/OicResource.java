package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;
import java.util.Arrays;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OicResource implements OicObjectInterface {
    public OicResourceId id;
    public ArrayList<String> resourceTypes;

    public OicResource(OicResourceId id, ArrayList<String> resourceTypes) {
        this.id = id;
        this.resourceTypes = new ArrayList<String>(resourceTypes);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id.toJSON());
        o.put("resourceTypes", new JSONArray(this.resourceTypes));

        return o;
    }
}
