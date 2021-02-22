package com.openwebserver.utils.OpenAPI.Components;

import org.json.JSONObject;

public class Tag extends JSONObject {

    public Tag(String name, String description){
        put("name", name);
        put("description", description);
    }

}
