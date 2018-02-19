/**
 * 
 */
package connecter.selector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import connecter.processor.IProcessor;
import factory.ProcessorFactory;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 下午4:43:38
 *
 */
public class InputSelector implements Runnable {

	private Selector selector;
	private OutputSelector outputSelector;

	public InputSelector(Selector selector, OutputSelector outputSelector) {
		this.selector = selector;
		this.outputSelector = outputSelector;
	}

	private void work() {
		while (true) {
			try {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();

					if (key.isAcceptable()) {
						ServerSocketChannel s = (ServerSocketChannel) key.channel();
						SocketChannel socketChannel = s.accept();
						socketChannel.configureBlocking(false);
						SelectionKey newKey = socketChannel.register(selector, SelectionKey.OP_READ);
						IProcessor processor = ProcessorFactory.createProcessor(socketChannel, outputSelector);
						newKey.attach(processor);
					} else if (key.isReadable()) {
						IProcessor processor = (IProcessor) key.attachment();
						processor.tryRead();
					}
				}
			} catch (IOException e) {
			} catch (Throwable e) {
//				e.printStackTrace();
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

}
