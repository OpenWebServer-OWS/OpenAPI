package com.openwebserver.utils.OpenAPI;

import FileManager.Folder;
import Tree.TreeArrayList;
import com.openwebserver.core.Content.Code;
import com.openwebserver.core.Objects.Headers.Header;
import com.openwebserver.core.Objects.Request;
import com.openwebserver.core.Objects.Response;
import com.openwebserver.core.WebException;
import com.openwebserver.services.Annotations.Route;
import com.openwebserver.services.Objects.Service;
import com.openwebserver.utils.OpenAPI.Annotations.Parameters;
import com.openwebserver.utils.OpenAPI.Annotations.Responses;
import com.openwebserver.utils.OpenAPI.Components.Tag;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import static Collective.Collective.*;
import static com.openwebserver.services.ServiceManager.*;

public class OpenAPI extends Service {

    public static String version = "3.0.3";
    private static final Folder resources = new Folder("./res/swagger_ui");

    private final TreeArrayList<String, java.lang.reflect.Method> routes = new TreeArrayList<>();

    //region JSONStructure
    private final JSONObject _root = new JSONObject().put("openapi", OpenAPI.version);
        private final JSONObject info = new JSONObject();
        private final JSONArray tags = new JSONArray();
        private final JSONArray schemes = new JSONArray();
        private final JSONObject paths = new JSONObject();
//        private final JSONObject definitions = new JSONObject(); //TODO
//        private final JSONObject externalDocs = new JSONObject(); //TODO
    //endregion

    public OpenAPI(String title, String description, String version){
        super("/openapi");
        super.routes.add(this);
        info.put("title", title);
        info.put("description", description);
        info.put("version", version);
        generateSpecification();
    }

    public OpenAPI addInfo(String key, Object value){
        info.put(key, value);
        return this;
    }

    public OpenAPI setHost(String host){
        _root.put("host", host);
        return this;
    }

    public OpenAPI setBasePath(String basePath){
        _root.put("basePath", basePath);
        return this;
    }

    public OpenAPI addTag(Tag tag){
        tags.put(tag);
        return this;
    }

    public OpenAPI addScheme(String scheme){
        schemes.put(scheme);
        return this;
    }

    public OpenAPI addRoute(java.lang.reflect.Method m){
        if(m.isAnnotationPresent(Route.class)){
            routes.addOn(m.getAnnotation(Route.class).path(), m);
        }
        return this;
    }

    public void generateSpecification(){
        doForEach(getServices().values(), s -> s.getClass().isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class), service ->
                doForEach(service.getClass().getDeclaredMethods(),
                        m -> m.isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class),
                        this::addRoute
                ));
    }

    public JSONObject generate(){
        _root.put("info", info);
        _root.put("schemes", schemes);
        routes.forEach((r, methods) ->{
            JSONObject route = new JSONObject();
            methods.forEach(m -> {
                JSONObject method = new JSONObject();
                //region tags
                com.openwebserver.utils.OpenAPI.Annotations.OpenAPI api = m.getAnnotation(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class);
                method.put("tags", api.tags());
                //endregion
                //region summary
                method.put("summary", m.getAnnotation(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class).summary());
                //endregion
                //region description
                String description = api.description();
                method.put("description", (!description.equals(""))? description: "Description not provided");
                //endregion
                //region operationId
                String operationId = api.operationId();
                method.put("operationId", (!operationId.equals("#"))? operationId: m.getName());
                //endregion
                //region parameters
                Route r1 = m.getAnnotation(Route.class);
                if(RESTDecoder.containsRegex(r1.path()) && m.isAnnotationPresent(Parameters.class)){
                    JSONArray parameters = new JSONArray();
                    Parameters params = m.getAnnotation(Parameters.class);
                    for (int i = 0; i < params.names().length; i++) {
                        JSONObject param = new JSONObject();
                        param.put("in", params.in()[i]);
                        param.put("name", params.names()[i]);
                        param.put("description", params.descriptions()[i]);
                        param.put("required", true);
                        parameters.put(param);
                    }
                    method.put("parameters", parameters);
                }
                //endregion
                //region responses
                if(m.isAnnotationPresent(Responses.class)){
                    JSONObject responsesJSON = new JSONObject();
                    Responses responses = m.getAnnotation(Responses.class);
                    for (int i = 0; i < responses.codes().length; i++) {
                        responsesJSON.put(String.valueOf(responses.codes()[i].getCode()),new JSONObject().put("description", responses.descriptions()[i]));
                    }
                    method.put("responses", responsesJSON);
                }
                //endregion
                route.put(r1.method().name().toLowerCase(Locale.ROOT), method);
            });
            paths.put(r, route);
        });
        _root.put("paths", paths);

        return _root;
    }

    @Route(path = "/specification")
    public Response specification(Request request){
        if(schemes.isEmpty()){
           addScheme(getDomain().getProtocol());
           setHost(getDomain().getAlias());
        }
        return Response.simple(generate());
    }

