/**
 * 
 */
package connecter.selector;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.atomic.AtomicBoolean;

import connecter.processor.IProcessor;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 下午4:43:38
 *
 */
public class OutputSelector implements Runnable {

	private ConcurrentLinkedQueue<IProcessor> processorQueue = new ConcurrentLinkedQueue<>();
	private Selector writeSelector;

	public OutputSelector(Selector writeSelector) {
		this.writeSelector = writeSelector;
	}

	private void work() {
		int count = 0;
		IProcessor wait4write = null;
		while (true) {
			try {
				count = writeSelector.selectNow();
				// 注册队列中的通道
				while ((wait4write = processorQueue.poll()) != null) {
					try {
						wait4write.getSocketChannel().register(writeSelector, SelectionKey.OP_WRITE, wait4write);
						count++;
					} catch (ClosedChannelException | CancelledKeyException e) {
					}
				}
				count += writeSelector.selectNow();

				if (count == 0) {
					// 若无事件,休息一下能极大地降低CPU性能浪费
					Thread.sleep(1);
					continue;
				}

				Set<SelectionKey> selectedKeys = writeSelector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();

					if (key.isWritable()) {
						IProcessor processor = (IProcessor) key.attachment();
						processor.tryWrite();
					}
				}
			} catch (IOException e) {
			} catch (Throwable e) {
				// e.printStackTrace();
			}
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		work();
	}

	public void addWriteTask(IProcessor processor) {
		processorQueue.add(processor);
	}

	public Selector getSelector() {
		return writeSelector;
	}

	// public void selectNow() {
	// try {
	// if (writeSelector.selectNow() != 0) {
	// hasKey.set(true);
	// }
	// } catch (IOException e) {
	// throw new RuntimeException(e);
	// }
	// }
}
