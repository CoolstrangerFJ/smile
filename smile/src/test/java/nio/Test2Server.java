package nio;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Test2Server {

	public static void main(String[] args) {
		try {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.socket().bind(new InetSocketAddress("127.0.0.1", 80));
			ssc.configureBlocking(false);
			Selector selector = Selector.open();
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			
			while (true) {
				SocketChannel socketChannel = ssc.accept();
				System.out.println(System.currentTimeMillis()/1000);
				ServletInputStreamImpl sis = new ServletInputStreamImpl(socketChannel);
				// BufferedReader br = new BufferedReader(new
				// InputStreamReader(sis));
				// ByteBuffer buffer = ByteBuffer.allocate(256);
				// socketChannel.read(buffer);
				// buffer.flip();
				// String msg = new String(buffer.array());
				// System.out.println(msg);
				// int c = -1;
				// while ((c = sis.read()) != -1) {
				// System.out.print((char) c);
				// }
				String s = null;
				while ((s = sis.readLine()) != null) {
					if (s.equals("")) {
						System.out.println(s);
						break;
					}
					System.out.println(s);
				}
				// byte[] buf = new byte[1024];
				// int len = -1;
				// while ((len = sis.readLine(buf, 0, 1024)) != -1) {
				// System.out.println(new String(buf, 0, len));
				// }
				sis.close();
				System.out.println("finish!");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
