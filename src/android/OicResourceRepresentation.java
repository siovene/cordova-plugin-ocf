package com.intel.cordova.plugin.oic;

// Java
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

    public void setValue(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getValue(String key) {
        return this.properties.get(key);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
            o.put(entry.getKey(), entry.getValue());
        }

        return o;
    }
}
