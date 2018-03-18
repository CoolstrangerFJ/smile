package container.staticResource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 */
public class FileListener {

	private List<WatcherHolder> holders;
	private StaticResourceManager staticResourceManager;

	private class WatcherHolder {
		WatchService watcher;
		Path parent;
	}

	public FileListener(StaticResourceManager staticResourceManager) {
		this.staticResourceManager = staticResourceManager;
		this.holders = new ArrayList<>();
	}

	public void register(Path path) {
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			WatcherHolder watcherHolder = new WatcherHolder();
			watcherHolder.parent = path;
			watcherHolder.watcher = watcher;
			
			holders.add(watcherHolder);
			System.out.println(path.toString() + "已注册");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void start() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						//每10秒检查一次
						Thread.sleep(500);
						
						for (WatcherHolder watcherHolder : holders) {
							//逐个watcher检查
							WatchKey key = watcherHolder.watcher.poll();
							if (key != null) {
								for (WatchEvent<?> event : key.pollEvents()) {
									Kind<?> kind = event.kind();
									Path parent = watcherHolder.parent;
									System.out.println("parent:" + parent);
									
									Path context = (Path) event.context();
									System.out.println("context: " + context);
									
									System.out.println("resolve: "+parent.resolve(context));
									
									
									URI uri = parent.resolve(context).toUri();
									if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
										System.out.println("create: " + uri);
										staticResourceManager.createResource(uri);
									} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
										System.out.println("modify: " + uri);
										staticResourceManager.modifyResource(uri);
									} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
										System.out.println("delete: " + uri);
										staticResourceManager.removeResource(uri);
									}
								}
								key.reset();
							}
						}
					} catch (InterruptedException e) {
						// 这里不需要处理中断
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.setName("fileListener");
		thread.start();
	}
}
