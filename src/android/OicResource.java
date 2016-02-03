package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    private OcResource nativeResource;
    private boolean getFinished = true;

    private OicResourceId id;
    private ArrayList<String> resourceTypes;
    private ArrayList<String> interfaces;
    private ArrayList<String> mediaTypes;

    public OicResource(OcResource nativeResource) {
        this.buildFromNative(nativeResource);
    }

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

    public void buildFromNative(OcResource nativeResource) {
        String deviceId = nativeResource.getHost();
        String resourcePath = nativeResource.getUri();

        this.nativeResource = nativeResource;
        this.id = new OicResourceId(deviceId, resourcePath);
        this.setResourceTypes(new ArrayList<String> (nativeResource.getResourceTypes()));
        this.setInterfaces(new ArrayList<String> (nativeResource.getResourceInterfaces()));

        // Get all poperties
        try {
            this.getFinished = false;
            nativeResource.get(new HashMap<String, String>(), this);
        } catch (OcException ex) {
            Log.e("OIC", ex.toString());
        }
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
        Log.d("OIC", "onGetCompleted: headers size = "+ headerOptionList.size());
        for(OcHeaderOption option: headerOptionList) {
            Log.d("OIC", "Option: " + option.getOptionData());
        }
        this.getFinished = true;
    }

    @Override
    public synchronized void onGetFailed(java.lang.Throwable ex) {
        Log.e("OIC", "onGetFailed");
        this.getFinished = true;
    }
}
