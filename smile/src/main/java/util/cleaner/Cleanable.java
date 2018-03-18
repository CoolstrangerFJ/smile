/**
 * 
 */
package util.cleaner;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import util.Background;

/**
 * 
 * @author CoolStranger
 */
public interface Cleanable {

	static ScheduledThreadPoolExecutor cleaner = Background.getInstance().getPool();

	default void cheakLater(CleanTask cleanTask, long delay) {
		cleaner.schedule(cleanTask, delay, TimeUnit.MILLISECONDS);
	}

	long getLastUsedTime();

	void clean();
}
