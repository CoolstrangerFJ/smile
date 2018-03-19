/**
 * 
 */
package util.cleaner;

import java.lang.ref.WeakReference;

/**
 * 此类用于延时清理过期对象,而且被清理的资源很有可能会被提前清理,之所以使用了弱引用,是为了避免由于提交清理任务到线程池后产生的强引用导致资源无法释放。
 * 如果要提高复用性可以改为泛型,并且做一个Cleanable接口,里面有getLastUsedTime()和clean()方法
 * 不过StaticCache和Session好像都没有必要使用WeakReference,因为它们基本上不会自行清除自己,所以上面的想法暂时没有实现
 * 
 * 已实现上述优化,因为本地文件修改后,缓存中的静态资源需要清除,session也顺带修改了
 * 
 * @author CoolStranger
 * @date 2018年1月11日
 * @time 下午4:35:12
 *
 */
public class CleanTask implements Runnable {
	private WeakReference<Cleanable> ref;
	private long timeOut;
	
	public CleanTask() {
	}

	public CleanTask(Cleanable cleanable, long timeOut) {
		this.ref = new WeakReference<Cleanable>(cleanable);
		this.timeOut = timeOut;
	}

	@Override
	public void run() {
		Cleanable cleanable = ref.get();

		if (cleanable != null) {
			long curTime = System.currentTimeMillis();
			long lastUsed = cleanable.getLastUsedTime();
			if ((curTime - lastUsed) >= timeOut) {
				cleanable.clean();
			}
		}
	}
	
	public CleanTask setTimeOut(long timeOut) {
		this.timeOut = timeOut;
		return this;
	}
	
	public CleanTask setCleanable(Cleanable cleanable) {
		this.ref = new WeakReference<Cleanable>(cleanable);
		return this;
	}
}
