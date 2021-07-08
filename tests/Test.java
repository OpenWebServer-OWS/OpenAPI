
import com.openwebserver.core.objects.Request;
import com.openwebserver.core.objects.Response;
import com.openwebserver.core.security.CORS.CORS;
import com.openwebserver.core.security.sessions.annotations.Session;
import com.openwebserver.openapi.annotations.OpenAPI;
import com.openwebserver.openapi.annotations.Parameters;
import com.openwebserver.openapi.annotations.Responses;
import com.openwebserver.services.annotations.Route;
import com.openwebserver.services.objects.Service;


@OpenAPI(tags = "Test")
public class Test extends Service {

    public Test(String path) {
        super(path);
    }

    @OpenAPI(description = "Get root params", summary = "SUMMARY TEST")
    @Responses
    @Parameters(names = "id", in="path", descriptions = "id")
    @CORS(value = "test", overrideOrigin = true)
    @Route(path = "/hello/{id}", method = Method.PUT)
    public Response root(Request request){
        return Response.simple(request.GET);
    }

    @OpenAPI(description = "start session", summary = "SUMMARY TEST")
    @Responses
    @Route(path = "/start", method = Method.GET)
    public Response session(Request request){
        return Response.simple("started").addHeader(new com.openwebserver.core.security.sessions.Session());
    }

    @OpenAPI(description = "POST root params")
    @Responses
    @Session
    @Route(path = "/hello", method = Method.POST, require = {"id", "name", "password"})
    public Response root1(Request request){
        return Response.simple("123");
    }

    @OpenAPI(description = "POST root2 params")
    @Responses
    @Route(path = "/hello", method = Method.DELETE, require = {"id", "name"})
    public Response root2(Request request){
        return Response.simple("123");
    }

}
