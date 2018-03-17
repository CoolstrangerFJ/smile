/**
 * 
 */
package connecter.selector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import connecter.processor.IProcessor;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 下午4:43:38
 *
 */
public class InputSelector extends AbstractSelector {

	public InputSelector(Selector selector) {
		super(selector, SelectionKey.OP_READ);
	}

	@Override
	protected void handle(SelectionKey key) throws IOException {
		if (key.isReadable()) {
			IProcessor processor = (IProcessor) key.attachment();
			processor.tryRead();
		}
	}

}
