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
import java.util.ArrayList;
import java.util.List;

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
	private static HttpConnecterNIO instance = new HttpConnecterNIO();
	private long startupTime;
	private List<InetSocketAddress> inetSocketAddresses = new ArrayList<>();
	
	private HttpConnecterNIO(){
	}
	
	public static HttpConnecterNIO getInstance(){
		return instance;
	}

	public void addSocketAddress(InetSocketAddress socketAddress){
		inetSocketAddresses.add(socketAddress);
	}
	public void launch() throws IOException {
		long startLoading = System.currentTimeMillis();
		
		Container.init();
		ServerMoniter.getInstance().listen();
		Selector acceptSelector = Selector.open();
		Selector writeSelector = Selector.open();
		
		for (InetSocketAddress inetSocketAddress : inetSocketAddresses) {
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.bind(inetSocketAddress);
			serverChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
		}
		
		OutputSelector outputSelector = new OutputSelector(writeSelector);
		// outputSelector.run();
		new Thread(outputSelector).start();
		new Thread(new AcceptableSelector(acceptSelector, outputSelector)).start();
		
		this.startupTime = System.currentTimeMillis();
		long loadingTime = startupTime - startLoading;
		success(loadingTime);
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
	
	public long getStartupTime(){
		return startupTime;
	}
}
