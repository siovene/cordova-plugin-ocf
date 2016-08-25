package com.intel.cordova.plugin.ocf;

// Java
import java.util.ArrayList;
import java.util.Arrays;

// Third party
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OcfDevice implements OcfObjectInterface {
    private String uuid;
    private String url;
    private String name;
    private ArrayList<String> dataModels;
    private String coreSpecVersion;
    private String role;

    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getUuid() { return this.uuid; }

    public void setUrl(String url) { this.url = url; }
    public String getUrl() { return this.url; }

    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }

    public void setDataModels(ArrayList<String> dataModels) {
        this.dataModels = new ArrayList<String>(dataModels);
    }
    public ArrayList<String> getDataModels() { return this.dataModels; }

    public void setCoreSpecVersion(String coreSpecVersion) {
        this.coreSpecVersion = coreSpecVersion;
    }
    public String getCoreSpecVersion() { return this.coreSpecVersion; }

    public void setRole(String role) { this.role = role; }
    public String getRole() { return this.role; }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("uuid", this.uuid);
        o.put("url", this.url);
        o.put("name", this.name);
        o.put("dataModels", new JSONArray(this.dataModels));
        o.put("coreSpecVersion", this.coreSpecVersion);
        o.put("role", this.role);

        return o;
    }
}
