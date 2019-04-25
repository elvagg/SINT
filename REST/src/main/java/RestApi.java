import Model.Model;
import Model.DataSourceConfig;
import Resources.GradesResource;
import Resources.StudentsResource;
import Resources.SubjectsResource;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.mongodb.morphia.Datastore;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RestApi {

    public static void main(String[] args) throws Exception {
        Model model = Model.getModelInstance();
        model.generateData();

//
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(8000).build();
        ResourceConfig config = new ResourceConfig(StudentsResource.class);
        config.register(DeclarativeLinkingFeature.class);
        config.register(SubjectsResource.class);
        config.register(GradesResource.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
    }
}

