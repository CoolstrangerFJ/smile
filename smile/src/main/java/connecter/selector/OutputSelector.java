/**
 * 
 */
package connecter.selector;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import connecter.processor.IProcessor;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 下午4:43:38
 *
 */
public class OutputSelector extends AbstractSelector {

	public OutputSelector(Selector selector) {
		super(selector, SelectionKey.OP_WRITE);
	}

	@Override
	protected void handle(SelectionKey key) {
		if (key.isWritable()) {
			IProcessor processor = (IProcessor) key.attachment();
			processor.tryWrite();
		}
	}

	public Selector getSelector() {
		return selector;
	}
}
