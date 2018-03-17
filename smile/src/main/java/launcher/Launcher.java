package launcher;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import connecter.HttpConnecterNIO;

public class Launcher {

	public static void main(String[] args) {
		HttpConnecterNIO server = HttpConnecterNIO.getInstance();
		try {
			server.addSocketAddress(new InetSocketAddress("127.0.0.1", Configuration.serverPort));
			server.addSocketAddress(new InetSocketAddress(InetAddress.getLocalHost(),Configuration.serverPort));
			server.addSocketAddress(new InetSocketAddress("127.0.0.1", 9081));
			server.addSocketAddress(new InetSocketAddress(InetAddress.getLocalHost(),9081));
			server.launch();
		} catch (Throwable e) {
			System.out.println("启动失败");
			e.printStackTrace();
		}
	}
}
