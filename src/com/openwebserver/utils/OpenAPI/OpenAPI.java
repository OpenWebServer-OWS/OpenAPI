package com.openwebserver.utils.OpenAPI;

import FileManager.Folder;
import FileManager.Local;
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

public class OpenAPI extends Service implements SpecificationHolder {

    public static String version = "3.0.3";

    private final TreeArrayList<String, MethodSpecification> routes = new TreeArrayList<>();
    private final Folder resource = new Folder("./res/swagger_ui");

    //region JSONStructure
    private final JSONObject _root = new JSONObject().put("openapi", OpenAPI.version);
    private final JSONObject info = new JSONObject();
    private final JSONArray servers = new JSONArray();
    private final JSONObject paths = new JSONObject();
    private final JSONObject components = new JSONObject();
    private Folder folder = null;
    //endregion

    public OpenAPI(String title, String description, String version){
        super("/openapi");
        info.put("title", title);
        info.put("description", description);
        info.put("version", version);
    }

    public void setFolder(Folder folder){
        this.folder = folder;
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
                new MethodSpecification(handler, this);
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
            return Response.simple(new Local(resource.getPath().toString() + "/" + request.getFileName()));
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
