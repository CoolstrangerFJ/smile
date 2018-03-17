/**
 * 
 */
package connector.processor;

import java.io.IOException;
import java.lang.ref.WeakReference;

import launcher.Configuration;

/**
 * 此类用于延时清理过期对象,而且被清理的资源很有可能会被提前清理,之所以使用了弱引用,是为了避免由于提交清理任务到线程池后产生的强引用导致资源无法释放。
 * 如果要提高复用性可以改为泛型,并且做一个Cleanable接口,里面有getLastUsedTime()和clean()方法
 * 不过StaticCache和Session好像都没有必要使用WeakReference,因为它们基本上不会自行清除自己,所以上面的想法暂时没有实现
 * 
 * @author CoolStranger
 * @date 2018年1月11日
 * @time 下午4:35:12
 *
 */
public class CleanTask implements Runnable {
	private WeakReference<HTTPProcessor> ref;
	private static long processorTimeOut = Configuration.processorTimeOut * 1000;

	public CleanTask(HTTPProcessor processor) {
		this.ref = new WeakReference<HTTPProcessor>(processor);
	}

	@Override
	public void run() {
		HTTPProcessor processor = ref.get();

		if (processor != null) {
			long curTime = System.currentTimeMillis();
			long lastUsed = processor.getLastUsed();
			if ((curTime - lastUsed) >= processorTimeOut) {
				try {
					boolean connected = processor.getSocketChannel().isConnected();
					processor.close(connected);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
