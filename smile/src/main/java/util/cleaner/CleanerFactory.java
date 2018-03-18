package util.cleaner;

public class CleanerFactory {

	private long timeout;
	
	public CleanerFactory(long timeout) {
		this.timeout = timeout;
	}

	public CleanTask getCleanTask(Cleanable cleanable) {
		CleanTask cleanTask = new CleanTask();
		cleanTask.setTimeOut(timeout);
		cleanTask.setCleanable(cleanable);
		return cleanTask;
	}
}
