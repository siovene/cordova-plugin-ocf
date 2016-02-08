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
import org.iotivity.base.OcResource;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;

// Third party
import org.json.JSONArray;
import org.json.JSONException;


public class OicBackendIotivity
    implements OicBackendInterface,
               OcPlatform.OnResourceFoundListener
{
    private CallbackContext callbackContext;

    public OicBackendIotivity(Context context) {
        PlatformConfig platformConfig = new PlatformConfig(
            context,
            ServiceType.IN_PROC,
            ModeType.CLIENT,
            "0.0.0.0", // By setting to "0.0.0.0", it binds to all available interfaces
            0,         // Uses randomly available port
            QualityOfService.LOW
        );
        OcPlatform.Configure(platformConfig);
    }

    public void onResourceFound(OcResource resource) {
        String deviceId = resource.getHost();
        String resourcePath = resource.getUri();

        OicResourceId id = new OicResourceId(deviceId, resourcePath);

        ArrayList<String> resourceTypes = new ArrayList<String> (resource.getResourceTypes());
        ArrayList<String> interfaces = new ArrayList<String> (resource.getResourceInterfaces());

        OicResource oicResource = new OicResource(id);
        oicResource.setResourceTypes(resourceTypes);
        oicResource.setInterfaces(interfaces);
        oicResource.setMediaTypes(new ArrayList<String>()); // Not implemented by Iotivity
        OicResourceEvent ev = new OicResourceEvent(oicResource);

        try {
            PluginResult result = new PluginResult(PluginResult.Status.OK, ev.toJSON());
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        } catch (JSONException ex) {
            this.callbackContext.error(ex.getMessage());
        }
    }

    public void findResources(JSONArray args, CallbackContext cc)
        throws JSONException
    {
        String host = args.getJSONObject(0).getString("deviceId");
        String resourceUri = args.getJSONObject(0).getString("resourcePath");

        this.callbackContext = cc;

        try {
            OcPlatform.findResource(
                host,
                OcPlatform.WELL_KNOWN_QUERY + "?rt=" + resourceUri,
                EnumSet.of(OcConnectivityType.CT_DEFAULT),
                this);
        } catch (OcException ex) {
            this.callbackContext.error(ex.getMessage());
        }
    }
}
