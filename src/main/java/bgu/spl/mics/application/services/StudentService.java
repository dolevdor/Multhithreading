package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.outputs.JSONOutput;
import bgu.spl.mics.application.outputs.ModelOutput;
import bgu.spl.mics.application.outputs.StudentOutput;
//import com.sun.org.apache.xpath.internal.operations.Mod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    private Student student;
    private LinkedList<Model> trainedModels;

    public StudentService(String name, Student student) {
        super(name);
        this.student = student;
        int size = this.student.getModelsList().size();
        trainedModels = new LinkedList<>();
    }

    public Student getStudent(){
        return student;
    }

    public LinkedList<ModelOutput> getTrainedModels() {
        LinkedList<ModelOutput> trainedOutputModels = new LinkedList<>();

        for (Model t : trainedModels) {
            if (t != null) {
                Model model = t;
                String status = "PreTrained";
                if (model.getStatus() == 1)
                    status = "Training";
                else if (model.getStatus() == 2)
                    status = "Trained";
                else if (model.getStatus() == 3)
                    status = "Tested";

                String result = "None";
                if (model.getResult() == 1)
                    result = "Good";
                else if (model.getResult() == 2)
                    result = "Bad";

                ModelOutput modelOutput = new ModelOutput(model.getName(), model.getData(),
                        status, result);
                trainedOutputModels.add(modelOutput);
            }
        }
        return trainedOutputModels;
    }

    protected void initialize() {
        subscribeBroadcast(TerminateBroadcast.class, terminatecallback -> {
            terminate();
        });

        subscribeBroadcast(PublishConferenceBroadcast.class, publishConfrencecallback -> {
            LinkedList<Model> modelsList = publishConfrencecallback.getConference().getModels();

            int myModels = 0;
            int otherModels = 0;

            for (int i = 0; i < modelsList.size(); i++) {
                Model model = modelsList.get(i);
                if (model.getStudent() == student)
                    myModels++;
                else
                    otherModels++;
            }

            student.setPublications(myModels);
            student.setPapersRead(otherModels);

        });
        Thread t = new Thread(() ->
        {
            LinkedList<Model> myModels = student.getModelsList();
            Iterator<Model> it = myModels.iterator();
            while (it.hasNext()) {
                Model currentM = it.next();
                Future<Model> f_trained = sendEvent(new TrainModelEvent(currentM));
                if (f_trained != null) {
                    currentM = f_trained.get();
                    Future<Model> f_tested = sendEvent(new TestModelEvent(currentM));
                    if (f_tested != null) {
                        currentM = f_tested.get();
                        trainedModels.add(currentM);
                    }
                }

                if (currentM.getResult() == 1)
                    sendEvent(new PublishResultsEvent(currentM));

            }
        });
        t.setDaemon(true);
        t.start();

    }
}