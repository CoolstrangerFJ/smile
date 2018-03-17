/**
 * 
 */
package util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author CoolStranger
 * @date 2018年2月27日
 */
public class HashThreadPoolExecutor {

	private int poolSize;
	private Worker[] workers;

	public HashThreadPoolExecutor(int poolSize) {
		this.poolSize = poolSize;
		buildWorkers();
	}

	private void buildWorkers() {
		workers = new Worker[poolSize];
		for (int i = 0; i < poolSize; i++) {
			BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
			Worker worker = new Worker(queue);
			worker.work();
			workers[i] = worker;
		}
	}

	public Worker submit(Runnable task) {
		int hash = hash(task);
		int index = hash % poolSize;
		
		workers[index].submit(task);
		return workers[index];
	}

	static final int hash(Object key) {
		int h;
		return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
	}

}
