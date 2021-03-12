import com.openwebserver.core.Objects.Request;
import com.openwebserver.services.Annotations.Route;
import com.openwebserver.utils.OpenAPI.Annotations.OpenAPI;
import com.openwebserver.utils.OpenAPI.Annotations.Parameters;
import com.openwebserver.utils.OpenAPI.Annotations.Responses;


@OpenAPI(tags = "Test")
public class Test extends com.openwebserver.services.Objects.Service {

    public Test(String path) {
        super(path);
    }

    @OpenAPI(description = "Get root params", summary = "SUMMARY TEST")
    @Responses
    @Parameters(names = "id", in="path", descriptions = "id")
    @Route(path = "/hello/{id}", method = Method.GET)
    public com.openwebserver.core.Objects.Response root(Request request){
        return com.openwebserver.core.Objects.Response.simple(request.GET());
    }

    @OpenAPI(description = "POST root params")
    @Responses
    @Route(path = "/hello", method = Method.POST, require = {"id", "name", "password"})
    public com.openwebserver.core.Objects.Response root1(Request request){
        return com.openwebserver.core.Objects.Response.simple("123");
    }

    @OpenAPI(description = "POST root2 params")
    @Responses
    @Route(path = "/hello", method = Method.PUT, require = {"id", "name"})
    public com.openwebserver.core.Objects.Response root2(Request request){
        return com.openwebserver.core.Objects.Response.simple("123");
    }

}
