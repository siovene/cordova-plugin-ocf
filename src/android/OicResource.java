package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;
import java.util.Arrays;

// Android
import android.util.Log;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OicResource implements OicObjectInterface
{
    private OicResourceId id;
    private ArrayList<String> resourceTypes;
    private ArrayList<String> interfaces;
    private ArrayList<String> mediaTypes;
    private OicResourceRepresentation properties;

    public OicResource(OicResourceId id) {
        this.id = id;
        this.properties = new OicResourceRepresentation();
    }

    public OicResource(String deviceId, String resourcePath) {
        this.id = new OicResourceId(deviceId, resourcePath);
        this.properties = new OicResourceRepresentation();
    }

    public void finalize() {
        Log.d("OIC", "Resource finalized");
    }

    public void setResourceTypes(ArrayList<String> resourceTypes) {
        this.resourceTypes = new ArrayList<String>(resourceTypes);
    }

    public void setInterfaces(ArrayList<String> interfaces) {
        this.interfaces = new ArrayList<String>(interfaces);
    }

    public void setMediaTypes(ArrayList<String> mediaTypes) {
        this.mediaTypes = new ArrayList<String>(mediaTypes);
    }

    public void setProperty(String key, Object value) {
        this.properties.setValue(key, value);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id.toJSON());
        o.put("resourceTypes", new JSONArray(this.resourceTypes));
        o.put("interfaces", new JSONArray(this.interfaces));
        o.put("mediaTypes", new JSONArray(this.mediaTypes));
        o.put("properties", this.properties.toJSON());

        return o;
    }
}
