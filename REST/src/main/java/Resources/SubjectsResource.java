package Resources;
import Entities.Grade;
import Entities.Student;
import Entities.Subject;
import Model.Model;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Collection;
import java.util.List;

@Path("/subjects")
public class SubjectsResource {
    private Model model = Model.getModelInstance();

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Collection<Subject> getAll(
            @QueryParam("subjectName") String subjectName,
            @QueryParam("teacherName") String teacherName) {
        Query<Subject> query = model.getDatastore().createQuery(Subject.class);
        if (subjectName != null && teacherName != null) {
            query.and(query.criteria("subjectName").containsIgnoreCase(subjectName),
                    query.criteria("teacherName").containsIgnoreCase(teacherName));
        } else if (subjectName != null){
            query.criteria("subjectName").containsIgnoreCase(subjectName);
        } else if (teacherName != null) {
            query.criteria("teacherName").containsIgnoreCase(teacherName);
        }
        return query.asList();
    }

    @GET
    @Path("/{subject_id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Subject getOne(@PathParam("subject_id") String id) {
        Query<Subject> subjects = model.getDatastore().createQuery(Subject.class);
        return subjects.field("subject_id").equal(Integer.parseInt(id)).get();
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response postSubject(Subject subject, @Context UriInfo uriInfo) {
        if (subject.getSubjectName() != null && subject.getTeacherName() != null) {
            int ind = model.nextCourseIndex();
            Subject newSubject = new Subject(ind, subject.getSubjectName(), subject.getTeacherName());
            model.getDatastore().save(newSubject);

            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(Integer.toString(newSubject.getSubjectId()));
            return Response.created(builder.build()).build();
        }
        return Response.status(400).build();
    }

    @PUT
    @Path("/{subject_id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putStudent(Subject subject, @PathParam("subject_id") String index) {
        Query<Subject> query = model.getDatastore().createQuery(Subject.class).field("subject_id").equal(Integer.parseInt(index));
        if (query != null) {
            UpdateOperations<Subject> ops = model.getDatastore().createUpdateOperations(Subject.class).set("subjectName", subject.getSubjectName()).set("teacherName", subject.getTeacherName());
            model.getDatastore().update(query, ops);
            return Response.status(201).entity(subject).build();
        }
        return Response.status(400).build();
    }

    @DELETE
    @Path("/{subject_id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteSubject(@PathParam("subject_id") String id) {
        Query<Subject> query = model.getDatastore().createQuery(Subject.class).field("subject_id").equal(Integer.parseInt(id));
        if (query != null) {
            Query<Student> students_query = model.getDatastore().createQuery(Student.class);
            List<Grade> grades_to_delete = model.getDatastore().createQuery(Grade.class).field("subject").hasThisOne(query.get()).asList();
            UpdateOperations<Student> ops = model.getDatastore().createUpdateOperations(Student.class).removeAll("grades", grades_to_delete);
            model.getDatastore().update(students_query, ops);
            model.getDatastore().findAndDelete(query);
            return Response.status(200).build();
        }
        return Response.status(400).build();
    }
}
