/**
 * 
 */
package util;

import java.util.concurrent.BlockingQueue;

/**
 * @author CoolStranger
 * @date 2018年2月27日
 */
public class Worker implements Runnable{
	private BlockingQueue<Runnable> queue;
	private Thread thread;
	
	public Worker(BlockingQueue<Runnable> queue) {
		this.queue = queue;
	}
	
	public void work(){
		this.thread = new Thread(this);
		thread.start();
	}

	public void run() {
		while(true){
			try {
				Runnable task = queue.take();
				task.run();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void submit(Runnable task){
		queue.add(task);
	}
}
