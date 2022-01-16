package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private LinkedList<Model> successModels;
    private AtomicInteger tick;
    private boolean published = false;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ConfrenceInformation(String name, int date){
        this.name = name;
        this.date = date;
        successModels = new LinkedList<Model>();
        tick = new AtomicInteger(1);
    }

    public void addToConfrence(Model model){
        successModels.add(model);
    }

    public LinkedList<Model> getModels(){
        return this.successModels;
    }

    public int getDate(){
        return date;
    }

    public String getName(){return name;}

    public void setRunning(){
        running.set(false);
    }

    public AtomicBoolean getRunning() {
        return running;
    }

    public void publish() {
//        while (tick.get() < date && running.get()) {
//                try {
//                    synchronized (tick) {
//                        tick.wait(); // waits for tick thread to notify
//                    }
//                } catch (InterruptedException e) {Thread.currentThread().interrupt();}
        //               System.out.println("I woke up " + tick);
//        }
        int currTick;
        do {
            currTick = tick.get();
        } while(currTick < date);
        published = true;
    }


    public boolean getPublished(){
        return published;
    }

    public void setTime(){
        int currTick;
        do {
            currTick = tick.get();
        } while(!tick.compareAndSet(currTick, currTick + 1));
//            int currTick = tick.get();
//            tick.compareAndSet(currTick, currTick + 1);
//        synchronized (this.tick) {
//            tick.notifyAll();
    }
//    }


    public AtomicInteger getTick(){
        return tick;
    }


}
