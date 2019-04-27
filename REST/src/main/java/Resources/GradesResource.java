package Resources;
import Entities.Grade;
import Entities.Student;
import Entities.Subject;
import Model.Model;
import com.mongodb.BasicDBObject;
import org.glassfish.grizzly.utils.ArrayUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/students/{ID}/grades")
public class GradesResource {
    private Model model = Model.getModelInstance();

    @GET
    @Path("/")
    @PermitAll
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Grade> getAllGrades(
            @PathParam("ID") String index,
            @QueryParam("subjectId") int subjectId,
            @QueryParam("order") int order,
            @QueryParam("value") float value) {
        Query<Grade> query = model.getDatastore().find(Grade.class).field("student_grade_id").equal(Integer.parseInt(index));
        if (subjectId > 0)
            if (!model.getDatastore().find(Subject.class).field("subject_id").equal(subjectId).asList().isEmpty())
                query.field("subjectId").equal(subjectId);
            else throw new NotFoundException();
        if (value > 0) {
            switch (order) {
                case 0:
                    query.field("value").equal(value); break;
                case 1:
                    query.field("value").greaterThan(value); break;
                case -1:
                    query.field("value").lessThan(value); break;
                default: break;
            }
        }
        if (query.asList().isEmpty()) throw new NotFoundException();
        return query.asList();
    }

    @GET
    @Path("/{grade_id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Grade getGrade(@PathParam("ID") String index,
                                @PathParam("grade_id") String id) {
        final Query<Student> students = model.getDatastore().createQuery(Student.class);
        Student student = students.field("ID").equal(Integer.parseInt(index)).get();
        Grade grade_to_return = null;
        if (student != null) {
            List<Grade> grades = student.getGrades();
            for (Grade grade : grades) {
                if (grade.getGrade_id() == Integer.parseInt(id)) {
                     grade_to_return = grade;
                }
            }
        } else {
            throw new NotFoundException();
        }
        if (grade_to_return != null) {
            return grade_to_return;
        } else {
            throw new NotFoundException();
        }
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response postGrade(@PathParam("ID") String index, Grade grade, @Context UriInfo uriInfo) {
        Query<Student> query = model.getDatastore().createQuery(Student.class).field("ID").equal(Integer.parseInt(index));
        Student student = query.get();
        if (student != null) {
            Float[] possibleValues = {2f, 2.5f, 3f, 3.5f, 4f, 4.5f, 5f};
            if (Arrays.asList(possibleValues).contains(grade.getValue()) && grade.getDate() != null && grade.getSubject() != null) {
                Query<Subject> query_subject = model.getDatastore()
                        .createQuery(Subject.class)
                        .field("subject_id")
                        .equal(Integer.parseInt(Integer.toString(grade.getSubject().getSubjectId())));
                if (query_subject != null) {
                    UpdateOperations<Student> ops = model.getDatastore().createUpdateOperations(Student.class);
                    int ind = model.nextGradeIndex();
                    Grade newGrade = new Grade(ind, grade.getValue(), grade.getDate(), query_subject.get(), student.getStudentId());
                    model.getDatastore().save(newGrade);
                    ops.addToSet("grades", newGrade);
                    model.getDatastore().update(query, ops);
                    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                    builder.path(Integer.toString(newGrade.getGrade_id()));
                    return Response.created(builder.build()).build();
                }
            }
        }
        return Response.status(400).build();
    }

    @PUT
    @Path("/{grade_id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putGrade(@PathParam("ID") String index, @PathParam("grade_id") String id, Grade grade) {
        Student student = model.getDatastore().createQuery(Student.class).field("ID").equal(Integer.parseInt(index)).get();
        if (student != null) {
            List<Grade> grades = student.getGrades();
            Float[] possibleValues = {2f, 2.5f, 3f, 3.5f, 4f, 4.5f, 5f};

            for (Grade grade_it : grades) {
                if (grade_it.getGrade_id() == Integer.parseInt(id)) {
                    if (Arrays.asList(possibleValues).contains(grade.getValue()) && grade.getDate() != null && grade.getSubject() != null) {
                        grade_it.setValue(grade.getValue());
                        grade_it.setDate(grade.getDate());
                        grade_it.setSubject(grade.getSubject());
                        model.getDatastore().save(student);
                        model.getDatastore().save(grade_it);
                        return Response.status(201).entity(grade).build();
                    }
                }
            }
        }
        return Response.status(400).build();
    }

    @DELETE
    @Path("/{grade_id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteGrade(@PathParam("ID") String index, @PathParam("grade_id") String id) {
        Query<Student> query = model.getDatastore().createQuery(Student.class).field("ID").equal(Integer.parseInt(index));
        if (query != null) {
            UpdateOperations<Student> ops = model.getDatastore().createUpdateOperations(Student.class).removeAll("grades", new BasicDBObject("grade_id", Integer.parseInt(id)));
            model.getDatastore().update(query, ops);
            return Response.status(200).build();
        }
        return Response.status(400).build();
    }
}
