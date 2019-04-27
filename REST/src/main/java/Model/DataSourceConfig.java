package Model;
import Entities.Grade;
import Entities.Student;
import Entities.Subject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import com.mongodb.MongoClient;

public class DataSourceConfig {

    private Morphia morphia() {
        final Morphia morphia = new Morphia();
        morphia.mapPackageFromClass(Student.class);
        morphia.mapPackageFromClass(Grade.class);
        morphia.mapPackageFromClass(Sequence.class);
        morphia.mapPackageFromClass(Subject.class);
        return morphia;
    }

    public Datastore datastore(MongoClient mongoClient) {
        final Datastore datastore = morphia().createDatastore(mongoClient, "paulinawarkockadb");
        datastore.ensureIndexes();
        return datastore;
    }
}
