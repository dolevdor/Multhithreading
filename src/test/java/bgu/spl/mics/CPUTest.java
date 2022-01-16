package bgu.spl.mics;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class CPUTest extends TestCase {

    private CPU cpu;
    private Cluster cluster = Cluster.getInstance();

//    @Before
//    public void SetUp(){
//       cpu = new CPU(4);
//    }
    /**
     @PRE: None
     @POST: currTick(pre) + 1 == currTick
     */
    @Test
    public void testSetTime() {
        cpu = new CPU(4);
        int currTick = cpu.getTick().get();
        cpu.setTime();
        assertEquals(cpu.getTick().get(), currTick+1);
    }

    @Test
    public void testTestProcess() {
        cpu = new CPU(4);
        Data data = new Data("Images", 10000);
        DataBatch db = new DataBatch(data, 0, 1);

        int startTick = cpu.getTick().get();
        cluster.addDataBatches(new DataBatch(new Data("image",2000), 0,0),0);

        Thread t = new Thread(() -> {
            cpu.processToTest(db);
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++)
            cpu.setTime();
        });

        t.start();
        t2.start();

        try {
            t.join();
            t2.join();
        }
        catch(Exception e){};

        int afterTick = cpu.getTick().get();
        assertTrue(startTick+4 < afterTick);
    }
}