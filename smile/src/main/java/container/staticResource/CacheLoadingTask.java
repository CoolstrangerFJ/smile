package container.staticResource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

import launcher.Configuration;

public class CacheLoadingTask implements Callable<StaticCache> {

	private String path;
	private CacheManager cacheManager;
	
	public CacheLoadingTask(String path, CacheManager cacheManager) {
		this.path = path;
		this.cacheManager = cacheManager;
	}

	@Override
	public StaticCache call() throws Exception {
		File file = getFile(path);
		if (file != null) {
			ByteBuffer buffer = getBuffer(file);
			long size = buffer.capacity();
			String name = file.getName();
			long lastModified = file.lastModified();
			return new StaticCache(path, buffer, name, lastModified, size, cacheManager);
		}
		return null;
	}

	private File getFile(String path) {
		File resource = null;
		if (path.equals("/")) {// 首页
			resource = new File(Configuration.WEBAPPS + "root/index.html");
		} else {
			path = path.replaceFirst("/", "");
			resource = new File(Configuration.WEBAPPS + path);
			if (resource.exists()) {// 查找静态资源
				if (resource.isDirectory()) {
					resource = new File(Configuration.WEBAPPS + path + "/index.html");
					if (!resource.exists()) {
						resource = null;
					}
				}
			} else {
				resource = null;
			}
		}
		return resource;
	}
	
	private ByteBuffer getBuffer(File file) {
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			FileChannel channel = randomAccessFile.getChannel();
			long size = channel.size();
			MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
			ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
			buffer.put(mappedByteBuffer);
			buffer.flip();
			channel.close();
			randomAccessFile.close();
			return buffer;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
