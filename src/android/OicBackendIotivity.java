package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.iotivity.base.OcHeaderOption;
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
    // Needed to associate OcResource.On{Get,Post̋}Listener instances to an
    // OicResource without placing Iotivity specific code in there.
    private static class OicResourceWrapper
        implements OcResource.OnGetListener, OcResource.OnPutListener
    {
        private OcResource nativeResource;
        private OicResource oicResource;
        private boolean getFinished = false;
        private boolean putFinished = false;

        public OicResourceWrapper(
            OcResource nativeResource, OicResource oicResource)
        {
            this.nativeResource = nativeResource;
            this.oicResource = oicResource;
        }

        public boolean isGetFinished() {
            return this.getFinished;
        }

        public boolean isPutFinished() {
            return this.putFinished;
        }

        @Override
        public synchronized void onGetCompleted(
            java.util.List<OcHeaderOption> headerOptionList,
            OcRepresentation ocRepresentation)
        {
            for(String key: ocRepresentation.getKeys()) {
                try {
                    Object value = ocRepresentation.getValue(key);
                    String type = value.getClass().getSimpleName();
                    oicResource.setProperty(key, value);
                } catch (OcException ex) {
                    Log.e("OIC", "Unable to retrieve key: " + key + ": " +
                          ex.toString());
                }
            }

            this.getFinished = true;
        }

        @Override
        public synchronized void onGetFailed(java.lang.Throwable ex) {
            Log.e("OIC", "onGetFailed");
            this.getFinished = true;
        }

        @Override
        public synchronized void onPutCompleted(
            java.util.List<OcHeaderOption> headerOptionList,
            OcRepresentation ocRepresentation)
        {
            this.putFinished = true;
        }

        @Override
        public synchronized void onPutFailed(java.lang.Throwable ex) {
            Log.e("OIC", "onPutFailed");
            this.putFinished = true;
        }
    }


    private CallbackContext findDevicesCallbackContext;
    private CallbackContext findResourcesCallbackContext;

    private static final String OC_RSRVD_DEVICE_ID = "di";
    private static final String OC_RSRVD_DEVICE_NAME = "n";
    private static final String OC_RSRVD_SPEC_VERSION = "lcv";
    private static final String OC_RSRVD_DATA_MODEL_VERSION = "dmv";

    private OicPlugin plugin;
    private CallbackContext callbackContext;
    private HashMap<String, OcResource> foundResources =
        new HashMap<String, OcResource>();

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

    // ------------------------------------------------------------------------
    // TODO: conversion functions should be in backend specific subclasses of
    // Oic* classes.
    // ------------------------------------------------------------------------

    private static OicResource buildResourceFromNative(OcResource nativeResource) {
        int elapsed = 0;
        final int timeout = 5000;
        final int sleepTime = 100;

        String deviceId = nativeResource.getHost();
        String resourcePath = nativeResource.getUri();

        OicResource oicResource = new OicResource(nativeResource.getHost(), nativeResource.getUri());
        oicResource.setResourceTypes(new ArrayList<String> (nativeResource.getResourceTypes()));
        oicResource.setInterfaces(new ArrayList<String> (nativeResource.getResourceInterfaces()));

        OicResourceWrapper resourceWrapper = new OicResourceWrapper(nativeResource, oicResource);

        // Get all poperties
        Log.d("OIC", "==========================================================");
        try {
            nativeResource.get(new HashMap<String, String>(), resourceWrapper);
            while(resourceWrapper.isGetFinished() == false && elapsed <= timeout) {
                Thread.sleep(sleepTime);
                elapsed += sleepTime;
            }
        } catch (OcException ex) {
            Log.e("OIC", ex.toString());
        } catch (InterruptedException ex) {
        }

        return oicResource;
    }

    private static OcResource resourceToNative(OicResource oicResource) {
        OcResource nativeResource = null;
        String url = oicResource.getId().getDeviceId();
        String host = oicResource.getId().getResourcePath();
        ArrayList<String> resourceTypes = oicResource.getResourceTypes();
        ArrayList<String> interfaces = oicResource.getInterfaces();

        Log.d("OIC", "creating native resource...");
        Log.d("OIC", "url = " + url);
        Log.d("OIC", "host = " + host);
        Log.d("OIC", "resourceTypes size = " + resourceTypes.size());
        Log.d("OIC", "interfaces size = " + interfaces.size());
        try {
            nativeResource = OcPlatform.constructResourceObject(
                oicResource.getId().getDeviceId(),
                oicResource.getId().getResourcePath(),
                EnumSet.of(OcConnectivityType.CT_DEFAULT),
                false,
                oicResource.getResourceTypes(),
                oicResource.getInterfaces());
        } catch (OcException ex) {
            Log.e("OIC", ex.toString());
        }

        Log.d("OIC", "returning native resource...");
        return nativeResource;
    }

    private static OcRepresentation representationToNative(
        OicResourceRepresentation repr)
    {
        OcRepresentation nativeRepr = new OcRepresentation();
        for (Map.Entry<String, Object> entry : repr.getProperties().entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            String type = value.getClass().getName();
            String stringValue = "" + value;

            // Parse value and support only primitive types for now.
            try {
                boolean done = false;

                try {
                    boolean b = Boolean.parseBoolean(stringValue);
                    nativeRepr.setValue(key, b);
                    done = true;
                } catch(NumberFormatException ex) {
                    Log.w("OIC", "Value is not a boolean");
                }

                try {
                    int i = Integer.parseInt(stringValue);
                    nativeRepr.setValue(key, i);
                    done = true;
                } catch(NumberFormatException ex) {
                    Log.w("OIC", "Value is not an integer");
                }

                try {
                    double d = Double.parseDouble(stringValue);
                    nativeRepr.setValue(key, d);
                    done = true;
                } catch(NumberFormatException ex) {
                    Log.w("OIC", "Value is not a double");
                }

                if (!done) {
                    nativeRepr.setValue(key, stringValue);
                }
            } catch(OcException ex) {
                Log.e("OIC", ex.toString());
            }
        }

        return nativeRepr;
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
        String host = resource.getHost();
        String resourcePath = resource.getUri();
        String key = host + resourcePath;

        Log.d("OIC", "Found resource: " + key);

        if (resourcePath.equals("/oic/p") || resourcePath.equals("/oic/d")) {
            return;
        }

        // Keep for later
        this.foundResources.put(key, resource);

        OicResource oicResource = this.buildResourceFromNative(resource);
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

    public void updateResource(JSONArray args, CallbackContext cc)
        throws JSONException
    {
        int elapsed = 0;
        final int timeout = 5000;
        final int sleepTime = 100;

        OicResource oicResource = OicResource.fromJSON(args.getJSONObject(0));
        OcResource nativeResource = this.foundResources.get(
            oicResource.getId().getDeviceId() + oicResource.getId().getResourcePath());
        if (nativeResource == null) {
            Log.d("OIC", "Unable to recycle known native resource");
            nativeResource = OicBackendIotivity.resourceToNative(oicResource);
        }

        OcRepresentation nativeRepr = this.representationToNative(
            oicResource.getProperties());
        OicResourceWrapper resourceWrapper = new OicResourceWrapper(
            nativeResource, oicResource);

        Log.d("OIC", "Updating resource: " +  oicResource.toJSON().toString());

        try {
            nativeResource.put(
                nativeRepr, new HashMap<String, String>(), resourceWrapper);
            while(resourceWrapper.isPutFinished() == false && elapsed <= timeout) {
                Thread.sleep(sleepTime);
                elapsed += sleepTime;
            }
        } catch (OcException ex) {
            Log.e("OIC", ex.toString());
        } catch (InterruptedException ex) {
        }

        PluginResult.Status status;
        if (resourceWrapper.isPutFinished()) {
            status = PluginResult.Status.OK;
        } else {
            status = PluginResult.Status.ERROR;
        }

        cc.sendPluginResult(new PluginResult(status));
    }
}
