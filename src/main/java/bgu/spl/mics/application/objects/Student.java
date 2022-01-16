package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications=0;
    private int papersRead=0;
    private LinkedList<Model> modelsList;

    public Student(String name, String department, String status){
        this.name = name;
        this.department = department;
        if(status.equals("MSc"))
            this.status = Degree.MSc;
        else
            this.status = Degree.PhD;
        modelsList = new LinkedList<>();
    }

    public void addModel(Model model){
        modelsList.add(model);
    }
    public String getName(){
        return name;
    }

    public String getDepartment(){
        return this.department;
    }

    public String getDegree(){
        if(status == Degree.MSc)
            return "MSc";
        else
            return "PhD";
    }

    public void setPublications(int x){
        this.publications = publications + x;
    }

    public void setPapersRead(int x){
        this.papersRead = papersRead + x;
    }

    public int getPapersRead(){
       return papersRead;
    }

    public int getPublications() {
        return publications;
    }

    public LinkedList<Model> getModelsList(){
        return this.modelsList;
    }

}
