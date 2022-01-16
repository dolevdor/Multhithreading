package bgu.spl.mics;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    private MessageBusImpl messageBus;
    private MicroService m1_register;
    private MicroService m2_register;
    private MicroService m1_notregister;
    private MicroService bl_register;
   // private MicroService bl2_not_register;
    private MicroService eh_register;
    private MicroService eh2_register;
    private Event<String> event1;
    private Event<String> event2;
    private Broadcast broadcast;
    private TestModelEvent testModelEvent;
    private TrainModelEvent trainModelEvent;
    private PublishResultsEvent publishResultsEvent;

    @Before
    public void SetUp(){
        messageBus = messageBus.getInstance();
        m1_register = new ExampleMessageSenderService("eventSender", new String[]{"event"});
        m2_register = new ExampleMessageSenderService("broadcastSender", new String[]{"broadcast"});
        bl_register = new ExampleBroadcastListenerService("listener",new String[]{"1"});
      //  bl2_not_register = new ExampleBroadcastListenerService("listener2",new String[]{"3"});
        eh_register = new ExampleEventHandlerService("handler",new String[]{"1"});
        eh2_register = new ExampleEventHandlerService("handler2",new String[]{"1"});
        m1_notregister = new ExampleMessageSenderService("eventSenderNOTregister", new String[]{"event"});
        messageBus.register(m1_register);
  //      messageBus.register(m1_register);
        messageBus.register(m2_register);
        messageBus.register(bl_register);
     //   messageBus.register(bl2_not_register);
        messageBus.register(eh_register);
        messageBus.register(eh2_register);
        broadcast = new ExampleBroadcast("1");
        event1 = new ExampleEvent("event1");
        event2 = new ExampleEvent("event2");
        Data data = new Data("Images", 1000);
        Model model = new Model(data, "Dolevi", new Student("Rotemi", "Art", "MSc"));
        testModelEvent = new TestModelEvent(model);
        trainModelEvent = new TrainModelEvent(model);
        publishResultsEvent = new PublishResultsEvent(model);
    }

    @Test
    public void testGetInstance() {
        MessageBusImpl mb = messageBus;
        messageBus.getInstance();
        assertEquals(mb, messageBus);
    }

    @Test
    public void testSubscribeEvent() {
        try {
            messageBus.subscribeEvent(ExampleEvent.class, m1_notregister);
            fail("Exception expected!");
        } catch (IllegalStateException e) {
            //success

        }

        messageBus.subscribeEvent(TestModelEvent.class, eh_register);
        messageBus.sendEvent(testModelEvent);
        try {
            assertEquals(testModelEvent, messageBus.awaitMessage(eh_register));
        } catch (InterruptedException e) {
            fail("No Exception expected!");
        }
    }
    @Test
    public void testSubscribeBroadcast(){
        try{
            messageBus.subscribeBroadcast(ExampleBroadcast.class, m1_notregister);
            fail("Exception expected!");
        } catch (IllegalStateException e){
            //success
        }

        messageBus.subscribeBroadcast(ExampleBroadcast.class, bl_register);
        messageBus.sendBroadcast(broadcast);
        try {
            assertEquals(broadcast, messageBus.awaitMessage(bl_register));
        } catch (InterruptedException e) {
            fail("No Exception expected!");
        }
    }

    @Test
    public void testComplete() {
        messageBus.subscribeEvent(ExampleEvent.class, eh_register);
        Future<String> future = messageBus.sendEvent(event1);
        messageBus.complete(event1, "Yes");
        assertTrue(future.isDone());
        assertEquals("Yes", future.get());
    }

    @Test
    public void testSendBroadcast() {
        messageBus.subscribeBroadcast(ExampleBroadcast.class, bl_register);
        messageBus.sendBroadcast(broadcast);
        try {
            assertEquals(broadcast, messageBus.awaitMessage(bl_register));
        } catch (InterruptedException e) {
            fail("No Exception expected!");
        }
    }

        @Test
    public void testSendEvent() {
        messageBus.subscribeEvent(TrainModelEvent.class, eh2_register);
        messageBus.sendEvent(trainModelEvent);
        try {
            assertEquals(trainModelEvent, messageBus.awaitMessage(eh2_register));
        } catch (InterruptedException e) {
            fail("No Exception expected!");
        }
    }

    @Test
    public void testRegister() {
        try {
            messageBus.register(m1_register);
            fail("Exception expected!");
        } catch (IllegalStateException e) {
            //success
        }
        messageBus.register(m1_notregister);
        messageBus.subscribeEvent(ExampleEvent.class, m1_notregister);
    }

    @Test
    public void testUnregister() {
        try {
            messageBus.unregister(m1_notregister);
            fail("Exception expected!");
        } catch (IllegalStateException e) {
        }

        messageBus.unregister(m1_register);

        try {
            messageBus.subscribeEvent(ExampleEvent.class, m1_register);
            fail("Exception expected!");
        } catch (IllegalStateException e) {
            //success
        }
    }

    @Test
    public void testAwaitMessage() {
        try {
            messageBus.awaitMessage(m1_notregister);
            fail("Exception expected!");
        }
        catch (InterruptedException e1) {
           //SUCCESS;
        }
     //   catch (Exception e){}
        ExampleEventHandlerService temp = new ExampleEventHandlerService("marina", new String[]{"1"});
        messageBus.register(temp);

        messageBus.subscribeEvent(PublishResultsEvent.class, temp);
        messageBus.sendEvent(publishResultsEvent);
        try {
            assertEquals(publishResultsEvent, messageBus.awaitMessage(temp));
        }
        catch (InterruptedException e){
            fail("No Exception expected!");
        }

        messageBus.unregister(eh_register);
//        messageBus.subscribeEvent(ExampleEvent.class, eh2_register);
//        try{
//            messageBus.sendEvent(event2);
//            messageBus.awaitMessage(eh2_register);
//            //success
//        }
//        catch (InterruptedException e){
//            fail("Exception expected");
//        }
    }

}

