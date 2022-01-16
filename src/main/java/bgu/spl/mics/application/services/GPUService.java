package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent}
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private LinkedBlockingQueue<DataBatch> processedBatches;
    private LinkedBlockingQueue<DataBatch> unprocessedBatches;
    private GPU gpu;
    private int size;
    private int counter;
    private Cluster cluster;
    private CountDownLatch countDownLatch;

    public GPUService(String name, GPU gpu, CountDownLatch countDownLatch) {
        super(name);
        processedBatches = new LinkedBlockingQueue<DataBatch>(gpu.getAmount());
        unprocessedBatches = new LinkedBlockingQueue<DataBatch>();
        this.gpu = gpu;
        cluster = Cluster.getInstance();
        this.countDownLatch = countDownLatch;
    }

    @Override
    protected void initialize() {
        cluster.registerGPU(this);
        subscribeBroadcast(TerminateBroadcast.class, terminatecallback->{
            terminate();
        });
        subscribeBroadcast(TickBroadcast.class, tickbroadcastBroadcast-> {
            plusTick();
            gpu.setTime();
        });
        subscribeEvent(TrainModelEvent.class, trainmodelEvent->{
            Model model = trainmodelEvent.getModel();
            model.setStatus("Training");
            Data data = model.getData();
            unprocessedBatches = gpu.breakToBatches(data);
            int size = (data.getSize())/1000;

            Thread collector = new Thread(() -> {
                    for (int i = 0; i < size && !this.getTerminated(); i++){
                        try {
                            DataBatch db = cluster.getNextProcessedDB(gpu.getId());
                            if (db!=null)
                                processedBatches.put(db);
                            else
                                i--;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
            });

            Thread send_to_gpu = new Thread(() -> {
                    for (int i = 0; i < size && !this.getTerminated(); i++){
                        DataBatch dataBatch = null;
                        try {
                            dataBatch = processedBatches.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        gpu.process(dataBatch);
                        model.getData().setProcessed(1);
                    }

                    model.setStatus("Trained");
 //               System.out.println("Trained " + model.getName());
                    getMessageBus().complete(trainmodelEvent, model);
            });

            collector.setDaemon(true);
            send_to_gpu.setDaemon(true);

            collector.start();
            send_to_gpu.start();

            while (!unprocessedBatches.isEmpty()) {
                for (int i = 0; i < 100; i++){
                    try {
                        cluster.addDataBatches(unprocessedBatches.take(), gpu.getId());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(4);
                }
                catch(InterruptedException e){};
            }

        });
        subscribeEvent(TestModelEvent.class, testmodelEvent-> {
            gpu.testProcess(testmodelEvent.getModel());
            getMessageBus().complete(testmodelEvent, testmodelEvent.getModel());
        });

        countDownLatch.countDown();
    }
public GPU getGpu(){
        return gpu;
}

    public void setCountDownLatch(CountDownLatch countDownLatch){
        this.countDownLatch = countDownLatch;
    }

}
