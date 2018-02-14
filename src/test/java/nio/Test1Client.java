package nio;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class Test1Client {
	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("127.0.0.1", 80);
		OutputStream out = socket.getOutputStream();
		String req = "GET / HTTP/1.1\r\n" + "Host: localhost\r\n" + "Connection: keep-alive\r\n"
				+ "Cache-Control: max-age=0\r\n" + "Upgrade-Insecure-Requests: 1\r\n"
				+ "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 SE 2.X MetaSr 1.0\r\n"
				+ "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n"
				+ "Accept-Language: zh-CN,zh;q=0.8\r\n" + "Accept-Encoding: gzip, deflate\r\n" + "\r\n";
		byte[] buf = req.getBytes("utf-8");
		System.out.println("length: " + buf.length);
		Random r = new Random();
		int offset = 0, length = 0;
		for (int i = 0; i < 3; i++) {
			offset = 0;
			length = 0;
			while (offset < buf.length) {
				length = r.nextInt(10) + 1;
				if (offset + length > buf.length) {
					length = buf.length - offset;
				}
				out.write(buf, offset, length);
				out.flush();
				offset += length;
				Thread.sleep(50);
				// System.out.println("offset: " + offset);
			}
			System.out.println(i);
		}
		out.flush();
		InputStream inputStream = socket.getInputStream();
		int c = -1;
		while ((c = inputStream.read()) != -1) {
			System.out.print((char) c);
		}
		socket.close();
	}
}
