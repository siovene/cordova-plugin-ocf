package com.intel.cordova.plugin.oic;

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


public class OicBackendMock implements OicBackendInterface {
    private List<Map<OicResource, OicResourceRepresentation> > resourceUpdates =
        new ArrayList<Map<OicResource, OicResourceRepresentation> >();

    public OicBackendMock(OicPlugin plugin) {
    }

    public void findResources(JSONArray args, CallbackContext cc)
            throws JSONException
    {
        final JSONObject obj = args.getJSONObject(0);
        OicResource res = new OicResource(
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

        OicResourceEvent ev = new OicResourceEvent(res);
        PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }

    public void findDevices(CallbackContext cc) {
        OicDevice device = new OicDevice();
        device.setUuid("1234567890");
        device.setUrl("http://example.com/");
        device.setName("Device name");
        device.setDataModels(new ArrayList<String>() {{
            add("data1");
            add("data2");
        }});
        device.setCoreSpecVersion("0.1.0");
        device.setRole("server");

        OicDeviceEvent ev = new OicDeviceEvent(device);
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
        OicResource resource = OicResource.fromJSON(args.getJSONObject(0));
        OicResourceRepresentation repr = resource.getProperties();

        Map<OicResource, OicResourceRepresentation> update =
            new HashMap<OicResource, OicResourceRepresentation>();
        update.put(resource, repr);
        this.resourceUpdates.add(update);

        OicResourceUpdateEvent ev = new OicResourceUpdateEvent(resource);
        PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }

    public JSONArray getResourceUpdates() throws JSONException {
        JSONArray updates = new JSONArray();
        for(Map<OicResource, OicResourceRepresentation> map: this.resourceUpdates) {
            for(Map.Entry<OicResource, OicResourceRepresentation> entry: map.entrySet()) {
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
