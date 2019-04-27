package Entities;
import Model.ObjectIdJaxbAdapter;
import Resources.GradesResource;
import Resources.StudentsResource;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.bson.types.ObjectId;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import org.mongodb.morphia.annotations.*;
import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;


@Entity("students")
@XmlRootElement(name="Student")
public class Student {
    @Id
    @XmlTransient
    private ObjectId id;
    private static int lastID = -1;
    private int ID;
    private String name;
    private String surname;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="CET")
    private Date birth_date;
    private ArrayList<Grade> grades = new ArrayList<>();


//    @InjectLinks({
//            @InjectLink(
//                    resource = StudentsResource.class,
//                    method = "getAll",
//                    rel = "allstudents"
//            ),
//            @InjectLink(
//                    resource = GradesResource.class,
//                    method = "getAllGrades",
//                    bindings = @Binding(name = "ID", value = "${instance.getStudentId()}"),
//                    rel = "grades"
//            ),
//            @InjectLink(
//                    resource = StudentsResource.class,
//                    rel = "self",
//                    bindings = @Binding(name = "ID", value = "${instance.getStudentId()}"),
//                    method = "getOne"
//            )
//    })
//    @XmlElement(name="link")
//    @XmlElementWrapper(name = "links")
//    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
//    @Transient
//    List<Link> links;

    public Student(){
    }

    public Student(int index, String name, String surname, Date birthdate){
        this.ID = index;

        this.name = name;
        this.surname = surname;

        this.birth_date = birthdate;
        this.grades = new ArrayList<>();
    }
    @XmlTransient
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getStudentId() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING,
            pattern="yyyy-MM-dd", timezone="CET")
    public Date getBirth_date() {
        return birth_date;
    }

    public ArrayList<Grade> getGrades() {
        return grades;
    }



    public void setStudentId(int id) {
        this.ID = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setBirth_date(Date birthdate) {
        this.birth_date = birthdate;
    }

    public void setGrades(ArrayList<Grade> grades) {
        this.grades = grades;
    }

}
