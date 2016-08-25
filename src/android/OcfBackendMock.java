package com.intel.cordova.plugin.ocf;

// Java
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Cordova
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

// Android
import android.content.Context;
import android.util.Log;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OcfBackendMock implements OcfBackendInterface {
    private List<Map<OcfResource, OcfResourceRepresentation> > resourceUpdates =
        new ArrayList<Map<OcfResource, OcfResourceRepresentation> >();

    public OcfBackendMock(OcfPlugin plugin) {
    }

    public void findResources(JSONArray args, CallbackContext cc)
            throws JSONException
    {
        final JSONObject obj = args.getJSONObject(0);
        OcfResource res = new OcfResource(
            obj.optString("deviceId"), obj.optString("resourcePath"));

        res.setResourceTypes(new ArrayList<String>() {{
            JSONArray a = obj.optJSONArray("resourceTypes");
            if (a != null) {
                for (int i = 0; i < a.length(); i++) {
                    add(a.getString(i));
                }
            }
        }});
        res.setInterfaces(new ArrayList<String>() {{
            add("iface1");
            add("iface2");
        }});
        res.setMediaTypes(new ArrayList<String>() {{
            add("mediaType1");
            add("mediaType2");
        }});

        res.setProperty("some_int", 1);
        res.setProperty("some_string", "s");

        res.setObservable(false);

        OcfResourceEvent ev = new OcfResourceEvent(res);
        PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }

    public void findDevices(CallbackContext cc) {
        OcfDevice device = new OcfDevice();
        device.setUuid("1234567890");
        device.setUrl("http://example.com/");
        device.setName("Device name");
        device.setDataModels(new ArrayList<String>() {{
            add("data1");
            add("data2");
        }});
        device.setCoreSpecVersion("0.1.0");
        device.setRole("server");

        OcfDeviceEvent ev = new OcfDeviceEvent(device);
        try {
            PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
            result.setKeepCallback(true);
            cc.sendPluginResult(result);
        } catch (JSONException e) {
            cc.error("Internal error: " + e.getMessage());
        }
    }

    public void updateResource(JSONArray args, CallbackContext cc)
        throws JSONException
    {
        OcfResource resource = OcfResource.fromJSON(args.getJSONObject(0));
        OcfResourceRepresentation repr = resource.getProperties();

        Map<OcfResource, OcfResourceRepresentation> update =
            new HashMap<OcfResource, OcfResourceRepresentation>();
        update.put(resource, repr);
        this.resourceUpdates.add(update);

        OcfResourceUpdateEvent ev = new OcfResourceUpdateEvent(resource);
        PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }

    public JSONArray getResourceUpdates() throws JSONException {
        JSONArray updates = new JSONArray();
        for(Map<OcfResource, OcfResourceRepresentation> map: this.resourceUpdates) {
            for(Map.Entry<OcfResource, OcfResourceRepresentation> entry: map.entrySet()) {
                JSONObject obj = new JSONObject();
                obj.put(
                    entry.getKey().getId().getUniqueKey(),
                    entry.getValue().toJSON());
                updates.put(obj);
            }
        }

        this.resourceUpdates.clear();
        return updates;
    }
}
