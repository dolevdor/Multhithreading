package bgu.spl.mics.application.objects;


import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.GPUService;
//import org.graalvm.compiler.nodes.cfg.Block;
//import sun.print.CUPSPrinter;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
	private LinkedList<CPUService> cpuList;
	private LinkedList<GPUService> gpuList;
	private LinkedBlockingQueue<DataBatch> unprocessedDB;
	private ConcurrentHashMap<Integer, LinkedBlockingQueue<DataBatch>> queues_to_send;
	private ConcurrentHashMap<Integer, Integer> batch_GPU_Map;
	private boolean terminated = false;
	private int globalCpuTime=0;
	private int globalGpuTime=0;
	private int batchesGlobal = 0;
//	private LinkedBlockingQueue<DataBatch> temp = new LinkedBlockingQueue();

	private static class ClusterHolder{
		private static Cluster Cluster_instance = new Cluster();
	}
	public static Cluster getInstance()
	{
		return ClusterHolder.Cluster_instance;
	}

	private Cluster(){
		cpuList = new LinkedList<>();
		gpuList = new LinkedList<>();
		List<CPUService> cpuSyncList = Collections.synchronizedList(cpuList);
		List<GPUService> gpuSyncList = Collections.synchronizedList(gpuList);
		unprocessedDB = new LinkedBlockingQueue<>();
		queues_to_send = new ConcurrentHashMap<>();
//		batch_GPU_Map = new ConcurrentHashMap<>();
	}

	public void registerGPU(GPUService gpu){
		gpuList.add(gpu);
		queues_to_send.put(gpu.getGpu().getId(), new LinkedBlockingQueue<DataBatch>());
	}

	public void registerCPU(CPUService cpu){
		cpuList.add(cpu);
	}

	public void addDataBatches(DataBatch db, Integer gpuID){
		try {
			unprocessedDB.put(db);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		batch_GPU_Map.put(db.getGpuPapa(), gpuID);
	}

	public void addProcessedData(DataBatch db) {
//		Integer gpu_papa = batch_GPU_Map.get(db.getGpuPapa());
//		System.out.println(gpu_papa + " Im gpupapa");
//		if (gpu_papa != null) {
//			System.out.println(db.getGpuPapa());
			LinkedBlockingQueue<DataBatch> temp = queues_to_send.get(db.getGpuPapa());
//			if (temp != null) {
//				try {
					(queues_to_send.get(db.getGpuPapa())).add(db);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
//		}
//	}

	public DataBatch getNextProcessedDB(Integer gpuID) throws InterruptedException {
		LinkedBlockingQueue temp = queues_to_send.get(gpuID);
//		if(temp!=null)
			return (DataBatch) temp.take();
//		else
//			return null;
	}

	public DataBatch getNextUnprocessedDB() throws InterruptedException {
//		System.out.println(Thread.currentThread());
			return unprocessedDB.take();
	}

	public void addToCpuGlobal(int toAdd){
		globalCpuTime = globalCpuTime+toAdd;
	}
	public void addToGpuGlobal(int toAdd){
		globalGpuTime = globalGpuTime+toAdd;
	}
	public void addToBatchesGlobal(){
		batchesGlobal++;
	}
	public int getGlobalCpuTime(){ return globalCpuTime;}
	public int getGlobalGpuTime(){ return globalGpuTime;}
	public int getBatchesGlobal(){ return batchesGlobal;}
}