//    public OpenAPI(String title, String description, String version){
//        super("/openapi");
//        this.routes.add(this);
//        info.put("title", title);
//        info.put("description", description);
//        info.put("version", version);
//        this.paths = generateSpecification();
//    }
//
//    public JSONObject generateSpecification(){
//        for (Service service : ServiceManager.getServices().values()) {
//            if(service.getClass().isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class)) {
//                return getRoutes(service);
//            }
//        }
//        return new JSONObject();
//    }
//
//    public JSONObject getRoutes(Service service){
//        JSONObject paths = new JSONObject();
//        TreeArrayList<String, java.lang.reflect.Method> methodTreeArrayList = new TreeArrayList<>();
//        for (java.lang.reflect.Method method : service.getClass().getDeclaredMethods()) {
//            if(method.isAnnotationPresent(Route.class)){
//                methodTreeArrayList.addOn(method.getAnnotation(Route.class).path(), method);
//            }
//        }
//
//        String summary = "";
//        String description = "";
//        for (Constructor<?> constructor : service.getClass().getDeclaredConstructors()) {
//            if(constructor.isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class)){
//                description = constructor.getAnnotation(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class).value();
//            }
//            if(constructor.isAnnotationPresent(Summary.class)){
//                summary = constructor.getAnnotation(Summary.class).value();
//            }
//        }
//
//        String finalSummary = summary;
//        String finalDescription = description;
//        methodTreeArrayList.forEach((path, methods) ->{
//            JSONObject route = new JSONObject();
//            route.put("summary", finalSummary);
//            route.put("description", finalDescription);
//            methods.forEach(method -> {
//                JSONObject requestMethod = new JSONObject();
//                if(method.isAnnotationPresent(Summary.class)){
//                    requestMethod.put("summary", method.getAnnotation(Summary.class).value());
//                }
//                if(method.isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class)){
//                    requestMethod.put("description", method.getAnnotation(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class).description());
//                    requestMethod.put("tags", method.getAnnotation(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class).tags());
//                }
//                if(method.isAnnotationPresent(Responses.class)){
//                    Responses responses = method.getAnnotation(Responses.class);
//                    JSONObject responsesJSON = new JSONObject();
//                    if(responses.descriptions().length != responses.codes().length){
//                        try {
//                            throw new OpenApiNotationException("Invalid Responses declaration on method '"+method.getName()+"' in '"+service.getName()+"'");
//                        } catch (OpenApiNotationException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    else{
//                        for (int i = 0; i < responses.descriptions().length; i++) {
//                            responsesJSON.put(responses.codes()[i].name().toLowerCase(Locale.ROOT), new JSONObject().put("description", responses.descriptions()[i]));
//                        }
//                        requestMethod.put("responses", responsesJSON);
//                    }
//
//                }
//                if(method.isAnnotationPresent(Route.class)){
//                    Route r = method.getAnnotation(Route.class);
//                    if(RESTDecoder.containsRegex(r.path()) && method.isAnnotationPresent(Parameters.class)){
//                        Parameters parameters = method.getAnnotation(Parameters.class);
//                        JSONArray params = new JSONArray();
//                        try {
//                            for (int i = 0; i < parameters.in().length; i++) {
//                                JSONObject parameterJSON = new JSONObject();
//                                parameterJSON.put("in", parameters.in()[i]);
//                                parameterJSON.put("name", parameters.names()[i]);
//                                parameterJSON.put("description", parameters.descriptions()[i]);
//                                parameterJSON.put("required", parameters.required()[i]);
//                                params.put(parameterJSON);
//                            }
//                            requestMethod.put("parameters", params);
//                        }catch (IndexOutOfBoundsException e){
//                            try {
//                                throw new OpenApiNotationException("Invalid Parameters declaration on method '"+method.getName()+"' in '"+service.getName()+"'");
//                            } catch (OpenApiNotationException openApiNotationException) {
//                                openApiNotationException.printStackTrace();
//                            }
//                        }
//                    }
//                }
//                route.put(method.getAnnotation(Route.class).method().toString().toLowerCase(Locale.ROOT), requestMethod);
//            });
//            paths.put(path,route);
//
//        });
//        return paths;
//    }
//
//    public Response root(Request request){
//        return Response.simple(Code.Ok).addHeader(new Header("Location", "./index.html"));
//    }
//
//    @Route(path = "/specification")
//    public Response specification(Request request){
//        return Response.simple(new JSONObject()
//                .put("openapi", version)
//                .put("info", info)
//                .put("servers", new JSONArray().put(new JSONObject().put("url", "/")))
//                .put("paths", paths)
//        );
//    }
//
    @Override
    public Response handle(Request request) throws Throwable {
        if(request.isFile()){
            return Response.simple(resources.search(request.getFileName()));
        }else{
            return Response.simple(Code.Temporary_Redirect).addHeader(new Header("Location", "./index.html"));
        }
    }

    public static class OpenApiNotationException extends WebException {
        public OpenApiNotationException(String message){
            super(message);
        }
    }
}
