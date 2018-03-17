/**
 * 
 */
package connecter.selector;

import java.io.IOException;
import java.nio.channels.Selector;

public class LoadBalancer {

	private InputSelector[] inputSelectors;
	private int total;
	private int assignNum;
	private int index;

	public LoadBalancer(int total) throws IOException {
		this.total = total;
		init();
	}

	private void init() throws IOException {
		assignNum = total - 1;
		inputSelectors = new InputSelector[total];
		for (int i = 0; i < total; i++) {
			Selector selector = Selector.open();
			InputSelector inputSelector = new InputSelector(selector);
			inputSelectors[i] = inputSelector;
			new Thread(inputSelector).start();
		}
	}

	public InputSelector getInputSelector() {
		index &= assignNum;
		return inputSelectors[index++];
	}
}
