
import com.openwebserver.core.WebServer;
import com.openwebserver.core.objects.Domain;
import com.openwebserver.core.routing.Router;
import com.openwebserver.core.security.CORS.Policy;
import com.openwebserver.core.security.CORS.PolicyManager;
import com.openwebserver.openapi.OpenAPI;

public class Main {

    public static void main(String[] args) {

        PolicyManager.Register(new Policy("test")
                .setOrigin("*")
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
