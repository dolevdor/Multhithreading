package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.outputs.ConferenceOutput;
import bgu.spl.mics.application.outputs.JSONOutput;
import bgu.spl.mics.application.outputs.ModelOutput;
import bgu.spl.mics.application.outputs.StudentOutput;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link },
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    private ConfrenceInformation conference;
    private CountDownLatch countDownLatch;

    public ConferenceService(String name, ConfrenceInformation conference, CountDownLatch countDownLatch) {
        super(name);
        this.conference = conference;
        this.countDownLatch = countDownLatch;
    }

    protected void initialize() {
//        getMessageBus().register(this);
        subscribeBroadcast(TerminateBroadcast.class, terminatecallback -> {
            conference.setRunning();
            ConferenceOutput conferenceOutput = new ConferenceOutput(conference.getName(),
                    conference.getDate(), getTrainedModels());
            JSONOutput.GetInstance().addConferenceOutputs(conferenceOutput);
            terminate();
        });

        subscribeEvent(PublishResultsEvent.class, publishResultEventcallback -> {
            conference.addToConfrence(publishResultEventcallback.getModel());
        });

        subscribeBroadcast(TickBroadcast.class, tickbroadcastBroadcast -> {
            conference.setTime();
        });

        Thread publish = new Thread(() -> {
            conference.publish();
            sendBroadcast(new PublishConferenceBroadcast(conference));
            if (!conference.getRunning().get())
                getMessageBus().unregister(this);
        });

        publish.setDaemon(true);
        publish.start();

        countDownLatch.countDown();
    }

        public LinkedList<ModelOutput> getTrainedModels () {
            LinkedList<ModelOutput> trainedOutputModels = new LinkedList<>();

            for (Model t : conference.getModels()) {
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


        public void setCountDownLatch (CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }

}

