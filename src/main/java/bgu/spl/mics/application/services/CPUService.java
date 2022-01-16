package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.DataBatch;

import java.util.concurrent.CountDownLatch;

/**
 * CPU service is responsible for handling the {@link}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;
    private Cluster cluster;
    private CountDownLatch countDownLatch;

    public CPUService(String name, CPU cpu, CountDownLatch countDownLatch) {
        super(name);
        this.cpu = cpu;
        cluster = Cluster.getInstance();
        this.countDownLatch = countDownLatch;
    }


    @Override
    protected void initialize() {

        cluster.registerCPU(this);

        subscribeBroadcast(TerminateBroadcast.class, terminatecallback->{
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, tickbroadcastBroadcast-> {
         //   plusTick();
//            System.out.println("Im happening!");
            cpu.setTime();
        });

        Thread t = new Thread(() -> {
            while (!getTerminated()) {
         //     System.out.println("cpu thread"+Thread.currentThread()+"process");
                cpu.process();
            }
        });

        t.setDaemon(true);
        t.start();

        countDownLatch.countDown();
    }

    public void setCountDownLatch(CountDownLatch countDownLatch){
        this.countDownLatch = countDownLatch;
    }
}
