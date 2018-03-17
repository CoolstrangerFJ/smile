/**
 * 
 */
package container.staticResource;

//import java.io.File;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.nio.ByteBuffer;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Future;
//import java.util.concurrent.FutureTask;
//import java.util.concurrent.atomic.AtomicLong;
//
//import launcher.Configuration;

/**
 * 缓存管理器
 * 
 * @author CoolStranger
 * @date 2017年12月30日
 * @time 上午11:50:53
 *
 */
public class CacheManager1 {
//	private ConcurrentHashMap<String, Future<StaticCache>> cacheMap = new ConcurrentHashMap<>();
//	private AtomicLong cacheSize = new AtomicLong();
//	private StaticCache notFound = loadNotFound();
//	private static int minCacheSize = Configuration.minCacheSize;
//	private static int maxCacheSize = Configuration.maxCacheSize;
//
//	private boolean isOverSize() {
//		return cacheSize.get() > maxCacheSize;
//	}
//
//	public boolean isNeedToClean() {
//		return cacheSize.get() > minCacheSize;
//	}
//
//	public void remove(String path) {
//		StaticCache remove = null;
//		try {
//			remove = cacheMap.remove(path).get();
//			long size = remove.getSize();
//			cacheSize.addAndGet(-size);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public StaticCache getNotFound() {
//		return notFound;
//	}
//
//	private StaticCache loadNotFound() {
//		File resource = new File(Configuration.WEBAPPS + "root/notfound.html");
//		StaticCache notFound = null;
//		if (resource.exists()) {
//			long lastModified = resource.lastModified();
//			long size = resource.length();
//			String name = resource.getName();
//			ByteBuffer buffer = getBuffer(resource);
//			notFound = new StaticCache("notFound", buffer, name, lastModified, size, this);
//		} else {
//			long lastModified = System.currentTimeMillis();
//			byte[] buf = "<html><body><h1>404 Not Found</h1></body></html>".getBytes();
//			long size = buf.length;
//			String name = "notFound.html";
//			ByteBuffer buffer = ByteBuffer.wrap(buf);
//			notFound = new StaticCache("notFound", buffer, name, lastModified, size, this);
//		}
//		return notFound;
//	}
//
//	public StaticCache getResource(String path) {
//		StaticCache cache = null;
//		Future<StaticCache> future = cacheMap.get(path);
//		if (future == null) {
//			Callable<StaticCache> task = new Callable<StaticCache>() {
//
//				@Override
//				public StaticCache call() throws Exception {
//					File file = getFile(path);
//					if (file != null) {
//						ByteBuffer buffer = getBuffer(file);
//						long size = buffer.capacity();
//						String name = file.getName();
//						long lastModified = file.lastModified();
//						return new StaticCache(path, buffer, name, lastModified, size, CacheManager.this);
//					}
//					cacheMap.remove(path);
//					return null;
//				}
//			};
//			FutureTask<StaticCache> futureTask = new FutureTask<>(task);
//			future = cacheMap.putIfAbsent(path, futureTask);
//			if (future == null) {
//				future = futureTask;
//				futureTask.run();
//			}
//			try {
//				cache = future.get();
//			} catch (Exception e) {
//				e.printStackTrace();
//				return null;
//			}
//			if (cache == null || isOverSize()) {
//				cacheMap.remove(path);
//			} else {
//				long size = cache.getSize();
//				cacheSize.addAndGet(size);
//			}
//			return cache;
//		} else {
//			try {
//				return future.get();
//			} catch (Exception e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
//		// 此处待优化,遇到同时请求同一未缓存资源时,会缓存多次浪费性能,需要锁一下,暂时想不到用什么对像锁
//		// 对象锁需要满足以下需求：
//		// 1、请求同一资源多个request共同拥有
//		// 2、该资源目前未缓存
//		// 3、不能与请求其他资源的request冲突
//		// 已有解决方案,使用future存放StaticCache
//		// ByteBuffer buffer = null;
//		// buffer = getBuffer(resFile);
//		// long size = buffer.capacity();
//		// String name = resFile.getName();
//		// long lastModified = resFile.lastModified();
//		// cache = new StaticCache(path, buffer, name, lastModified, size,
//		// this);
//	}
//
//	private ByteBuffer getBuffer(File file) {
//		try {
//			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
//			FileChannel channel = randomAccessFile.getChannel();
//			long size = channel.size();
//			MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
//			ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
//			buffer.put(mappedByteBuffer);
//			buffer.flip();
//			channel.close();
//			randomAccessFile.close();
//			return buffer;
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private File getFile(String path) {
//		File resource = null;
//		if (path.equals("/")) {// 首页
//			resource = new File(Configuration.WEBAPPS + "root/index.html");
//		} else {
//			path = path.replaceFirst("/", "");
//			resource = new File(Configuration.WEBAPPS + path);
//			if (resource.exists()) {// 查找静态资源
//				if (resource.isDirectory()) {
//					resource = new File(Configuration.WEBAPPS + path + "/index.html");
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

}
