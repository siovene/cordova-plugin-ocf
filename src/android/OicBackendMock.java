package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;

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

        OicResourceEvent ev = new OicResourceEvent(res);
        PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }

    public void findDevices(CallbackContext cc) throws JSONException {
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
        PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }

    public void updateResource(JSONArray args, CallbackContext cc)
        throws JSONException
    {
        Log.d("OIC", args.toString());
        OicResource updates = OicResource.fromJSON(args.getJSONObject(0));
        OicResourceUpdateEvent ev = new OicResourceUpdateEvent(updates);
        PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
        Log.d("OIC", ev.toJSON().toString());
        result.setKeepCallback(true);
        cc.sendPluginResult(result);
    }
}
