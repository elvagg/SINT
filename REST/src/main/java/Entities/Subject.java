package Entities;
import Model.ObjectIdJaxbAdapter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Entity("subjects")
@XmlRootElement
public class Subject {
    @Id
    @XmlTransient
    private ObjectId id;
    private static int lastID = -1;
    private int subject_id;
    private String subjectName;
    private String teacherName;

    public Subject() {}

    public Subject(int index, String name, String teachername){
        this.subject_id = index;
        this.subjectName = name;
        this.teacherName = teachername;
    }

    @XmlJavaTypeAdapter(ObjectIdJaxbAdapter.class)
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getSubjectId() {
        return this.subject_id;
    }

    public String getSubjectName() {
        return this.subjectName;
    }

    public String getTeacherName() {
        return this.teacherName;
    }

    public void setSubjectId(int id) {
        this.subject_id = id;
    }

    public void setSubjectName(String name) {
        this.subjectName = name;
    }

    public void setTeacherName(String teachername) {
        this.teacherName = teachername;
    }
}
