package test;

import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BioClient {
	public static void main(String[] args) throws Exception {
		Socket s = null;
		OutputStream out = null;
		List<Socket> list = new ArrayList<>(10000);

		// 建立连接
		for (int i = 0; i < 5000; i++) {
			try {
				// s = new Socket("192.168.92.28", 80);
				s = new Socket("127.0.0.1", 80);

				System.out.println("连接成功" + i);

				out = s.getOutputStream();
				out.write("hello".getBytes());
				out.flush();
				// 保持连接
				list.add(s);
			} catch (Exception e) {
				System.out.println("第" + i + "个连接已失败");
			}
		}

		// 暂停
		System.out.println("finish!!!");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		out.write("world".getBytes());
		out.flush();
		s.shutdownOutput();
		out.close();
		s.close();
	}
}
