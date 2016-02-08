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


public class OicBackendMock implements OicBackendInterface {
    public OicBackendMock(Context context) {
    }

    public void findResources(JSONArray args, CallbackContext cc)
            throws JSONException {
        String deviceId;
        String resourcePath;

        ArrayList<String> resourceTypes;
        JSONArray resourceTypesJson;
        resourceTypes = new ArrayList<String>();

        try {
            deviceId = args.getJSONObject(0).getString("deviceId");
            resourcePath = args.getJSONObject(0).getString("resourcePath");
            resourceTypesJson = args.getJSONObject(0).getJSONArray("resourceTypes");
            if (resourceTypesJson != null) {
                for (int i = 0; i < resourceTypesJson.length(); i++) {
                    resourceTypes.add(resourceTypesJson.getString(i));
                }
            }
        } catch (JSONException ex) {
            deviceId = "";
            resourcePath = "";
        }

        OicResource res = new OicResource(deviceId, resourcePath);
        res.setResourceTypes(resourceTypes);
        res.setInterfaces(new ArrayList<String>() {{
            add("iface1");
            add("iface2");
        }});
        res.setMediaTypes(new ArrayList<String>() {{
            add("mediaType1");
            add("mediaType2");
        }});

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
}
