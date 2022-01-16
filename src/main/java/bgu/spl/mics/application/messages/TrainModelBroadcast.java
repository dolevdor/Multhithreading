package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TrainModelBroadcast implements Broadcast {
    private Student student;
    private Model model;

    public Student getStudent() {
        return student;
    }

    public Model getModel() {
        return model;
    }

    public TrainModelBroadcast(Student student, Model model) {
        this.student = student;
        this.model = model;
    }
}
