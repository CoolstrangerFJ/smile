package util;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Background {
	private static Background instance = new Background();
	private ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1);

	private Background() {

	}

	public static Background getInstance(){
		return instance;
	}
	
	public ScheduledThreadPoolExecutor getPool(){
		return pool;
	}
}
