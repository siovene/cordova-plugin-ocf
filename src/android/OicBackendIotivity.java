package com.intel.cordova.plugin.oic;

// Java
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.iotivity.base.ObserveType;
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
import org.json.JSONObject;


public class OicBackendIotivity
    implements OicBackendInterface,
               OcPlatform.OnDeviceFoundListener,
               OcPlatform.OnResourceFoundListener
{
    // Needed to associate OcResource.On{Get,Post̋}Listener instances to an
    // OicResource without placing Iotivity specific code in there.
    private static class OicResourceWrapper
        implements OcResource.OnGetListener, OcResource.OnPutListener,
                   OcResource.OnObserveListener
    {
        private OicBackendIotivity backend;
        private OcResource nativeResource;
        private OicResource oicResource;
        private boolean getFinished = false;
        private boolean putFinished = false;

        public OicResourceWrapper(
            OicBackendIotivity backend, OcResource nativeResource,
            OicResource oicResource)
        {
            this.backend = backend;
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
            Map<String, Object> values = ocRepresentation.getValues();
            for(Map.Entry<String, Object> entry: values.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                oicResource.setProperty(key, value);
            }
            this.getFinished = true;
        }

        @Override
        public synchronized void onGetFailed(java.lang.Throwable ex) {
            Log.e("CordovaPluginOIC", "onGetFailed");
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
            Log.e("CordovaPluginOIC", "onPutFailed");
            this.putFinished = true;
        }

        @Override
        public synchronized void onObserveCompleted(
               java.util.List<OcHeaderOption> headerOptionList,
               OcRepresentation ocRepresentation,
               int sequenceNumber)
        {
            Log.d("CordovaPluginOIC", "onObserveCompleted: " + this.oicResource.getId().getUniqueKey());
            if (this.backend != null) {
                this.backend.addResourceUpdate(this.oicResource, ocRepresentation);
            }
        }

        @Override
        public synchronized void onObserveFailed(java.lang.Throwable ex) {
            Log.e("CordovaPluginOIC", "onObserveFailed");
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
    private List<String> observedResources = new ArrayList<String>();
    private List<Map<OicResource, OicResourceRepresentation> > resourceUpdates =
        new ArrayList<Map<OicResource, OicResourceRepresentation> >();

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

    public void addResourceUpdate(OicResource resource, OcRepresentation repr)
    {
        Map<OicResource, OicResourceRepresentation> update =
            new HashMap<OicResource, OicResourceRepresentation>();

        update.put(resource, this.representationFromNative(repr));
        this.resourceUpdates.add(update);
    }

    // ------------------------------------------------------------------------
    // TODO: conversion functions should be in backend specific subclasses of
    // Oic* classes.
    // ------------------------------------------------------------------------

    private static OicResource resourceFromNative(OcResource nativeResource) {
        int elapsed = 0;
        final int timeout = 5000;
        final int sleepTime = 100;

        String deviceId = nativeResource.getHost();
        String resourcePath = nativeResource.getUri();

        OicResource oicResource = new OicResource(nativeResource.getHost(), nativeResource.getUri());
        oicResource.setResourceTypes(new ArrayList<String> (nativeResource.getResourceTypes()));
        oicResource.setInterfaces(new ArrayList<String> (nativeResource.getResourceInterfaces()));
        oicResource.setObservable(nativeResource.isObservable());

        OicResourceWrapper resourceWrapper = new OicResourceWrapper(null, nativeResource, oicResource);

        // Get all poperties
        Log.d("CordovaPluginOIC", "==========================================================");
        try {
            nativeResource.get(new HashMap<String, String>(), resourceWrapper);
            while(resourceWrapper.isGetFinished() == false && elapsed <= timeout) {
                Thread.sleep(sleepTime);
                elapsed += sleepTime;
            }
        } catch (OcException ex) {
            Log.e("CordovaPluginOIC", ex.toString());
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

        Log.d("CordovaPluginOIC", "creating native resource...");
        Log.d("CordovaPluginOIC", "url = " + url);
        Log.d("CordovaPluginOIC", "host = " + host);
        Log.d("CordovaPluginOIC", "resourceTypes size = " + resourceTypes.size());
        Log.d("CordovaPluginOIC", "interfaces size = " + interfaces.size());
        try {
            nativeResource = OcPlatform.constructResourceObject(
                oicResource.getId().getDeviceId(),
                oicResource.getId().getResourcePath(),
                EnumSet.of(OcConnectivityType.CT_DEFAULT),
                false,
                oicResource.getResourceTypes(),
                oicResource.getInterfaces());
        } catch (OcException ex) {
            Log.e("CordovaPluginOIC", ex.toString());
        }

        Log.d("CordovaPluginOIC", "returning native resource...");
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

                if (stringValue.toLowerCase().equals("true")) {
                    nativeRepr.setValue(key, true);
                    done = true;
                } else if (stringValue.toLowerCase().equals("false")) {
                    nativeRepr.setValue(key, false);
                    done = true;
                }

                try {
                    int i = Integer.parseInt(stringValue);
                    nativeRepr.setValue(key, i);
                    done = true;
                } catch(NumberFormatException ex) {
                    Log.w("CordovaPluginOIC", "Value is not an integer");
                }

                try {
                    double d = Double.parseDouble(stringValue);
                    nativeRepr.setValue(key, d);
                    done = true;
                } catch(NumberFormatException ex) {
                    Log.w("CordovaPluginOIC", "Value is not a double");
                }

                if (!done) {
                    nativeRepr.setValue(key, stringValue);
                }
            } catch(OcException ex) {
                Log.e("CordovaPluginOIC", ex.toString());
            }
        }

        return nativeRepr;
    }

    private static OicResourceRepresentation representationFromNative(
        OcRepresentation nativeRepr)
    {
        OicResourceRepresentation repr = new OicResourceRepresentation();
        Map<String, Object> values = nativeRepr.getValues();
        for (Map.Entry<String, Object> entry: values.entrySet()) {
            repr.setValue(entry.getKey(), entry.getValue());
        }
        return repr;
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
            Log.e("CordovaPluginOIC", "Error reading OcRepresentation");
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

    public void findDevices(CallbackContext cc) {
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

        Log.d("CordovaPluginOIC", "Found resource: " + key);

        if (resourcePath.equals("/oic/p") || resourcePath.equals("/oic/d")) {
            return;
        }


        OicResource oicResource = this.resourceFromNative(resource);
        OicResourceWrapper resourceWrapper = new OicResourceWrapper(this, resource, oicResource);

        if (resource.isObservable() && ! this.observedResources.contains(key)) {
            try {
                Log.d("CordovaPluginOIC", "Observing resource: " + key);
                resource.observe(ObserveType.OBSERVE, new HashMap<String, String>(), resourceWrapper);
                this.observedResources.add(key);
            } catch (OcException e) {
                Log.e("CordovaPluginOIC", "Unable to observe resoure");
            }
        }

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
        String deviceId = args.getJSONObject(0).optString("deviceId");
        String resourceType = args.getJSONObject(0).optString("resourceType");

        this.findResourcesCallbackContext = cc;

        try {
            OcPlatform.findResource(
                deviceId,
                OcPlatform.WELL_KNOWN_QUERY + "?rt=" + resourceType,
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
        OcResource nativeResource = OicBackendIotivity.resourceToNative(oicResource);
        OcRepresentation nativeRepr = this.representationToNative(
            oicResource.getProperties());
        OicResourceWrapper resourceWrapper = new OicResourceWrapper(
            this, nativeResource, oicResource);

        Log.d("CordovaPluginOIC", "Updating resource: " +  oicResource.toJSON().toString());

        try {
            nativeResource.put(
                nativeRepr, new HashMap<String, String>(), resourceWrapper);
            while(resourceWrapper.isPutFinished() == false && elapsed <= timeout) {
                Thread.sleep(sleepTime);
                elapsed += sleepTime;
            }
        } catch (OcException ex) {
            Log.e("CordovaPluginOIC", ex.toString());
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
