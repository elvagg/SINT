package Model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity
public class Sequence {
    @Id
    ObjectId _id;
    private int studentIndex;
    private int courseID;
    private int gradeID;

    public Sequence() {
    }

    public Sequence(int studentIndex, int courseID, int gradeID) {
        this.studentIndex = studentIndex;
        this.courseID = courseID;
        this.gradeID = gradeID;
    }

    public int getCourseID() {
        return courseID;
    }

    public int getGradeID() {
        return gradeID;
    }

    public int getStudentIndex() {
        return studentIndex;
    }
}
