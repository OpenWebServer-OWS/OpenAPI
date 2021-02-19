import com.openwebserver.core.Objects.Request;
import com.openwebserver.services.Annotations.Route;
import com.openwebserver.utils.OpenAPI.Annotations.OpenAPI;
import com.openwebserver.utils.OpenAPI.Annotations.Responses;
import com.openwebserver.utils.OpenAPI.Annotations.Summary;

@OpenAPI(tags = "Test")
public class Test extends com.openwebserver.services.Objects.Service {

    @Summary("default")
    @OpenAPI(value = "Test Service", description = "Testing service")
    public Test(String path) {
        super(path);
    }

    @OpenAPI(description = "Get root params")
    @Responses
    @Route(path = "/hello/{id}", method = Method.GET)
    public com.openwebserver.core.Objects.Response root(Request request){
        return com.openwebserver.core.Objects.Response.simple(request.GET());
    }

    @OpenAPI(description = "POST root params")
    @Responses(descriptions = {"123"})
    @Route(path = "/hello", method = Method.POST)
    public com.openwebserver.core.Objects.Response root1(Request request){
        return com.openwebserver.core.Objects.Response.simple("123");
    }

}
