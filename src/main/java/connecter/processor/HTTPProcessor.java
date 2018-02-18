/**
 * 
 */
package connecter.processor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import connecter.parser.IRequestParser;
import connecter.request.Request;
import connecter.response.Response;
import connecter.response.ResponseWriter;
import connecter.selector.OutputSelector;
import container.Container;
import factory.ParserFactory;
import factory.ResponseFactory;
import launcher.Configuration;
import util.Background;

/**
 * @author CoolStranger
 * @date 2017年12月26日
 * @time 下午11:55:10
 *
 */
public class HTTPProcessor implements IProcessor {

	private static ExecutorService pool = Executors.newFixedThreadPool(Configuration.processorThreadPoolSize);
	private static ScheduledThreadPoolExecutor cleaner = Background.getInstance().getPool();
	private static final int SECOND = 1000;
	private static long delay = Configuration.processorTimeOut * SECOND;
	private static Container container = Container.getInstance();
	private SocketChannel socketChannel;
	private OutputSelector outputSelector;
	private ConcurrentLinkedQueue<Request> reqQueue = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<ResponseWriter> resQueue = new ConcurrentLinkedQueue<>();
	private IRequestParser parser;
	private CleanTask cleanTask;
	private long lastUsed;
	private AtomicBoolean isProcessing = new AtomicBoolean();
	private int invalidCount;

	static {
		if (Configuration.processorThreadPoolSize > 0) {
			pool = Executors.newFixedThreadPool(Configuration.processorThreadPoolSize);
		} else {
			pool = Executors.newCachedThreadPool();
		}
	}

	public HTTPProcessor(SocketChannel socketChannel, OutputSelector outputSelector) {
		this.socketChannel = socketChannel;
		parser = ParserFactory.createParser(socketChannel);
		this.outputSelector = outputSelector;
		this.cleanTask = new CleanTask(this);
		updateLastUsed();
	}

	/**
	 * @see connecter.processor.IProcessor#tryRead()
	 */
	@Override
	public void tryRead() {
		try {
			if (parser.read()) {
				reset();

				Request request = null;
				while (true) {
					request = parser.getRequest();

					if (request == null) {
						break;
					}
					// System.out.println("submit!");
					reqQueue.add(request);
					pool.submit(this);
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
	 * @see connecter.processor.IProcessor#process()
	 */
	@Override
	public void process() throws IOException {
		Request request = null;
		while ((request = reqQueue.peek()) != null) {
			Response response = ResponseFactory.createResponse(socketChannel,request);
			container.handle(request, response);
//			String uri = request.getRequestURI();
//			RequestDispatcher dispatcher = request.getRequestDispatcher(uri);
//			try {
//				// System.out.println("forwarding!");
//				dispatcher.forward(request, response);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
			reqQueue.poll();
			response.ready4Write();
			resQueue.add(response);
			outputSelector.addWriteTask(this);
			updateLastUsed();
		}
	}

	/**
	 *
	 * @throws IOException 
	 * @see connecter.processor.IProcessor#tryWrite()
	 */
	@Override
	public void tryWrite() throws IOException {
		ResponseWriter peekWriter = null;
		boolean done = false;

		while ((peekWriter = resQueue.peek()) != null) {
			done = peekWriter.bufferToChannel();

			if (done) {
				if (!peekWriter.isConnectionKeepALive()) {
					close();
				}
				resQueue.poll();
			} else {
				break;
			}
		}

		if (done) {
			SelectionKey key = socketChannel.keyFor(outputSelector.getSelector());
			key.cancel();
			// outputSelector.selectNow();
		}
		updateLastUsed();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (isProcessing.compareAndSet(false, true)) {
			try {
				process();
				/*
				 * 有可能执行到这个地方的时候，又有新的请求加入到队列并提交了任务，然而调用run方法的时候(isProcessing==
				 * true)没有获得执行权，直接跳过，所以需要在finally中检查队列是否为空,若不为空再提交一次任务
				 */
			} catch (Throwable e) {
				exceptionHandler(e);
			} finally {
				isProcessing.set(false);
				// 检查队列是否为空,若不为空再提交一次任务
				if (reqQueue.peek() != null) {
					pool.submit(this);
					System.out.println("再次提交任务");
				}
			}
		}
	}

	/**
	 * @param e
	 */
	private void exceptionHandler(Throwable e) {
		try {
			e.printStackTrace();
			close();
			System.out.println("通道已被移除");
		} catch (IOException e1) {
			System.out.println("关闭通道出现异常！");
//			e1.printStackTrace();
		}
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public long getLastUsed() {
		return lastUsed;
	}

	public void updateLastUsed() {
		this.lastUsed = System.currentTimeMillis();
		cleaner.schedule(cleanTask, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * @throws IOException
	 *
	 */
	public void close() throws IOException {
		socketChannel.close();
		// outputSelector.selectNow();
	}

	public void invalidIncrease() {
		invalidCount++;
		// System.out.println("invalid");
		if (invalidCount > 3 && reqQueue.isEmpty() && resQueue.isEmpty()) {
			try {
				// System.out.println("close!");
				close();
			} catch (IOException e) {
//				e.printStackTrace();
			}
		}
	}

	public void reset() {
		invalidCount = 0;
	}
}
