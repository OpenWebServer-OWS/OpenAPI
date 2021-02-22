import DevMode.DevConsole;
import com.openwebserver.core.Domain;
import com.openwebserver.core.Routing.Router;
import com.openwebserver.core.WebServer;
import com.openwebserver.utils.OpenAPI.OpenAPI;

import java.net.MalformedURLException;

public class Main {

    public static void main(String[] args) throws MalformedURLException {
        DevConsole.ENABLE = false;
        new WebServer().addDomain(new Domain("http://dev.public.sourceproject.nl/")
                .addHandler(new Test("/"))
                .addHandler(new OpenAPI("OpenAPI Test","Test omschrijving", "1.0.0"))
        ).start();

        Router.print();
    }

}
