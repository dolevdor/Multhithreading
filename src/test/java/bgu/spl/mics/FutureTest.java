package bgu.spl.mics;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {
    private Future<Integer> future;

    @Before
    public void setUp() throws Exception {
        future = new Future<>();
    }


    @Test
    public void testGet() {
        assertFalse(future.isDone());
        future.resolve(3);
        int x = future.get();
        assertTrue(future.isDone());
        assertEquals(3, x);
    }

    @Test
    public void testResolve() {
        int result = 3;
        future.resolve(result);
        assertTrue(future.isDone());
        assertEquals(3, result);
    }

    @Test
    public void testIsDone(){
        assertFalse(future.isDone());
        future.resolve(3);
        assertTrue(future.isDone());
    }

    @Test
    public void testSecondget() {
        assertFalse(future.isDone());
        future.get(100, TimeUnit.MILLISECONDS);
        assertFalse(future.isDone());
        future.resolve(3);
        int result = future.get(100, TimeUnit.MILLISECONDS);
        assertEquals(3, result);
    }

}
