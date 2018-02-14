package nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Test1Server {

	public static void main(String[] args) {
		try {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.socket().bind(new InetSocketAddress("127.0.0.1", 80));
			// ssc.configureBlocking(false);
			while (true) {
				SocketChannel socketChannel = ssc.accept();
				System.out.println(socketChannel == null);
				System.out.println(System.currentTimeMillis() / 1000);
				ServletInputStreamImpl sis = new ServletInputStreamImpl(socketChannel);
				// BufferedReader br = new BufferedReader(new
				// InputStreamReader(sis));
				// ByteBuffer buffer = ByteBuffer.allocate(256);
				// socketChannel.read(buffer);
				// buffer.flip();
				// String msg = new String(buffer.array());
				// System.out.println(msg);
				int c = -1/*, count = 0*/;
				while ((c = sis.read()) != -1) {
					if (c == '\r') {
						System.out.print("\\r");
					} else if (c == '\n') {
						System.out.println("\\n");
//						count++;
//						if (count == 9) {
//							break;
//						}
					} else {
						System.out.print((char) c);
					}
				}
				System.out.println("read once!");
				String body = "<html><body><img src='img/haha.jpg' /></body></html>";
				String msg = "HTTP/1.1 200 OK\r\n"
						+"Content-Type: text/html; charset=utf-8\r\n"
						+"Content-Length: "+body.getBytes().length
						+"\r\n\r\n"
						+body;
				System.out.println(msg);
				byte[] msgbuf = msg.getBytes("utf-8");
				ByteBuffer wrap = ByteBuffer.wrap(msgbuf);
				int write = socketChannel.write(wrap);
				
				System.out.println("written! "+write);
				while ((c = sis.read()) != -1) {
					if (c == '\r') {
						System.out.print("\\r");
					} else if (c == '\n') {
						System.out.println("\\n");
					} else {
						System.out.print((char) c);
					}
				}
				
				// String s = null;
				// while ((s = sis.readLine()) != null) {
				// if (s.equals("")) {
				// System.out.println(s);
				// break;
				// }
				// System.out.println(s);
				// }
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
