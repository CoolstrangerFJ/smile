package test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Bioserver {
	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(4396);
		Socket accept = ss.accept();
		InputStream in = accept.getInputStream();
		int c = -1;
		while ((c = in.read()) != -1) {
			System.out.print((char) c);
		}
		System.out.println();
		System.out.println("done!");
		accept.shutdownInput();
		accept.close();
		ss.close();
	}
}
