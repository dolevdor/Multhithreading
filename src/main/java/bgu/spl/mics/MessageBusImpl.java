package bgu.spl.mics;
import bgu.spl.mics.application.services.CPUService;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private static class MessageBusHolder{
		private static MessageBusImpl MessageBus_instance = new MessageBusImpl();
	}

	private ConcurrentHashMap<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventshash;
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcasthash;
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> microServiceQueues;
	private ConcurrentHashMap<Event, Future> futureHashMap;
	private ConcurrentHashMap<MicroService, List<Class<? extends Message>>> micromessages;

	private MessageBusImpl(){
		eventshash = new ConcurrentHashMap<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>>();
		broadcasthash = new ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>>();
		microServiceQueues = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		futureHashMap = new ConcurrentHashMap<Event, Future>();
		micromessages = new ConcurrentHashMap<MicroService, List<Class<? extends Message>>>();

	}

	public static MessageBusImpl getInstance()
	{
		return MessageBusHolder.MessageBus_instance;
	}
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// checks if the microservice is registered
		if(!isregister(m))
			throw new IllegalStateException("can not subscribe something which is not register");
		// don't send a message and subscribe at the same time to the same message type queue
		synchronized (type) {
			ConcurrentLinkedQueue<MicroService> queueevenet = eventshash.get(type);
			// checks if the event type is already in the hashmap. if not - create new queue for it
			if (queueevenet == null) {
				queueevenet = new ConcurrentLinkedQueue<MicroService>();
				eventshash.put(type, queueevenet);
			}
			// checks if the microservice is already subscribed the Event
			if (!queueevenet.contains(m)) {
				queueevenet.add(m);
				micromessages.get(m).add(type);
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// checks if the microservice is registered
		if(!isregister(m))
			throw new IllegalStateException("can not subscribe something which is not register");
		// don't send a message and subscribe at the same time to the same message type queue
		synchronized (type) {
			ConcurrentLinkedQueue<MicroService> broadcastlist = broadcasthash.get(type);
			// checks if the broadcast type is already in the hashmap. if not - create new queue for it
			if (broadcastlist == null) {
				broadcastlist = new ConcurrentLinkedQueue<MicroService>();
				broadcasthash.put(type, broadcastlist);
			}
			// checks if the microservice is already subscribed the Broadcast
			if (!broadcastlist.contains(m)) {
				broadcastlist.add(m);
				micromessages.get(m).add(type);
			}
		}
	}

	public <T> void complete(Event<T> e, T result) {
			Future<T> future = futureHashMap.get(e);
			// checks if event is registered
			if (future == null)
				throw new IllegalStateException("the event is not registered");
			future.resolve(result);
			futureHashMap.remove(e);
	}

	public void sendBroadcast(Broadcast b) {
		// don't send a message and subscribe at the same time to the same message type queue
		synchronized (b.getClass()) {
			Queue<MicroService> blist = broadcasthash.get(b.getClass());
			// checks if the  broadcast is registered. if it is - insert b to all the relevant queues of microservices
			if (blist != null) {
				for (MicroService m : blist) {
					// we don't want a message to be sent while the ms is unregistering
					synchronized (m) {
						if (microServiceQueues.containsKey(m))
							microServiceQueues.get(m).add(b);
					}
				}
			}
		}
	}

	public <T> Future<T> sendEvent(Event<T> e) {
		// don't send a message and subscribe at the same time to the same message type queue
		synchronized(e.getClass()){
			ConcurrentLinkedQueue<MicroService> equeue = eventshash.get(e.getClass());
			// checks if the event is registered. if it is - insert e to the first relevant queues of microservices
			if(equeue != null && !equeue.isEmpty()) {
				while (!equeue.isEmpty()){
					MicroService m = equeue.remove();
					// we don't want a message to be sent while the ms is unregistering
					synchronized (m){
						if(microServiceQueues.containsKey(m)){
							Future<T> future = new Future<T>();
							futureHashMap.put(e, future);
							microServiceQueues.get(m).add(e);
							// returns the m to the queue so it will be the last one which will get the event next time
							equeue.add(m);
							return future;
						}
					}
				}
			}
			// if there is no one that can take the event
			return null;
		}
	}

	public void register(MicroService m) {
		if(isregister(m))
			throw new IllegalStateException("the microservice is already registered");
		microServiceQueues.put(m, new LinkedBlockingQueue<Message>());
		micromessages.put(m, new LinkedList<Class<? extends Message>>());

	}

	private boolean isregister(MicroService m) {
		return microServiceQueues.containsKey(m);
	}

	public void unregister(MicroService m) {
		if(!isregister(m))
			throw new IllegalStateException("the microservice is not registered");
		// we don't want a message to be sent while the ms is unregistering
		synchronized (m){
			// removes m message queue
			microServiceQueues.remove(m);
			// removes m from all 'subscribe' queues and lists
			for(Class<? extends Message> cmessage: micromessages.get(m)){
				if(eventshash.containsKey(cmessage)){
					eventshash.get(cmessage).remove(m);
				}
				else if(broadcasthash.containsKey(cmessage)){
					broadcasthash.get(cmessage).remove(m);
				}
			}
			micromessages.remove(m);
		}
	}

	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(!isregister(m)){
			throw new InterruptedException("the microservice is not registered");}
		// the  blocking queue waits until there will be a message in the queue - not empty
		return microServiceQueues.get(m).take();
	}
}
