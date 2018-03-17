package connecter;

import connecter.processor.HTTPProcessor;
import manage.Moniter;

/**
 * 服务器运行监视器,一个进程一个单例
 * 
 * @author CoolStranger
 */
public class ServerMoniter implements Moniter {

	private static ServerMoniter instance = new ServerMoniter();
	private Thread concurrentListener;
	private long totalCount;
	private long qps;

	private ServerMoniter() {
		concurrentListener = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						qps = HTTPProcessor.getQPSAndReset();
						totalCount += qps;
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		concurrentListener.setName("concurrentListener");
		concurrentListener.setDaemon(true);
	}

	public void listen() {
		try {
			concurrentListener.start();
		} catch (Exception e) {
		}
	}

	public static ServerMoniter getInstance() {
		return instance;
	}

	@Override
	public long getRuntime() {
		return (System.currentTimeMillis() - HttpConnecterNIO.getInstance().getStartupTime()) / 1000;
	}

	@Override
	public int getConcurrency() {
		return HTTPProcessor.getConcurrency();
	}

	@Override
	public int getQPS() {
		return (int) qps;
	}

	@Override
	public long getTotalCount() {
		return totalCount;
	}
}
