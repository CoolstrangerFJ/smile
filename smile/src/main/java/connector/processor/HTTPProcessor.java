package connector.processor;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import connector.parser.IRequestParser;
import connector.parser.ParserFactory;
import connector.request.Request;
import connector.response.Response;
import connector.response.ResponseFactory;
import connector.response.ResponseWriter;
import connector.selector.OutputSelector;
import container.Container;
import launcher.Configuration;
import util.HashThreadPoolExecutor;
import util.Worker;
import util.cleaner.CleanTask;
import util.cleaner.Cleanable;
import util.cleaner.CleanerFactory;

/**
 * 这类用于多个线程组之间通信,保存工作状态
 * 
 * @author CoolStranger
 */
public class HTTPProcessor implements IProcessor, Cleanable {

	private static AtomicInteger concurrency = new AtomicInteger();
	private static LongAdder qps = new LongAdder();
	// private static ExecutorService pool;
	private static HashThreadPoolExecutor pool;
	private Worker worker;
	private static final int SECOND = 1000;
	private static long delay = Configuration.processorTimeOut * SECOND;
	private static Container container = Container.getInstance();
	private static CleanerFactory processorCleanerFactory = new CleanerFactory(Configuration.processorTimeOut * 1000);
	private CleanTask cleanTask;
	private SocketChannel socketChannel;
	private OutputSelector outputSelector;
	private ConcurrentLinkedQueue<Request> reqQueue = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<ResponseWriter> resQueue = new ConcurrentLinkedQueue<>();
	private IRequestParser parser;
	private long lastUsedTime;
	private int invalidCount;

	static {
		if (Configuration.processorThreadPoolSize > 0) {
			pool = new HashThreadPoolExecutor(Configuration.processorThreadPoolSize);

		} else {
			throw new RuntimeException();
		}
	}

	public HTTPProcessor(SocketChannel socketChannel, OutputSelector outputSelector) {
		this.socketChannel = socketChannel;
		parser = ParserFactory.createParser(socketChannel);
		this.outputSelector = outputSelector;
		this.cleanTask = processorCleanerFactory.getCleanTask(this);
		updateLastUsed();
		concurrency.incrementAndGet();
	}

	/**
	 * @see connector.processor.IProcessor#tryRead()
	 */
	@Override
	public void tryRead() {
		try {
			if (parser.read()) {
				reset();

				boolean hasReq = false;
				Request request = null;
				while (true) {
					request = parser.getRequest();

					if (request == null) {
						break;
					}
					hasReq = true;

					reqQueue.add(request);
				}

				if (hasReq) {
					if (worker == null) {
						worker = pool.submit(this);
					} else {
						worker.submit(this);
					}
				}
			} else {
				invalidIncrease();
			}
			updateLastUsed();
		} catch (Throwable e) {
			exceptionHandler(e);
		}
	}

	/**
	 *
	 * @throws IOException
	 * @see connector.processor.IProcessor#process()
	 */
	@Override
	public void process() throws IOException {
		Request request = null;
		while ((request = reqQueue.peek()) != null) {
			Response response = ResponseFactory.createResponse(socketChannel, request);
			container.handle(request, response);
			reqQueue.poll();
			response.ready4Write();
			resQueue.add(response);
			outputSelector.addRegisterTask(this);
			updateLastUsed();
		}
	}

	/**
	 *
	 * @throws IOException
	 * @see connector.processor.IProcessor#tryWrite()
	 */
	@Override
	public void tryWrite() {
		try {
			ResponseWriter peekWriter = null;
			boolean done = false;

			while ((peekWriter = resQueue.peek()) != null) {
				done = peekWriter.bufferToChannel();

				if (done) {
					if (!peekWriter.isConnectionKeepALive()) {
						close(true);
					}
					resQueue.poll();
					qps.increment();
				} else {
					break;
				}
			}

			if (done) {
				SelectionKey key = socketChannel.keyFor(outputSelector.getSelector());
				key.cancel();
			}
			updateLastUsed();
		} catch (Throwable e) {
			exceptionHandler(e);
		}
	}

	// @Override
	// public void run() {
	// // int activeCount = Thread.activeCount();
	// // System.out.println("activeCount: "+activeCount);
	// if (isProcessing.compareAndSet(false, true)) {
	// try {
	// process();
	// /*
	// * 有可能执行到这个地方的时候，又有新的请求加入到队列并提交了任务，然而调用run方法的时候(isProcessing==
	// * true)没有获得执行权，直接跳过，所以需要在finally中检查队列是否为空,若不为空再提交一次任务
	// */
	// } catch (Throwable e) {
	// exceptionHandler(e);
	// } finally {
	// isProcessing.set(false);
	// // 检查队列是否为空,若不为空再提交一次任务
	// if (reqQueue.peek() != null) {
	// pool.submit(this);
	// System.out.println("再次提交任务");
	// }
	// }
	// }
	// }

	@Override
	public void run() {
		try {
			process();
		} catch (Throwable e) {
			exceptionHandler(e);
		}
	}

	/**
	 * @param e
	 */
	private void exceptionHandler(Throwable e) {
		try {
			close(!(e instanceof ClosedChannelException));
			System.out.println("通道已被移除");
		} catch (IOException e1) {
			System.out.println("关闭通道出现异常！");
			// e1.printStackTrace();
		}
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public long getLastUsed() {
		return lastUsedTime;
	}

	public void updateLastUsed() {
		this.lastUsedTime = System.currentTimeMillis();
		cheakLater(cleanTask, delay);
	}

	/**
	 * @throws IOException
	 *
	 */
	public void close(boolean isDec) throws IOException {
		socketChannel.close();
		if (isDec) {
			concurrency.decrementAndGet();
		}
	}

	public void invalidIncrease() {
		invalidCount++;
		// System.out.println("invalid");
		if (invalidCount > 3 && reqQueue.isEmpty() && resQueue.isEmpty()) {
			try {
				// System.out.println("close!");
				close(true);
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
	}

	public void reset() {
		invalidCount = 0;
	}

	public static int getConcurrency() {
		return concurrency.get();
	}

	public static void validate(int newConcurrency) {
		concurrency.set(newConcurrency);
	}

	public static long getQPSAndReset() {
		return qps.sumThenReset();
	}

	@Override
	public long getLastUsedTime() {
		return this.lastUsedTime;
	}

	@Override
	public void clean() {
		boolean connected = socketChannel.isConnected();
		try {
			close(connected);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
