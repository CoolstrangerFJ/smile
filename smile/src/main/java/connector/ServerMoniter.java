package connector;

import connector.processor.HTTPProcessor;
import connector.selector.LoadBalancer;
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
	private HttpConnectorNIO connector = HttpConnectorNIO.getInstance();
	private LoadBalancer loadBalancer = connector.getAcceptableSelector().getLoadBalancer();

	private ServerMoniter() {
		concurrentListener = new Thread(new Runnable() {

			int timer = 0;

			@Override
			public void run() {
				while (true) {
					try {
						qps = HTTPProcessor.getQPSAndReset();
						totalCount += qps;
						timer++;
						if (timer == 30) {
							timer = 0;
							int concurrency = loadBalancer.getConcurrency();
							HTTPProcessor.validate(concurrency);
						}
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
		return (System.currentTimeMillis() - connector.getStartupTime()) / 1000;
	}

	@Override
	public int getConcurrency() {
		int concurrency = HTTPProcessor.getConcurrency();
		return concurrency > 0 ? concurrency : 1;
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
