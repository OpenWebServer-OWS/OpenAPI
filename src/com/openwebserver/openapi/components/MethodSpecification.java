package com.openwebserver.openapi.components;

import com.openwebserver.core.handlers.RequestHandler;
import com.openwebserver.core.routing.Route;
import com.openwebserver.core.security.sessions.Session;
import com.openwebserver.openapi.annotations.Body;
import com.openwebserver.openapi.annotations.OpenAPI;
import com.openwebserver.openapi.annotations.Parameters;
import com.openwebserver.openapi.annotations.Responses;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

public class MethodSpecification extends JSONObject{

    private final String path;
    private final RequestHandler handler;
    private final SpecificationHolder holder;
    private final Class<?> serviceClass;

    public MethodSpecification(RequestHandler handler, SpecificationHolder holder) throws OpenApiException {
        if(handler.getReflection() == null){
            throw new OpenApiException("Not supported method");
        }
        if (!handler.getReflection().isAnnotationPresent(OpenAPI.class)) {
            throw new OpenApiException.NotationException(OpenAPI.class);
        }
        this.path = handler.getPath();
        this.handler = handler;
        holder.register(this);
        this.holder = holder;
        this.serviceClass = handler.getReflection().getDeclaringClass();
    }

    public String getPath() {
        return path;
    }

