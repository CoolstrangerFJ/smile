package connecter.selector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import connecter.processor.IProcessor;
import factory.ProcessorFactory;

public class AcceptableSelector extends AbstractSelector {

	private OutputSelector outputSelector;
	private LoadBalancer loadBalancer;

	public AcceptableSelector(Selector selector, OutputSelector outputSelector) throws IOException {
		super(selector, SelectionKey.OP_ACCEPT);
		this.outputSelector = outputSelector;
		this.loadBalancer = new LoadBalancer(4);
	}

	@Override
	protected void handle(SelectionKey key) throws IOException {
		if (key.isAcceptable()) {
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
			SocketChannel socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);

			IProcessor processor = ProcessorFactory.createProcessor(socketChannel, outputSelector);
			InputSelector inputSelector = loadBalancer.getInputSelector();
			inputSelector.addRegisterTask(processor);
		}
	}

	@Override
	protected boolean doSeclect() throws IOException, InterruptedException {
		super.selector.select();
		return true;
	}

	
}
