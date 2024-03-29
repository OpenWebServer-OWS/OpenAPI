package com.openwebserver.openapi;

import FileManager.Folder;
import FileManager.Local;
import com.openwebserver.core.handlers.RequestHandler;
import com.openwebserver.core.http.Header;
import com.openwebserver.core.http.content.Code;
import com.openwebserver.core.objects.Request;
import com.openwebserver.core.objects.Response;
import com.openwebserver.core.routing.Router;
import com.openwebserver.openapi.components.OpenApiException;
import com.openwebserver.services.annotations.Route;
import com.openwebserver.services.objects.Service;
import com.tree.TreeArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.function.Consumer;

public class OpenAPI extends Service implements com.openwebserver.openapi.components.SpecificationHolder {

    public static String version = "3.0.3";

    private final TreeArrayList<String, com.openwebserver.openapi.components.MethodSpecification> routes = new TreeArrayList<>();
    public static Folder resources = new Folder("./res/swagger_ui");

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

    @Override
    public void register(Consumer<RequestHandler> routeConsumer) {
        super.register(routeConsumer);
        Router.getRoutes(this.getDomain()).forEach(routes -> routes.values().forEach(handler -> {
            try {
                new com.openwebserver.openapi.components.MethodSpecification(handler, this);
            } catch (OpenApiException ignored) {}
        }));
    }

    public JSONObject generate(){
        if(!_root.has("paths")) {
            _root.put("info", getInfo());
            _root.put("servers", getServers());
            _root.put("components", getComponents());
            routes.forEach((route, specifications) -> {
                JSONObject requestMethods = new JSONObject();
                specifications.forEach(methodSpecification -> {
                    methodSpecification.generate(requestMethods);
                });
                getPaths().put(route, requestMethods);
            });
            _root.put("paths", getPaths());
        }
        return _root;
    }


    @Route(path = "/specification", method = Method.GET)
    public Response specification(Request request){
        return Response.simple(generate());
    }

    @Route(path = "/#", method = Method.GET)
    public Response ALL(Request request) throws FileNotFoundException {
        if(request.isFile()){
            return Response.simple(new Local(resources.getPath().toString() + "/" + request.getFileName()));
        }else{
            return Response.simple(Code.Temporary_Redirect).addHeader(new Header("Location", "./index.html"));
        }
    }

    @Override
    public void register(com.openwebserver.openapi.components.MethodSpecification specification) {
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