    public void generate(JSONObject container) {

        OpenAPI spec = handler.getReflection().getAnnotation(OpenAPI.class);
        if (!spec.summary().equals("")) {
            put("summary", spec.summary());
        }
        if (!spec.description().equals("")) {
            put("description", spec.description());
        }
        if (!spec.operationId().equals("#")) {
            put("operationId", spec.operationId());
        } else {
            put("operationId", handler.getReflection().getName() + "%" + UUID.randomUUID().toString());
        }

        if(handler.getPolicy() != null){
            if(!has("description")){
                put("description", "");
            }else{
                put("description", get("description") +
                        "</br>" +
                        "</br><b>========= CORS POLICY =========</b>" +
                        "</br> <b>Name: " + handler.getPolicy().getName() + "</b>" +
                        "</br> <b>Origin: " + handler.getPolicy().getAllowedOrigin() + "</b>" +
                        "</br> <b>Headers: " + handler.getPolicy().getAllowedHeaders() + "</b>" +
                        "</br> <b>Methods: " + handler.getPolicy().getAllowedMethods() + "</b>" +
                        "</br> <b>================================</b>"
                );
            }

            if(!has("summery")){
                put("summery", "");
            }else{
                put("summery", get("summery") + "[CORS]");
            }

        }

        //region tags
        ArrayList<String> tags = new ArrayList<>();
        if (serviceClass.isAnnotationPresent(OpenAPI.class)) {
            Collections.addAll(tags, serviceClass.getAnnotation(OpenAPI.class).tags());
        }
        if (spec.tags().length > 0) {
            Collections.addAll(tags, spec.tags());
        }
        put("tags", tags);
        //endregion

        //region parameters (Only path, cookie, query, header)
        JSONArray parameters = new JSONArray();
        ArrayList<String> requiredParams = new ArrayList<>();
        if (handler.getReflection().isAnnotationPresent(Parameters.class)) {
            String in = null;
            String description;
            boolean required = false;
            Class<?> type = String.class;
            Parameters annotationParameters = handler.getReflection().getAnnotation(Parameters.class);
            for (int i = 0; i < annotationParameters.names().length; i++) {
                JSONObject param = new JSONObject();
                if (annotationParameters.in().length >= i) {
                    in = annotationParameters.in()[i];
                }
                if (annotationParameters.descriptions().length > i) {
                    description = annotationParameters.descriptions()[i];
                } else {
                    description = "Not provided";
                }
                if (annotationParameters.required().length > i) {
                    required = annotationParameters.required()[i];
                }
                if (annotationParameters.type().length > i) {
                    type = annotationParameters.type()[i];
                }

                param.put("name", annotationParameters.names()[i]);
                param.put("in", in);
                param.put("description", description);
                param.put("schema", new JSONObject().put("type", type.getSimpleName().toLowerCase(Locale.ROOT)));
                if (in.equals("path")) {
                    param.put("required", true);
                } else {
                    param.put("required", required);
                }
                if (required) {
                    requiredParams.add(annotationParameters.names()[i]);
                }
                parameters.put(param);
            }
        }

        //region REST and required GET params
        if (handler.requires() || handler.isREST()) {
            if (handler.requires() && handler.getMethod().equals(Route.Method.GET)) {
                for (String param : handler.getRequired()) {
                    if (requiredParams.contains(param)) {
                        JSONObject paramJSON = new JSONObject();
                        paramJSON.put("name", param);
                        paramJSON.put("in", "query");
                        paramJSON.put("schema", new JSONObject().put("type", "string"));
                        paramJSON.put("required", true);
                        parameters.put(param);
                    }
                }
            }
            if (handler.isREST()) {
                handler.getRESTParams().forEach(item -> {
                    if (!requiredParams.contains(item)) {
                        JSONObject paramJSON = new JSONObject();
                        paramJSON.put("name", item.replace("{", "").replace("}", ""));
                        paramJSON.put("in", "path");
                        paramJSON.put("schema", new JSONObject().put("type", "string"));
                        paramJSON.put("required", true);
                        parameters.put(paramJSON);
                        requiredParams.add(item);
                    }
                });
            }
        }
        //endregion

        put("parameters", parameters);
        //endregion

        //region requestBody
        if (!handler.getMethod().equals(Route.Method.GET)) {
            ArrayList<String> requiredBodyParams = new ArrayList<>();
            JSONObject requestBody = new JSONObject();
            JSONObject properties = new JSONObject();
            String bodyType = "multipart/form-data";
            String schemaType = "object";
            if (handler.getReflection().isAnnotationPresent(Body.class)) {
                Body annotationBody = handler.getReflection().getAnnotation(Body.class);
                bodyType = annotationBody.type().getPretty();
                schemaType = annotationBody.schemaType();
                requestBody.put("required", annotationBody.required());

                //region properties

                boolean requires = false;
                String type = "string";
                for (int i = 0; i < annotationBody.properties().length; i++) {
                    if (annotationBody.types().length > i) {
                        type = annotationBody.types()[i];
                    }
                    if (annotationBody.requires().length > i) {
                        requires = annotationBody.requires()[i];
                    }
                    if (requires) {
                        requiredBodyParams.add(annotationBody.properties()[i]);
                    }
                    properties.put(annotationBody.properties()[i], new JSONObject()
                            .put("type",type.toLowerCase(Locale.ROOT))
                    );
                }
                //endregion

            }
            if (handler.requires()) {
                for (String r : handler.getRequired()) {
                    if (!requiredBodyParams.contains(r)) {
                        properties.put(r, new JSONObject()
                                .put("type", "string")
                        );
                        requiredBodyParams.add(r);
                    }
                }
            }
            if (!properties.isEmpty()) {
                requestBody.put("content", new JSONObject()
                        .put(bodyType, new JSONObject()
                                .put("schema", new JSONObject()
                                        .put("type", schemaType)
                                        .put("properties", properties)
                                        .put("required", requiredBodyParams)
                                )
                        )
                );
                put("requestBody", requestBody);
            }
        }
        //endregion

        //region responses
        if (handler.getReflection().isAnnotationPresent(Responses.class)) {
            JSONObject responses = new JSONObject();
            Responses annotationResponses = handler.getReflection().getAnnotation(Responses.class);
            for (int i = 0; i < annotationResponses.codes().length; i++) {
                responses.put(String.valueOf(annotationResponses.codes()[i].getCode()), new JSONObject().put("description", annotationResponses.descriptions()[i]));
            }
            put("responses", responses);
        }
        //endregion

        //region session auth
        if (handler.getSessionSpecification() != null) {
            holder.getSecuritySchemes().put("cookieAuth", new JSONObject()
                    .put("in", "cookie")
                    .put("name", Session.name)
                    .put("type", "apiKey")
            );
        }
        //endregion


        container.put(handler.getMethod().toString().toLowerCase(Locale.ROOT),this);
    }
}
