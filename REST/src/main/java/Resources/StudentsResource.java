package Resources;
import Entities.Student;
import Model.Model;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import Model.DateParamConverterProvider;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/students")
public class StudentsResource {
    private Model model = Model.getModelInstance();

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Collection<Student> getAll(
            @Context HttpHeaders httpHeaders,
            @QueryParam("name") String name,
            @QueryParam("surname") String surname,
            @QueryParam("birthDate") String date,
            @QueryParam("order") int order) {
        Date birthDate = new DateParamConverterProvider("yyyy-MM-dd").getConverter(Date.class, Date.class, null).fromString(date);

        Query<Student> query = model.getDatastore().createQuery(Student.class);
        if (name != null && surname != null) {
            query.and(query.criteria("surname").containsIgnoreCase(surname),
                    query.criteria("name").containsIgnoreCase(name));
        } else if (surname != null){
            query.criteria("surname").containsIgnoreCase(surname);
        } else if (name != null) {
            query.criteria("name").containsIgnoreCase(name);
        }
        if (birthDate != null) {
            switch (order) {
                case -1:
                    query.criteria("birth_date").lessThan(birthDate);
                    break;
                case 0 :
                    query.criteria("birth_date").equal(birthDate);
                    break;
                case 1:
                    query.criteria("birth_date").greaterThan(birthDate);
                    break;
                default: break;
            }
        }
        return query.asList();
    }

    @GET
    @Path("/{ID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getOne(@PathParam("ID") String index) {
        Student student = model.getDatastore().createQuery(Student.class).field("ID").equal(Integer.parseInt(index)).get();
        if (student != null) {
            return Response.status(200).entity(student).build();
        }
        return Response.status(404).build();
    }


    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response postStudent(Student entity, @Context UriInfo uriInfo) {
        if (entity.getSurname()!=null && entity.getName()!=null && entity.getBirth_date()!=null) {
            int ind = model.nextStudentIndex();
            Student newStudent = new Student(ind, entity.getName(), entity.getSurname(), entity.getBirth_date());
//            if (entity.getGrades() != null) {
//                newStudent.setGrades(entity.getGrades());
//            }
            model.getDatastore().save(newStudent);

            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(Integer.toString(newStudent.getStudentId()));
            return Response.created(builder.build()).build();
        }
        return Response.status(400).build();
    }

    @PUT
    @Path("/{ID}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putStudent(Student entity, @PathParam("ID") String index) {
        Query<Student> query = model.getDatastore().createQuery(Student.class).field("ID").equal(Integer.parseInt(index));
        if (query != null) {
            if (entity.getSurname()!=null && entity.getName()!=null && entity.getBirth_date()!=null) {
                UpdateOperations<Student> ops = model.getDatastore().createUpdateOperations(Student.class)
                        .set("name", entity.getName())
                        .set("surname", entity.getSurname())
                        .set("birth_date", entity.getBirth_date());
                model.getDatastore().update(query, ops);
                return Response.status(201).entity(entity).build();
            }
        }
        return Response.status(400).build();
    }

    @DELETE
    @Path("/{ID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteStudent(@PathParam("ID") String index) {
        Query<Student> query = model.getDatastore().createQuery(Student.class).field("ID").equal(Integer.parseInt(index));
        if (query != null) {
            model.getDatastore().findAndDelete(query);
            return Response.status(200).build();
        }
        return Response.status(400).build();
    }
}
