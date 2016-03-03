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
    public OicBackendMock(OicPlugin plugin) {
    }

    public void findResources(JSONArray args, CallbackContext cc)
            throws JSONException {
        OicResource res = OicResource.fromJSON(args.getJSONObject(0));

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
}
