/**
 * 
 */
package connecter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import connecter.selector.AcceptableSelector;
import connecter.selector.OutputSelector;
import container.Container;
import launcher.Configuration;

/**
 * @author CoolStranger
 * @date 2017年12月26日
 * @time 下午11:30:23
 *
 */
public class HttpConnecterNIO {
	private Selector acceptSelector;
	private Selector writeSelector;
	private static HttpConnecterNIO instance = new HttpConnecterNIO();
	
	private HttpConnecterNIO(){
	}
	
	public static HttpConnecterNIO getInstance(){
		return instance;
	}

	public void launch() throws IOException {
		long startLoading = System.currentTimeMillis();
		
		ServerSocketChannel serverChannel1 = ServerSocketChannel.open();
		serverChannel1.configureBlocking(false);
		serverChannel1.bind(new InetSocketAddress("127.0.0.1", Configuration.serverPort));
		
		ServerSocketChannel serverChannel2 = ServerSocketChannel.open();
		serverChannel2.configureBlocking(false);
		serverChannel2.bind(new InetSocketAddress(InetAddress.getLocalHost(),Configuration.serverPort));
		
		ServerSocketChannel serverChannel3 = ServerSocketChannel.open();
		serverChannel3.configureBlocking(false);
		serverChannel3.bind(new InetSocketAddress("127.0.0.1", 9081));
		
		ServerSocketChannel serverChannel4 = ServerSocketChannel.open();
		serverChannel4.configureBlocking(false);
		serverChannel4.bind(new InetSocketAddress(InetAddress.getLocalHost(),9081));
		
		acceptSelector = Selector.open();
		writeSelector = Selector.open();
		Container.init();

		serverChannel1.register(acceptSelector, SelectionKey.OP_ACCEPT);
		serverChannel2.register(acceptSelector, SelectionKey.OP_ACCEPT);
		serverChannel3.register(acceptSelector, SelectionKey.OP_ACCEPT);
		serverChannel4.register(acceptSelector, SelectionKey.OP_ACCEPT);
		
		OutputSelector outputSelector = new OutputSelector(writeSelector);
		// outputSelector.run();
		new Thread(outputSelector).start();
		new Thread(new AcceptableSelector(acceptSelector, outputSelector)).start();
		
		long startupTime = System.currentTimeMillis() - startLoading;
		success(startupTime);
	}

	private void success(long startupTime){
		System.out.println();
		System.out.println("----------欢迎使用" + Configuration.SERVER_NAME + "---------");
		System.out.println("版本: " + Configuration.version);
		try {
			System.out.println("服务器IP:  " + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		System.out.println("本次启动耗时 " + startupTime + "ms  作者：Coolstranger");
	}
	
}
