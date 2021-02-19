import com.openwebserver.core.Domain;
import com.openwebserver.core.Routing.Router;
import com.openwebserver.core.WebServer;
import com.openwebserver.utils.OpenAPI.OpenAPI;

import java.net.MalformedURLException;

public class Main {

    public static void main(String[] args) throws MalformedURLException {
        new WebServer().addDomain(new Domain("http://192.168.1.16")
                .addHandler(new Test("/"))
                .addHandler(new OpenAPI("OpenAPI Test","Test omschrijving", "1.0.0"))
        ).start();

        Router.print();
    }

}
