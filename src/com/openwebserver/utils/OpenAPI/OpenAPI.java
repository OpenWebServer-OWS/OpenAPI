package com.openwebserver.utils.OpenAPI;

import FileManager.Folder;
import Tree.TreeArrayList;
import com.openwebserver.core.Content.Code;
import com.openwebserver.core.Domain;
import com.openwebserver.core.Objects.Headers.Header;
import com.openwebserver.core.Objects.Request;
import com.openwebserver.core.Objects.Response;
import com.openwebserver.core.WebException;
import com.openwebserver.services.Annotations.Route;
import com.openwebserver.services.Objects.Service;
import com.openwebserver.services.ServiceManager;
import com.openwebserver.utils.OpenAPI.Annotations.Responses;
import com.openwebserver.utils.OpenAPI.Annotations.Summary;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.Locale;

public class OpenAPI extends Service {

    public static String version = "3.0.3";

    private final JSONObject info = new JSONObject();
    private static final Folder resources = new Folder("./res/swagger_ui");
    private final JSONObject paths;
    private Domain domain;

    public OpenAPI(String title, String description, String version){
        super("/openapi");
        this.routes.add(this);
        info.put("title", title);
        info.put("description", description);
        info.put("version", version);
        this.paths = generateSpecification();
    }

    public JSONObject generateSpecification(){
        for (Service service : ServiceManager.getServices().values()) {
            if(service.getClass().isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class)) {
                return getRoutes(service);
            }
        }
        return new JSONObject();
    }

    public JSONObject getRoutes(Service service){
        JSONObject paths = new JSONObject();
//        String path = service.getPath();

        TreeArrayList<String, java.lang.reflect.Method> methodTreeArrayList = new TreeArrayList<>();
        for (java.lang.reflect.Method method : service.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(Route.class)){
                methodTreeArrayList.addOn(method.getAnnotation(Route.class).path(), method);
            }
        }

        String summary = "";
        String description = "";
        for (Constructor<?> constructor : service.getClass().getDeclaredConstructors()) {
            if(constructor.isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class)){
                description = constructor.getAnnotation(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class).value();
            }
            if(constructor.isAnnotationPresent(Summary.class)){
                summary = constructor.getAnnotation(Summary.class).value();
            }
        }

        String finalSummary = summary;
        String finalDescription = description;
        methodTreeArrayList.forEach((path, methods) ->{
            JSONObject route = new JSONObject();
            route.put("summary", finalSummary);
            route.put("description", finalDescription);
            methods.forEach(method -> {
                JSONObject requestMethod = new JSONObject();
                if(method.isAnnotationPresent(Summary.class)){
                    requestMethod.put("summary", method.getAnnotation(Summary.class).value());
                }
                if(method.isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class)){
                    requestMethod.put("description", method.getAnnotation(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class).description());
                    requestMethod.put("tags", method.getAnnotation(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class).tags());
                }
                if(method.isAnnotationPresent(Responses.class)){
                    Responses responses = method.getAnnotation(Responses.class);
                    JSONObject responsesJSON = new JSONObject();
                    if(responses.descriptions().length != responses.codes().length){
                        try {
                            throw new OpenApiNotationException("Invalid Responses declaration on method '"+method.getName()+"' in '"+service.getName()+"'");
                        } catch (OpenApiNotationException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        for (int i = 0; i < responses.descriptions().length; i++) {
                            responsesJSON.put(responses.codes()[i].name().toLowerCase(Locale.ROOT), new JSONObject().put("description", responses.descriptions()[i]));
                        }
                        requestMethod.put("responses", responsesJSON);
                    }

                }
                route.put(method.getAnnotation(Route.class).method().toString().toLowerCase(Locale.ROOT), requestMethod);
            });
            paths.put("/../.."+path,route);

        });
        return paths;
    }

    public Response root(Request request){
        return Response.simple(Code.Ok).addHeader(new Header("Location", "./index.html"));
    }

    @Route(path = "/specification")
    public Response specification(Request request){
        return Response.simple(new JSONObject()
                .put("openapi", version)
                .put("info", info)
                .put("servers", new JSONArray().put(new JSONObject().put("url", "/")))
                .put("paths", paths)
        );
    }

    @Override
    public Response handle(Request request) throws Throwable {
        this.domain = request.getDomain();
        if(request.isFile()){
            return Response.simple(resources.search(request.getFileName()));
        }else{
            return root(request);
        }
    }

    public static class OpenApiNotationException extends WebException {
        public OpenApiNotationException(String message){
            super(message);
        }
    }
}
