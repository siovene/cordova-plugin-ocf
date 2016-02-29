package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;
import java.util.Arrays;

// Android
import android.util.Log;

// Iotivity
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OicResource
    implements OicObjectInterface, OcResource.OnGetListener
{
    private OicResourceId id;
    private ArrayList<String> resourceTypes;
    private ArrayList<String> interfaces;
    private ArrayList<String> mediaTypes;

    public OicResource(OicResourceId id) {
        this.id = id;
    }

    public OicResource(String deviceId, String resourcePath) {
        this.id = new OicResourceId(deviceId, resourcePath);
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

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id.toJSON());
        o.put("resourceTypes", new JSONArray(this.resourceTypes));
        o.put("interfaces", new JSONArray(this.interfaces));
        o.put("mediaTypes", new JSONArray(this.mediaTypes));

        return o;
    }

    @Override
    public synchronized void onGetCompleted(java.util.List<OcHeaderOption> headerOptionList,
        OcRepresentation ocRepresentation)
    {
        for(String key: ocRepresentation.getKeys()) {
            Log.d("OIC", "Key found: " + key);
        }
    }

    @Override
    public synchronized void onGetFailed(java.lang.Throwable ex) {
        Log.e("OIC", "onGetFailed");
    }
}
