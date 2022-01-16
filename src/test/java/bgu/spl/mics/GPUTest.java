package bgu.spl.mics;

import bgu.spl.mics.application.objects.*;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

public class GPUTest extends TestCase {
    private GPU gpu;
    private Cluster cluster = Cluster.getInstance();

    @Test
    public void testSetTime() {
        gpu = new GPU(0,1);
        int currTick = gpu.getTick().get();
        gpu.setTime();
        assertEquals(gpu.getTick().get(), currTick+1);
    }

    @Test
    public void testBreakToBatches() {
        gpu = new GPU(0,1);
        Data data = new Data("Images", 10000);
        LinkedBlockingQueue queue = gpu.breakToBatches(data);
        assertEquals(queue.size(), 10);
    }

    @Test
    public void testProcess() {
        gpu = new GPU(0,1);
        int currNum = cluster.getGlobalGpuTime();
        Data data = new Data("Images", 10000);
        DataBatch db = new DataBatch(data, 0, 1);

        Thread t = new Thread(() -> {
            gpu.process(db);
        });

        Thread t2 = new Thread(() -> {
            gpu.setTime();
        });

        t.start();
        t2.start();
        try {
            t.join();
            t2.join();
        }
        catch(Exception e){};

        assertEquals(cluster.getGlobalGpuTime(), currNum+1);
    }

    public void testTestProcess() {
        gpu = new GPU(0,0);
        Model model = new Model(new Data("image",0), "Model1",new Student("Dolevi", "MachineLearning","MSc"));
        gpu.testProcess(model);
        assertFalse(3 == model.getResult());
    }
}