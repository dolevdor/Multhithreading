package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.services.CPUService;
//import jdk.internal.misc.FileSystemOption;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private Cluster cluster;
    private AtomicInteger tick;

    //for Tests
    public AtomicInteger getTick() {
        return tick;
    }

    public CPU(int cores){
        cluster = Cluster.getInstance();
        this.cores = cores;
        tick = new AtomicInteger(1);
    }
    /**
     @PRE: None
     @POST: currTick(pre) + 1 == currTick
     */
    public void setTime() {
        int currTick;
        do {
            currTick = tick.get();
        } while(!tick.compareAndSet(currTick, currTick + 1));
    }
    /**
     @PRE: None
     @POST: currTick(pre) + 4 <= currTick
     */
    public void process() {
        DataBatch db;
        try {
            //hi
            db = cluster.getNextUnprocessedDB();
            int currTick = tick.get();
            int type;
            if (db.getData().getType() == 0)
                type = 4;
            else if (db.getData().getType() == 1)
                type = 2;
            else
                type = 1;
            int toProcess = (32/this.cores) * type;
            int firstTick = tick.get();
            do {
                currTick = tick.get();
            } while(currTick < firstTick + toProcess);

            cluster.addToBatchesGlobal();
            cluster.addProcessedData(db);
            cluster.addToCpuGlobal(toProcess);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void processToTest(DataBatch db) {
            int currTick = tick.get();
            int type;
            if (db.getData().getType() == 0)
                type = 4;
            else if (db.getData().getType() == 1)
                type = 2;
            else
                type = 1;
            int toProcess = (32/this.cores) * type;
            int firstTick = tick.get();
            do {
                currTick = tick.get();
            } while(currTick < firstTick + toProcess);

//            cluster.addToBatchesGlobal();
//            cluster.addProcessedData(db);
//            cluster.addToCpuGlobal(toProcess);
    }
}
