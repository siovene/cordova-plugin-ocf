package com.intel.cordova.plugin.ocf;

// Java
import java.util.ArrayList;
import java.util.Arrays;

// Android
import android.util.Log;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OcfResource implements OcfObjectInterface
{
    private OcfResourceId id;
    private ArrayList<String> resourceTypes;
    private ArrayList<String> interfaces;
    private ArrayList<String> mediaTypes;
    private OcfResourceRepresentation properties;
    private boolean observable;

    public OcfResource() {
        this.id  = new OcfResourceId();
        this.resourceTypes = new ArrayList<String>();
        this.interfaces = new ArrayList<String>();
        this.mediaTypes = new ArrayList<String>();
        this.properties = new OcfResourceRepresentation();
        this.observable = false;
    }

    public OcfResource(OcfResourceId id) {
        this.id = id;
        this.properties = new OcfResourceRepresentation();
    }

    public OcfResource(String deviceId, String resourcePath) {
        this.id = new OcfResourceId(deviceId, resourcePath);
        this.properties = new OcfResourceRepresentation();
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    public OcfResourceId getId() { return this.id; }

    public ArrayList<String> getResourceTypes() { return this.resourceTypes; }

    public ArrayList<String> getInterfaces() { return this.interfaces; }

    public ArrayList<String> getMediaTypes() { return this.mediaTypes; }

    public OcfResourceRepresentation getProperties() { return this.properties; }

    public boolean getObservable() { return this.observable; }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

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

    public void setObservable(boolean value) {
        this.observable = value;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id.toJSON());
        o.put("resourceTypes", new JSONArray(this.resourceTypes));
        o.put("interfaces", new JSONArray(this.interfaces));
        o.put("mediaTypes", new JSONArray(this.mediaTypes));
        o.put("properties", this.properties.toJSON());
        o.put("observable", this.observable);

        return o;
    }

    public static OcfResource fromJSON(JSONObject obj) throws JSONException {
        Log.d("OCF", obj.toString());
        OcfResource resource = new OcfResource();

        resource.id = OcfResourceId.fromJSON(obj.optJSONObject("id"));

        JSONArray resourceTypesJson = obj.optJSONArray("resourceTypes");
        if (resourceTypesJson != null) {
            resource.resourceTypes = new ArrayList<String>();
            for (int i = 0; i < resourceTypesJson.length(); i++) {
                resource.resourceTypes.add(resourceTypesJson.getString(i));
            }
        }

        JSONArray interfacesJson = obj.optJSONArray("interfaces");
        if (interfacesJson != null) {
            resource.interfaces = new ArrayList<String>();
            for (int i = 0; i < interfacesJson.length(); i++) {
                resource.interfaces.add(interfacesJson.getString(i));
            }
        }

        JSONArray mediaTypesJson = obj.optJSONArray("mediaTypes");
        if (mediaTypesJson != null) {
            resource.mediaTypes = new ArrayList<String>();
            for (int i = 0; i < mediaTypesJson.length(); i++) {
                resource.mediaTypes.add(mediaTypesJson.getString(i));
            }
        }

        JSONObject propertiesJson = obj.optJSONObject("properties");
        if (propertiesJson != null) {
            resource.properties = OcfResourceRepresentation.fromJSON(
                propertiesJson);
        }

        resource.observable = obj.optBoolean("observable");

        return resource;
    }
}
