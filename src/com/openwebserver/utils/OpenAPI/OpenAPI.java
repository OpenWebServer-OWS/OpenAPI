package com.openwebserver.utils.OpenAPI;

import FileManager.Folder;
import com.openwebserver.core.Content.Code;
import com.openwebserver.core.Handlers.RequestHandler;
import com.openwebserver.core.Objects.Headers.Header;
import com.openwebserver.core.Objects.Request;
import com.openwebserver.core.Objects.Response;
import com.openwebserver.core.Routing.Router;
import com.openwebserver.services.Annotations.Route;
import com.openwebserver.services.Objects.Service;
import com.openwebserver.utils.OpenAPI.Components.*;
import com.tree.TreeArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.function.Consumer;

import static com.openwebserver.utils.OpenAPI.utils.MyCollection.doForEach;

public class OpenAPI extends Service implements SpecificationHolder {

    public static String version = "3.0.3";
    private static final Folder resources = new Folder("./res/swagger_ui");

    private final TreeArrayList<String, MethodSpecification> routes = new TreeArrayList<>();

    //region JSONStructure
    private final JSONObject _root = new JSONObject().put("openapi", OpenAPI.version);
    private final JSONObject info = new JSONObject();
    private final JSONArray servers = new JSONArray();
    private final JSONObject paths = new JSONObject();
    private final JSONObject components = new JSONObject();
    //endregion

    public OpenAPI(String title, String description, String version){
        super("/openapi");
        info.put("title", title);
        info.put("description", description);
        info.put("version", version);
    }


    public OpenAPI addInfo(String key, Object value){
        info.put(key, value);
        return this;
    }

    public OpenAPI addServer(String server){
        servers.put(new JSONObject().put("url", server));
        return this;
    }

//    public OpenAPI addRoute(Pair<Service,java.lang.reflect.Method> serviceMethodPair){
////        if(serviceMethodPair.getValue().isAnnotationPresent(Route.class)){
////            routes.addOn(serviceMethodPair.getValue().getAnnotation(Route.class).path(), serviceMethodPair);
////        }
////        return this;
//    }


    @Override
    public void register(Consumer<RequestHandler> routeConsumer) {
        super.register(routeConsumer);
        Router.getRoutes(this.getDomain()).forEach(routes -> routes.values().forEach(handler -> {
            try {
                new MethodSpecification(handler, this);
            } catch (OpenApiException.NotationException ignored) {}
        }));
    }



//        doForEach(getServices().values(), s -> s.getClass().isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class), service ->
//                doForEach(service.getClass().getDeclaredMethods(),
//                        m -> m.isAnnotationPresent(com.openwebserver.utils.OpenAPI.Annotations.OpenAPI.class),
//                        method -> addRoute(new Pair<>(service, method))
//                ));



    public JSONObject generate(){
        _root.put("info", getInfo());
        _root.put("servers", getServers());
        _root.put("components",getComponents());
        routes.forEach((route, specifications) -> {
            JSONObject requestMethods = new JSONObject();
            specifications.forEach(methodSpecification -> {
                methodSpecification.generate(requestMethods);
            });
            getPaths().put(route,requestMethods);
        });
        _root.put("paths", getPaths());

        return _root;
    }


    @Route(path = "/specification", method = Method.GET)
    public Response specification(Request request){
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

    @Route(path = "/#", method = Method.GET)
    public Response ALL(Request request) throws FileNotFoundException {
        if(request.isFile()){
            return Response.simple(resources.search(request.getFileName()));
        }else{
            return Response.simple(Code.Temporary_Redirect).addHeader(new Header("Location", "./index.html"));
        }
    }

    @Override
    public void register(MethodSpecification specification) {
        routes.addOn(specification.getPath(), specification);
    }

    @Override
    public JSONObject getPaths() {
        return paths;
    }

    @Override
    public JSONObject getComponents() {
        return components;
    }

    @Override
    public JSONObject getInfo() {
        return info;
    }

    @Override
    public JSONArray getServers() {
        return servers;
    }


}
