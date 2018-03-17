/**
 * 
 */
package container.staticResource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import launcher.Configuration;

/**
 * 缓存管理器
 * 
 * @author CoolStranger
 * @date 2017年12月30日
 * @time 上午11:50:53
 *
 */
public class CacheManager {
	private ConcurrentHashMap<String, StaticCache> cacheMap = new ConcurrentHashMap<>();
	private AtomicLong cacheSize = new AtomicLong();
	private static int minCacheSize = Configuration.minCacheSize;
	private static int maxCacheSize = Configuration.maxCacheSize;
	private ConcurrentHashMap<String, File> staticResourceContent;
	private StaticCache notFound;

	public CacheManager() {
		notFound = loadNotFound();
	}

	private boolean isOverSize(long size) {
		return cacheSize.get() + size > maxCacheSize;
	}

	private synchronized void add(StaticCache cache) {
		long size = cache.getSize();
		if (!isOverSize(size)) {
			cacheMap.put(cache.getPath(), cache);
			cacheSize.addAndGet(size);
		}
	}

	/**
	 * 这里回收缓存的时候不需要考虑线程安全，因为只有一条线程处理后台事务
	 */
	public boolean isNeedToClean() {
		return cacheSize.get() > minCacheSize;
	}

	public void remove(String path) {
		StaticCache remove = cacheMap.remove(path);
		long size = remove.getSize();
		cacheSize.addAndGet(-size);
	}

	public StaticCache getNotFound() {
		return notFound;
	}

	private StaticCache loadNotFound() {
		File resource = new File(Configuration.WEBAPPS + "root/notfound.html");
		if (!resource.exists()) {
			return createNotFound();
		}
		long lastModified = resource.lastModified();
		long size = resource.length();
		String name = resource.getName();
		ByteBuffer buffer = getBuffer(resource);
		StaticCache notFound = new StaticCache("notFound", buffer, name, lastModified, size, this);
		return notFound;
	}

	private StaticCache createNotFound() {
		try {
			byte[] bytes = "<html><body><h1>404 Not Found</h1></body></html>".getBytes("utf-8");
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			String fileName = "notFound.html";
			long createTime = System.currentTimeMillis();
			long size = bytes.length;
			return new StaticCache("notFound", buffer, fileName, createTime, size, this);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

	}

	// 此处待优化,遇到同时请求同一未缓存资源时,会缓存多次浪费性能,需要锁一下,暂时想不到用什么对像锁
	// 对象锁需要满足以下需求：
	// 1、请求同一资源多个request共同拥有
	// 2、该资源目前未缓存
	// 3、不能与请求其他资源的request冲突
	// 已有解决方案一,使用Future存放StaticCache,但是效率不理想
	// 新的方案二,建立一个静态资源白名单,使用白名单中,key为路径,value为相应file对象,可以使用file对象作为锁
	// 下面的实现正是方案二
	public StaticCache getResource(String path) {
		StaticCache cache = cacheMap.get(path);

		if (cache == null) {
			File file = staticResourceContent.get(path);
			// 这里不需要判断是否为空,因为前面已经containKey检查过一次,且map中没有空值

			ByteBuffer buffer = null;
			synchronized (file) {
				//拿到锁以后再get一次
				cache = cacheMap.get(path);
				if (cache == null) {
					// 这里不需要担心doubleCheck问题, 因为这里调用的是一个完整的方法,而不是构造方法,不用担心指令重排
					buffer = getBuffer(file);
					long size = buffer.capacity();
					String name = file.getName();
					long lastModified = file.lastModified();
					cache = new StaticCache(path, buffer, name, lastModified, size, this);
					add(cache);
				}
			}
		}
		return cache;
	}

	private ByteBuffer getBuffer(File file) {
		System.err.println(file.getName() + " loading...");
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			FileChannel channel = randomAccessFile.getChannel();
			long size = channel.size();
			// MappedByteBuffer mappedByteBuffer =
			// channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
			// 这里可以考虑使用直接缓冲区,但是注意：
			// 在转发传入response对象可能是包装类,无法强转为responseWriter后,
			// 直接把缓存块传入response对象,此时直接缓存不能调用array！！！

			ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
//			ByteBuffer buffer = ByteBuffer.allocate((int) size);
			while (buffer.hasRemaining()) {
				channel.read(buffer);
			}

			// buffer.put(mappedByteBuffer);
			buffer.flip();
			channel.close();
			randomAccessFile.close();
			return buffer;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

//	private File getFile(String url) {
//		File resource = null;
//		if (url.equals("/")) {// 首页
//			resource = new File(Configuration.WEBAPPS + "root/index.html");
//		} else {
//			url = url.replaceFirst("/", "");
//			resource = new File(Configuration.WEBAPPS + url);
//			if (resource.exists()) {// 查找静态资源
//				if (resource.isDirectory()) {
//					resource = new File(Configuration.WEBAPPS + url + "/index.html");
//					if (!resource.exists()) {
//						resource = null;
//					}
//				}
//			} else {
//				resource = null;
//			}
//		}
//		return resource;
//	}

	public void setStaticResourceContent(ConcurrentHashMap<String, File> staticResourceContent) {
		this.staticResourceContent = staticResourceContent;
	}

}
