package Model;
import Entities.Grade;
import Entities.Sequence;
import Entities.Student;
import Entities.Subject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.text.SimpleDateFormat;
import java.util.*;

public class Model {
    private DataSourceConfig conf = new DataSourceConfig();
    private MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:8004"));
    private Datastore datastore = conf.datastore(client);
    private static Model modelInstance = null;
    private Map<Integer, Student> students = new HashMap<>();
    private Map<Integer, Subject> courses = new HashMap<>();

    public Model(){ }

    public Map<Integer, Student> getStudents(){
        return students;
    }

    public void generateData(){

        if (datastore.getCount(Subject.class) == 0 && datastore.getCount(Student.class) == 0) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            datastore.save(new Sequence(0, 0, 0));
            try {
                int ind = this.nextStudentIndex();
                final Student student1 = new Student(ind, "Paulina", "Warkocka", dateFormat.parse("19-02-1996"));
                ind = this.nextStudentIndex();
                final Student student2 = new Student(ind, "Jan", "Kowalski", dateFormat.parse("12-07-1994"));
                students.put(student1.getStudentId(), student1);
                students.put(student2.getStudentId(), student2);
                datastore.save(student1);
                datastore.save(student2);
                final Query<Student> query = datastore.createQuery(Student.class);
                final List<Student> students_query = query.asList();
                for (Student student : students_query) {
                    System.out.println(student);
                }
                ind = this.nextCourseIndex();
                Subject course1 = new Subject(ind, "Systemy Internetowe", "Tomasz Pawlak");
                ind = this.nextCourseIndex();
                Subject course2 = new Subject(ind, "Uczenie maszynowe", "Jerzy Stefanowski");
                ind = this.nextCourseIndex();
                Subject course3 = new Subject(ind, "Przetwarzanie obrazów", "Jan Nowak");
                courses.put(course1.getSubjectId(), course1);
                courses.put(course2.getSubjectId(), course2);
                courses.put(course3.getSubjectId(), course3);

                datastore.save(course1);
                datastore.save(course2);
                datastore.save(course3);
                ind = this.nextGradeIndex();
                Grade grade1 = new Grade(ind, 2.5f, new Date(), course1, student1.getStudentId());
                ind = this.nextGradeIndex();
                Grade grade2 = new Grade(ind, 4.5f, new Date(), course2, student1.getStudentId());
                ind = this.nextGradeIndex();
                Grade grade3 = new Grade(ind, 3f, new Date(), course3, student2.getStudentId());
                ind = this.nextGradeIndex();
                Grade grade4 = new Grade(ind, 5f, new Date(), course1, student2.getStudentId());
                ArrayList<Grade> gradesStudent1 = new ArrayList<Grade>();
                datastore.save(grade1);
                datastore.save(grade2);
                datastore.save(grade3);
                datastore.save(grade4);
                gradesStudent1.add(grade1);
                gradesStudent1.add(grade2);

                student1.getGrades().add(grade1);
                student1.getGrades().add(grade2);
                student2.getGrades().add(grade3);
                student2.getGrades().add(grade4);
                datastore.save(student1);
                datastore.save(student2);

                student1.setGrades(gradesStudent1);
                ArrayList<Grade> gradesStudent2 = new ArrayList<Grade>();
                gradesStudent2.add(grade3);
                gradesStudent2.add(grade4);
                student2.setGrades(gradesStudent2);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error while creating data");
            }
        }
    }

    public synchronized static Model getModelInstance(){
        if(modelInstance == null){
            modelInstance = new Model();
        }
        return modelInstance;
    }

    public Map<Integer,Subject> getSubjects() {
        return courses;
    }

    public Datastore getDatastore() {
        return this.datastore;
    }

    public int nextStudentIndex() {
        Sequence index = this.datastore.findAndModify(datastore.createQuery(Sequence.class), datastore.createUpdateOperations(Sequence.class).inc("studentIndex", 1));
        return index.getStudentIndex();
    }

    public int nextCourseIndex() {
        Sequence index = this.datastore.findAndModify(datastore.createQuery(Sequence.class), datastore.createUpdateOperations(Sequence.class).inc("courseID", 1));
        return index.getCourseID();
    }

    public int nextGradeIndex() {
        Sequence index = this.datastore.findAndModify(datastore.createQuery(Sequence.class), datastore.createUpdateOperations(Sequence.class).inc("gradeID", 1));
        return index.getGradeID();
    }
}
