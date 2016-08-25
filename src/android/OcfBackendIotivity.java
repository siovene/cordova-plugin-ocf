package com.intel.cordova.plugin.ocf;

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


public class OcfBackendIotivity
    implements OcfBackendInterface,
               OcPlatform.OnDeviceFoundListener,
               OcPlatform.OnResourceFoundListener
{
    // Needed to associate OcResource.On{Get,Post̋}Listener instances to an
    // OcfResource without placing Iotivity specific code in there.
    private static class OcfResourceWrapper
        implements OcResource.OnGetListener, OcResource.OnPutListener,
                   OcResource.OnObserveListener
    {
        private OcfBackendIotivity backend;
        private OcResource nativeResource;
        private OcfResource ocfResource;
        private boolean getFinished = false;
        private boolean putFinished = false;

        public OcfResourceWrapper(
            OcfBackendIotivity backend, OcResource nativeResource,
            OcfResource ocfResource)
        {
            this.backend = backend;
            this.nativeResource = nativeResource;
            this.ocfResource = ocfResource;
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
                ocfResource.setProperty(key, value);
            }
            this.getFinished = true;
        }

        @Override
        public synchronized void onGetFailed(java.lang.Throwable ex) {
            Log.e("CordovaPluginOCF", "onGetFailed");
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
            Log.e("CordovaPluginOCF", "onPutFailed");
            this.putFinished = true;
        }

        @Override
        public synchronized void onObserveCompleted(
               java.util.List<OcHeaderOption> headerOptionList,
               OcRepresentation ocRepresentation,
               int sequenceNumber)
        {
            Log.d("CordovaPluginOCF", "onObserveCompleted: " + this.ocfResource.getId().getUniqueKey());
            if (this.backend != null) {
                this.backend.addResourceUpdate(this.ocfResource, ocRepresentation);
            }
        }

        @Override
        public synchronized void onObserveFailed(java.lang.Throwable ex) {
            Log.e("CordovaPluginOCF", "onObserveFailed");
        }
    }


    private CallbackContext findDevicesCallbackContext;
    private CallbackContext findResourcesCallbackContext;

    private static final String OC_RSRVD_DEVICE_ID = "di";
    private static final String OC_RSRVD_DEVICE_NAME = "n";
    private static final String OC_RSRVD_SPEC_VERSION = "lcv";
    private static final String OC_RSRVD_DATA_MODEL_VERSION = "dmv";

    private OcfPlugin plugin;
    private CallbackContext callbackContext;
    private List<String> observedResources = new ArrayList<String>();
    private List<Map<OcfResource, OcfResourceRepresentation> > resourceUpdates =
        new ArrayList<Map<OcfResource, OcfResourceRepresentation> >();

    public OcfBackendIotivity(OcfPlugin plugin) {
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

    public void addResourceUpdate(OcfResource resource, OcRepresentation repr)
    {
        Map<OcfResource, OcfResourceRepresentation> update =
            new HashMap<OcfResource, OcfResourceRepresentation>();

        update.put(resource, this.representationFromNative(repr));
        this.resourceUpdates.add(update);
    }

    // ------------------------------------------------------------------------
    // TODO: conversion functions should be in backend specific subclasses of
    // Ocf* classes.
    // ------------------------------------------------------------------------

    private static OcfResource resourceFromNative(OcResource nativeResource) {
        int elapsed = 0;
        final int timeout = 5000;
        final int sleepTime = 100;

        String deviceId = nativeResource.getHost();
        String resourcePath = nativeResource.getUri();

        OcfResource ocfResource = new OcfResource(nativeResource.getHost(), nativeResource.getUri());
        ocfResource.setResourceTypes(new ArrayList<String> (nativeResource.getResourceTypes()));
        ocfResource.setInterfaces(new ArrayList<String> (nativeResource.getResourceInterfaces()));
        ocfResource.setObservable(nativeResource.isObservable());

        OcfResourceWrapper resourceWrapper = new OcfResourceWrapper(null, nativeResource, ocfResource);

        // Get all poperties
        Log.d("CordovaPluginOCF", "==========================================================");
        try {
            nativeResource.get(new HashMap<String, String>(), resourceWrapper);
            while(resourceWrapper.isGetFinished() == false && elapsed <= timeout) {
                Thread.sleep(sleepTime);
                elapsed += sleepTime;
            }
        } catch (OcException ex) {
            Log.e("CordovaPluginOCF", ex.toString());
        } catch (InterruptedException ex) {
        }

        return ocfResource;
    }

    private static OcResource resourceToNative(OcfResource ocfResource) {
        OcResource nativeResource = null;
        String url = ocfResource.getId().getDeviceId();
        String host = ocfResource.getId().getResourcePath();
        ArrayList<String> resourceTypes = ocfResource.getResourceTypes();
        ArrayList<String> interfaces = ocfResource.getInterfaces();

        Log.d("CordovaPluginOCF", "creating native resource...");
        Log.d("CordovaPluginOCF", "url = " + url);
        Log.d("CordovaPluginOCF", "host = " + host);
        Log.d("CordovaPluginOCF", "resourceTypes size = " + resourceTypes.size());
        Log.d("CordovaPluginOCF", "interfaces size = " + interfaces.size());
        try {
            nativeResource = OcPlatform.constructResourceObject(
                ocfResource.getId().getDeviceId(),
                ocfResource.getId().getResourcePath(),
                EnumSet.of(OcConnectivityType.CT_DEFAULT),
                false,
                ocfResource.getResourceTypes(),
                ocfResource.getInterfaces());
        } catch (OcException ex) {
            Log.e("CordovaPluginOCF", ex.toString());
        }

        Log.d("CordovaPluginOCF", "returning native resource...");
        return nativeResource;
    }

    private static OcRepresentation representationToNative(
        OcfResourceRepresentation repr)
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
                    Log.w("CordovaPluginOCF", "Value is not an integer");
                }

                try {
                    double d = Double.parseDouble(stringValue);
                    nativeRepr.setValue(key, d);
                    done = true;
                } catch(NumberFormatException ex) {
                    Log.w("CordovaPluginOCF", "Value is not a double");
                }

                if (!done) {
                    nativeRepr.setValue(key, stringValue);
                }
            } catch(OcException ex) {
                Log.e("CordovaPluginOCF", ex.toString());
            }
        }

        return nativeRepr;
    }

    private static OcfResourceRepresentation representationFromNative(
        OcRepresentation nativeRepr)
    {
        OcfResourceRepresentation repr = new OcfResourceRepresentation();
        Map<String, Object> values = nativeRepr.getValues();
        for (Map.Entry<String, Object> entry: values.entrySet()) {
            repr.setValue(entry.getKey(), entry.getValue());
        }
        return repr;
    }

    @Override
    public void onDeviceFound(final OcRepresentation repr) {
        OcfDevice device = new OcfDevice();
        try {
            device.setUuid((String) repr.getValue(OC_RSRVD_DEVICE_ID));
            device.setName((String) repr.getValue(OC_RSRVD_DEVICE_NAME));
            device.setCoreSpecVersion((String) repr.getValue(OC_RSRVD_SPEC_VERSION));
            device.setDataModels(new ArrayList<String>() {{
                add((String) repr.getValue(OC_RSRVD_DATA_MODEL_VERSION));
            }});
        } catch (OcException ex) {
            Log.e("CordovaPluginOCF", "Error reading OcRepresentation");
        }

        OcfDeviceEvent ev = new OcfDeviceEvent(device);
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
                "", "/ocf/d", EnumSet.of(OcConnectivityType.CT_DEFAULT), this);
        } catch (OcException ex) {
            this.findDevicesCallbackContext.error(ex.getMessage());
        }
    }

    @Override
    public synchronized void onResourceFound(OcResource resource) {
        String host = resource.getHost();
        String resourcePath = resource.getUri();
        String key = host + resourcePath;

        Log.d("CordovaPluginOCF", "Found resource: " + key);

        if (resourcePath.equals("/ocf/p") || resourcePath.equals("/ocf/d")) {
            return;
        }


        OcfResource ocfResource = this.resourceFromNative(resource);
        OcfResourceWrapper resourceWrapper = new OcfResourceWrapper(this, resource, ocfResource);

        if (resource.isObservable() && ! this.observedResources.contains(key)) {
            try {
                Log.d("CordovaPluginOCF", "Observing resource: " + key);
                resource.observe(ObserveType.OBSERVE, new HashMap<String, String>(), resourceWrapper);
                this.observedResources.add(key);
            } catch (OcException e) {
                Log.e("CordovaPluginOCF", "Unable to observe resoure");
            }
        }

        OcfResourceEvent ev = new OcfResourceEvent(ocfResource);

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

        OcfResource ocfResource = OcfResource.fromJSON(args.getJSONObject(0));
        OcResource nativeResource = OcfBackendIotivity.resourceToNative(ocfResource);
        OcRepresentation nativeRepr = this.representationToNative(
            ocfResource.getProperties());
        OcfResourceWrapper resourceWrapper = new OcfResourceWrapper(
            this, nativeResource, ocfResource);

        Log.d("CordovaPluginOCF", "Updating resource: " +  ocfResource.toJSON().toString());

        try {
            nativeResource.put(
                nativeRepr, new HashMap<String, String>(), resourceWrapper);
            while(resourceWrapper.isPutFinished() == false && elapsed <= timeout) {
                Thread.sleep(sleepTime);
                elapsed += sleepTime;
            }
        } catch (OcException ex) {
            Log.e("CordovaPluginOCF", ex.toString());
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
