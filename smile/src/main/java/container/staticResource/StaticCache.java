package container.staticResource;

import java.nio.ByteBuffer;

import launcher.Configuration;
import util.cleaner.CleanTask;
import util.cleaner.Cleanable;
import util.cleaner.CleanerFactory;

public class StaticCache implements Cleanable {

	private static long delay = Configuration.CACHE_MAX_INACTIVE_INTERVAL + 10;
	private static CleanerFactory processorCleanerFactory = new CleanerFactory(Configuration.CACHE_MAX_INACTIVE_INTERVAL);
	private CleanTask cleanTask = processorCleanerFactory.getCleanTask(this);
	private CacheManager cacheManager;
	private String path;
	private ByteBuffer buffer;
	private String name;
	private long lastModified;
	private long lastUsedTime;
	private long size;

	public StaticCache(String path, ByteBuffer buffer, String name, long lastModified, long size,
			CacheManager cacheManager) {
		this.cacheManager = cacheManager;
		this.path = path;
		this.buffer = buffer;
		this.name = name;
		this.lastModified = lastModified;
		this.size = size;
		this.lastUsedTime = System.currentTimeMillis();
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
		this.lastUsedTime = System.currentTimeMillis();
		cheakLater(cleanTask, delay);
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
	public long getLastUsedTime() {
		return lastUsedTime;
	}

	@Override
	public void clean() {
		if (cacheManager.isNeedToClean()) {
			cacheManager.remove(getPath());
			System.out.println("已过时,即将删除：" + this.toString());
		}
	}
}
