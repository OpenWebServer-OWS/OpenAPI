package com.openwebserver.utils.OpenAPI.Components;

import com.openwebserver.core.Handlers.RequestHandler;
import com.openwebserver.core.Routing.Route;
import com.openwebserver.core.Sessions.Session;
import com.openwebserver.utils.OpenAPI.Annotations.Body;
import com.openwebserver.utils.OpenAPI.Annotations.OpenAPI;
import com.openwebserver.utils.OpenAPI.Annotations.Parameters;
import com.openwebserver.utils.OpenAPI.Annotations.Responses;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

public class MethodSpecification {

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
        JSONObject methodSpecification = new JSONObject();
        OpenAPI spec = handler.getReflection().getAnnotation(OpenAPI.class);
        if (!spec.summary().equals("")) {
            methodSpecification.put("summary", spec.summary());
        }
        if (!spec.description().equals("")) {
            methodSpecification.put("summary", spec.description());
        }
        if (!spec.operationId().equals("#")) {
            methodSpecification.put("operationId", spec.operationId());
        } else {
            methodSpecification.put("operationId", handler.getReflection().getName() + "%" + UUID.randomUUID().toString());
        }

        //region tags
        ArrayList<String> tags = new ArrayList<>();
        if (serviceClass.isAnnotationPresent(OpenAPI.class)) {
            Collections.addAll(tags, serviceClass.getAnnotation(OpenAPI.class).tags());
        }
        if (spec.tags().length > 0) {
            Collections.addAll(tags, spec.tags());
        }
        methodSpecification.put("tags", tags);
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

        methodSpecification.put("parameters", parameters);
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
                methodSpecification.put("requestBody", requestBody);
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
            methodSpecification.put("responses", responses);
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


        container.put(handler.getMethod().toString().toLowerCase(Locale.ROOT), methodSpecification);
    }
}
