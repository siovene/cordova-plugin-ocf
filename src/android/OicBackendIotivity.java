package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// Cordova
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

// Android
import android.content.Context;
import android.util.Log;

// Iotivity
import org.iotivity.base.ModeType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;

// Third party
import org.json.JSONArray;
import org.json.JSONException;


public class OicBackendIotivity
    implements OicBackendInterface,
               OcPlatform.OnDeviceFoundListener,
               OcPlatform.OnResourceFoundListener
{
    private CallbackContext findDevicesCallbackContext;
    private CallbackContext findResourcesCallbackContext;

    private static final String OC_RSRVD_DEVICE_ID = "di";
    private static final String OC_RSRVD_DEVICE_NAME = "n";
    private static final String OC_RSRVD_SPEC_VERSION = "lcv";
    private static final String OC_RSRVD_DATA_MODEL_VERSION = "dmv";

    private OicPlugin plugin;
    private CallbackContext callbackContext;

    public OicBackendIotivity(OicPlugin plugin) {
        this.plugin = plugin;

        PlatformConfig platformConfig = new PlatformConfig(
            plugin.cordova.getActivity().getApplicationContext(),
            ServiceType.IN_PROC,
            ModeType.CLIENT,
            "0.0.0.0", // By setting to "0.0.0.0", it binds to all available interfaces
            0,         // Uses randomly available port
            QualityOfService.LOW
        );
        OcPlatform.Configure(platformConfig);
    }

    @Override
    public void onDeviceFound(final OcRepresentation repr) {
        OicDevice device = new OicDevice();
        try {
            device.setUuid((String) repr.getValue(OC_RSRVD_DEVICE_ID));
            device.setName((String) repr.getValue(OC_RSRVD_DEVICE_NAME));
            device.setCoreSpecVersion((String) repr.getValue(OC_RSRVD_SPEC_VERSION));
            device.setDataModels(new ArrayList<String>() {{
                add((String) repr.getValue(OC_RSRVD_DATA_MODEL_VERSION));
            }});
        } catch (OcException ex) {
            Log.e("OIC", "Error reading OcRepresentation");
        }

        OicDeviceEvent ev = new OicDeviceEvent(device);
        try {
            PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
            result.setKeepCallback(true);
            this.findDevicesCallbackContext.sendPluginResult(result);
        } catch (JSONException ex) {
            this.findDevicesCallbackContext.error(ex.getMessage());
        }
    }

    public void findDevices(CallbackContext cc) throws JSONException {
        this.findDevicesCallbackContext = cc;
        try {
            OcPlatform.getDeviceInfo(
                "", "/oic/d", EnumSet.of(OcConnectivityType.CT_DEFAULT), this);
        } catch (OcException ex) {
            this.findDevicesCallbackContext.error(ex.getMessage());
        }
    }

    @Override
    public synchronized void onResourceFound(OcResource resource) {
        String resourcePath = resource.getUri();
        if (resourcePath.equals("/oic/p") || resourcePath.equals("/oic/d")) {
            return;
        }

        OicResource oicResource = new OicResource(resource);
        OicResourceEvent ev = new OicResourceEvent(oicResource);

        try {
            PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
            result.setKeepCallback(true);
            this.findResourcesCallbackContext.sendPluginResult(result);
        } catch (JSONException ex) {
            this.findResourcesCallbackContext.error(ex.getMessage());
        }
    }

    public void findResources(JSONArray args, CallbackContext cc)
        throws JSONException
    {
        String host = args.getJSONObject(0).getString("deviceId");
        String resourceUri = args.getJSONObject(0).getString("resourcePath");

        this.findResourcesCallbackContext = cc;

        try {
            OcPlatform.findResource(
                host,
                OcPlatform.WELL_KNOWN_QUERY + "?rt=" + resourceUri,
                EnumSet.of(OcConnectivityType.CT_DEFAULT),
                this);
        } catch (OcException ex) {
            this.findResourcesCallbackContext.error(ex.getMessage());
        }
    }
}
