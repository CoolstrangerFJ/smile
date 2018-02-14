/**
 * 
 */
package test;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author CoolStranger
 * @date 2018年1月20日
 * @time 下午7:44:41
 *
 */
public class LockTest {

	private static ReentrantLock lock = new ReentrantLock();
	public static void main(String[] args) throws Exception {
		Thread task1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				lock.lock();
//				while(true){
//					try {
//						Thread.sleep(2000);
						test2(1);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
				lock.unlock();
			}
		});
		task1.start();
		
		Thread.sleep(500);
		
		Thread task2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
						test2(2);
				}
			}
		});
		task2.start();
		
	}
	
	public static void test2(int i){
		try {
			System.out.println(i);
			Thread.sleep(3000);
			System.out.println(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
