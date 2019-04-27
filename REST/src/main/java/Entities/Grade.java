package Entities;
import Model.ObjectIdJaxbAdapter;
import Resources.GradesResource;
import Resources.StudentsResource;
import Resources.SubjectsResource;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.bson.types.ObjectId;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;

@Entity("grades")
@XmlRootElement
public class Grade {

//    @InjectLinks({
//            @InjectLink(
//                    resource = SubjectsResource.class,
//                    method = "getOne",
//                    bindings = @Binding(name = "subject_id", value = "${instance.subject.getSubjectId()}"),
//                    rel = "subject"
//            ),
//            @InjectLink(
//                    resource = StudentsResource.class,
//                    method = "getOne",
//                    bindings = @Binding(name = "ID", value = "${instance.getStudentId()}"),
//                    rel = "student"),
//            @InjectLink(
//                    resource = GradesResource.class,
//                    method = "getGrade",
//                    bindings = {
//                            @Binding(name = "grade_id", value = "${instance.getGrade_id()}"),
//                            @Binding(name = "ID", value = "${instance.getStudentId()}")},
//                    rel = "student"
//            )
//    })
//    @XmlElement(name="link")
//    @XmlElementWrapper(name = "links")
//    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
//    @Transient
//    List<Link> links;

    @Id
    @XmlTransient
    private ObjectId id;
    private static int lastID = -1;
    private int grade_id;
    private float value;
    private int student_grade_id;
    private int subjectId;

    @JsonFormat(shape=JsonFormat.Shape.STRING,
            pattern="yyyy-MM-dd", timezone="CET")
    private Date date;
    @Reference
    private Subject subject;

    public Grade() {}

    public Grade(int index, float value, Date date, Subject subject, int id){
        this.value = value;
        this.grade_id = index;
        this.date = date;
        this.subject = subject;
        this.student_grade_id = id;
        this.subjectId = subject.getSubjectId();
    }
//    @XmlTransient
//    @XmlJavaTypeAdapter(ObjectIdJaxbAdapter.class)
//    public ObjectId getId() {
//        return id;
//    }
//
//    public void setId(ObjectId id) {
//        this.id = id;
//    }

//    public int getSubjectId() { return this.subjectId;}
//    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getStudentId() {return this.student_grade_id;}
    public int getGrade_id() {
        return this.grade_id;
    }

    public float getValue() {
        return this.value;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getDate() {
        return this.date;
    }

    public Subject getSubject() {
        return this.subject;
    }



    public void setStudentId(int id) {this.student_grade_id = id;}
    public void setGrade_id(int id) {
        this.grade_id = id;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

}
