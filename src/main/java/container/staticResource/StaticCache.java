package container.staticResource;

import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import launcher.Configuration;
import util.Background;

public class StaticCache implements Runnable {

	private static ScheduledThreadPoolExecutor cleanPool = Background.getInstance().getPool();
	private static long checkTimeOut = Configuration.CACHE_MAX_INACTIVE_INTERVAL;
	private static long delay = checkTimeOut + 10;
	private CacheManager cacheManager;
	private String path;
	private ByteBuffer buffer;
	private String name;
	private long lastModified;
	private long lastUsed;
	private long size;

	public StaticCache() {
	}

	public StaticCache(String path, ByteBuffer buffer, String name, long lastModified, long size,
			CacheManager cacheManager) {
		this.cacheManager = cacheManager;
		this.path = path;
		this.buffer = buffer;
		this.name = name;
		this.lastModified = lastModified;
		this.size = size;
		this.lastUsed = System.currentTimeMillis();
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ByteBuffer getBuffer() {
		this.lastUsed = System.currentTimeMillis();
		cleanPool.schedule(this, delay, TimeUnit.MILLISECONDS);
		return buffer.slice();
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public String toString() {
		return "StaticCache [path=" + path + ", buffer=" + buffer + ", name=" + name + ", lastModified=" + lastModified
				+ "]";
	}

	@Override
	public void run() {

		long curTime = System.currentTimeMillis();

		if (!this.cacheManager.isNeedToClean()) {
			return;
		}
		if ((curTime - this.lastUsed) > checkTimeOut) {
			this.cacheManager.remove(getPath());
			System.out.println("已过时,即将删除：" + this.toString());
		}
	}

}
