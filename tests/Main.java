import com.openwebserver.core.Objects.Domain;
import com.openwebserver.core.Routing.Router;
import com.openwebserver.core.Security.CORS.Policy;
import com.openwebserver.core.Security.CORS.PolicyManager;
import com.openwebserver.core.WebServer;
import com.openwebserver.openapi.OpenAPI;

public class Main {

    public static void main(String[] args) {

        PolicyManager.Register(new Policy("test")
                .setOrigin("http://livelaps.nl")
                .AllowAnyMethods()
                .AllowAnyHeader()
        );

        new WebServer().addDomain(new Domain()
                .addHandler(new Test("/"))
                .addHandler(new OpenAPI("OpenAPI Test","Test omschrijving", "1.0.0"))
        ).start();

        Router.print();
    }

}
