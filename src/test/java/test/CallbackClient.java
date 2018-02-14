package test;

import java.util.Random;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class CallbackClient {
	public static void main(String[] args) {
		try {
			SocketChannel socketChannel = SocketChannel.open();
			socketChannel.connect(new InetSocketAddress("127.0.0.1", 8000));

			ByteBuffer writeBuffer = ByteBuffer.allocate(32);
			ByteBuffer readBuffer = ByteBuffer.allocate(32);

			getMessage(readBuffer, socketChannel);
			sendRandomInt(writeBuffer, socketChannel, 1000);
			getMessage(readBuffer, socketChannel);

//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			sendRandomInt(writeBuffer, socketChannel, 10);
			getMessage(readBuffer, socketChannel);

			socketChannel.close();
		} catch (IOException e) {
		}
	}

	public static void sendRandomInt(ByteBuffer writeBuffer, SocketChannel socketChannel, int bound) {
		Random r = new Random();
		int d = 0;

		d = r.nextInt(bound);
		if (d == 0)
			d = 1;
		System.out.println(d);
		writeBuffer.clear();
		writeBuffer.put(String.valueOf(d).getBytes());
		writeBuffer.flip();
		try {
			socketChannel.write(writeBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getMessage(ByteBuffer readBuffer, SocketChannel socketChannel) {
		readBuffer.clear();
		byte[] buf = new byte[16];
		try {
			socketChannel.read(readBuffer);
		} catch (IOException e) {
		}
		readBuffer.flip();
		int len = readBuffer.remaining();
		readBuffer.get(buf, 0, len);
		System.out.println(new String(buf, 0, len));
	}
}
