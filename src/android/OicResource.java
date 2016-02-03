package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;
import java.util.Arrays;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OicResource implements OicObjectInterface {
    private OicResourceId id;
    private ArrayList<String> resourceTypes;
    private ArrayList<String> interfaces;

    public OicResource(OicResourceId id) {
        this.id = id;
    }

    public OicResource(String deviceId, String resourcePath) {
        this.id = new OicResourceId(deviceId, resourcePath);
    }

    public void setResourceTypes(ArrayList<String> resourceTypes) {
        this.resourceTypes = new ArrayList<String>(resourceTypes);
    }

    public void setInterfaces(ArrayList<String> interfaces) {
        this.interfaces = new ArrayList<String>(interfaces);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id.toJSON());
        o.put("resourceTypes", new JSONArray(this.resourceTypes));
        o.put("interfaces", new JSONArray(this.interfaces));

        return o;
    }
}
