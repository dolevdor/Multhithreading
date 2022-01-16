package bgu.spl.mics.application.objects;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;
    private Vector<DataBatch> dataBatchVector;
    private int amount;
    private int id;
    private AtomicInteger tick;

    public GPU(int typeNum, int id) {
        tick = new AtomicInteger(1);
        cluster = cluster.getInstance();
        if (typeNum == 2) {
            amount = 8;
            this.type = Type.GTX1080;
        }
        if (typeNum == 1) {
            amount = 16;
            this.type = Type.RTX2080;
        }
        if (typeNum == 0) {
            amount = 32;
            this.type = Type.RTX3090;
        }

        this.id = id;
    }

    public void setTime() {
//        synchronized (this.tick) {
//            this.tick = tick++;
//            tick.notifyAll();
//        }
        int currTick;
        do {
            currTick = tick.get();
        } while(!tick.compareAndSet(currTick, currTick + 1));
//            tick.notifyAll();
    }
//    }


    public Type getType() {
        return type;
    }

    public int getId(){
        return id;
    }

    public Model getModel(){
        return model;
    }

    public DataBatch getDataBatch(int index){
        return dataBatchVector.get(index);
    }

    public int getAmount(){
        return amount;
    }

    public int getVectorSize(){
        return dataBatchVector.size();
    }

    public void setModel(Model model){
        this.model = model;
    }

    /**
     * @RETURN: a blocking-queue of size (data.getSize())/1000
     * @param data
     */
    public LinkedBlockingQueue<DataBatch> breakToBatches(Data data) {
        LinkedBlockingQueue<DataBatch> toReturn = new LinkedBlockingQueue<DataBatch>();
        double size = data.getSize();
        int numOfBatches = (int)Math.round(size/1000);
        int counter = 0;
        while (numOfBatches > 0){
            DataBatch dataBatch = new DataBatch(data, counter, getId());
            toReturn.add(dataBatch);
            counter = counter+1000;
            numOfBatches--;
        }
//        System.out.println(Thread.currentThread());
        return toReturn;
    }

    /**
     * @PRE: None
     * @POST: cluster.globalGpuTime(pre) == cluster.globalGpuTime(pre) + timeToWait;
     * @param db
     */
    public void process(DataBatch db) {
//        System.out.println(tick);
        int currTick = tick.get();
        int timeToWait;
        if (amount == 8)
            timeToWait = 4;
        else if (amount == 16)
            timeToWait = 2;
        else
            timeToWait = 1;

        //                     System.out.println(tick);
        int firstTick = tick.get();
        do {
            currTick = tick.get();
        } while(currTick < firstTick + timeToWait);
//            while (tick.get() < currTick + timeToWait) {
//                synchronized (this.tick) {
//                    try {
//                     tick.wait(); //waits for tick thread to notify
//                   } catch (InterruptedException e) {
//                }
        //           }
//        while(!tick.compareAndSet(currTick + timeToWait, currTick + timeToWait));
        cluster.addToGpuGlobal(timeToWait);
//        System.out.println(tick);
    }
//        }
//    }

    /**
     @PRE: model.getResult() == None
     @POST: model.getResult() == Good/Bad
     */
    public void testProcess(Model model) {
        Double chance = Math.random();
        if (model.getStudent().getDegree().equals("MSc")) {
            if (chance < 0.6)
                model.setResult("Good");
            else
                model.setResult("Bad");
        } else if (model.getStudent().getDegree().equals("PhD")) {
            if (chance < 0.8)
                model.setResult("Good");
            else
                model.setResult("Bad");
        }
    }
    //for Tests
    public AtomicInteger getTick() {
        return tick;
    }

}

