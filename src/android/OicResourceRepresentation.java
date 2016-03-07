package com.intel.cordova.plugin.oic;

// Java
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

// Android
import android.util.Log;

// Third party
import org.json.JSONException;
import org.json.JSONObject;


public class OicResourceRepresentation implements OicObjectInterface {
    private Map<String, Object> properties;

    public OicResourceRepresentation() {
        this.properties = new HashMap<String, Object>();
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    public Map<String, Object> getProperties() { return this.properties; }

    public Object getValue(String key) {
        return this.properties.get(key);
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    public void setValue(String key, Object value) {
        this.properties.put(key, value);
    }

    // ------------------------------------------------------------------------
    // Conversions
    // ------------------------------------------------------------------------

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
            o.put(entry.getKey(), entry.getValue());
        }

        return o;
    }

    public static OicResourceRepresentation fromJSON(JSONObject obj)
        throws JSONException
    {
        OicResourceRepresentation repr = new OicResourceRepresentation();
        Iterator<String> it = obj.keys();
        while(it.hasNext()) {
            String key = it.next();
            repr.setValue(key, obj.get(key));
        }

        return repr;
    }
}
