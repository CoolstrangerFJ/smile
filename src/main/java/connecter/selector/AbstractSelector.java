package connecter.selector;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import connecter.processor.IProcessor;

public abstract class AbstractSelector implements Runnable {

	protected ConcurrentLinkedQueue<IProcessor> processorQueue = new ConcurrentLinkedQueue<>();
	protected Selector selector;
	protected final int registerInterestOps;

	public AbstractSelector(Selector selector, int registerInterestOps) {
		this.selector = selector;
		this.registerInterestOps = registerInterestOps;
	}

	public void work() {
		while (true) {
			try {
				if (!doSeclect()) {
					continue;
				}

				// 获取事件集合
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				// 遍历事件集合
				while (iterator.hasNext()) {
					try {
						SelectionKey key = iterator.next();
						handle(key);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						iterator.remove();
					}
				}
			} catch (IOException e) {
				//忽略IOException
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @return 有无为有效操作
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected boolean doSeclect() throws IOException, InterruptedException {
		int selectCount = selector.selectNow();
		int registerCount = 0;
		IProcessor wait4Register = null;
		// 注册队列中的通道
		while ((wait4Register = processorQueue.poll()) != null) {
			try {
				doRegister(wait4Register);
				registerCount++;
			} catch (ClosedChannelException | CancelledKeyException e) {
			}
		}
		selectCount += selector.selectNow();

		if (selectCount > 0) {
			return true;
		} else {
			if (registerCount == 0) {
				// 休息1ms
				Thread.sleep(1);
			}
			return false;
		}
	}

	protected void doRegister(IProcessor wait4Register) throws ClosedChannelException, CancelledKeyException {
		wait4Register.getSocketChannel().register(selector, registerInterestOps, wait4Register);
	}

	public void run() {
		work();
	}

	protected abstract void handle(SelectionKey key) throws IOException;

	public void addRegisterTask(IProcessor processor) {
		processorQueue.add(processor);
	}

}
