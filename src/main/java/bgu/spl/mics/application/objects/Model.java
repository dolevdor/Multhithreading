package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {
    enum Status {
        PreTrained, Training, Trained, Tested
    }

    enum Results {
        None, Good, Bad
    }

    private Data data;
    private String name;
    private Student student;
    private Status status;
    private Results result;

    public Model(Data data, String name, Student student)
    {
        this.data = data;
        this.name = name;
        this.student = student;
        this.status = Status.PreTrained;
        this.result = Results.None;
    }

    public void setStatus(String str){
        if (str.equals("Training"))
            this.status = Status.Training;
        else if (str.equals("Trained"))
            this.status = Status.Trained;
        else if (str.equals("Tested"))
            this.status = Status.Tested;
    }

    public void setResult(String str){
        if(str.equals("Bad"))
            this.result = Results.Bad;
        else if(str.equals("Good"))
            this.result = Results.Good;
    }

    public Data getData() {
        return data;
    }

    public String getName(){
        return name;
    }

    public Student getStudent(){
        return student;
    }

    public int getStatus(){
        if (status == Status.PreTrained)
            return 0;
        if (status == Status.Training)
            return 1;
        if (status == Status.Trained)
            return 2;
        else
            return 3;
    }

    public int getResult(){
        if(result == Results.Good)
            return 1;
        if(result == Results.Bad)
            return 2;
        else
            return 3;
    }
}
