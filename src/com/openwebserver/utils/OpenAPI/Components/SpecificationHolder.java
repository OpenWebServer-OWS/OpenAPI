package com.openwebserver.utils.OpenAPI.Components;

import org.json.JSONArray;
import org.json.JSONObject;

public interface SpecificationHolder {

    void register(MethodSpecification specification);

    JSONObject getPaths();

    JSONObject getComponents();

    JSONObject getInfo();

    JSONArray getServers();

    default JSONObject getSecuritySchemes(){
        if(!getComponents().has("securitySchemes")){
            getComponents().put("securitySchemes", new JSONObject());
        }
        return getComponents().getJSONObject("securitySchemes");
    }

}
