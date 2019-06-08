import Model.Model;
import Model.CustomHeaders;
import Resources.GradesResource;
import Resources.StudentsResource;
import Resources.SubjectsResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RestApi {

    public static void main(String[] args) throws Exception {
        Model model = Model.getModelInstance();
        model.generateData();
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(8000).build();
        ResourceConfig config = new ResourceConfig(StudentsResource.class);
        config.register(DeclarativeLinkingFeature.class);
        config.register(SubjectsResource.class);
        config.register(GradesResource.class);
        config.register(CustomHeaders.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
    }
}


