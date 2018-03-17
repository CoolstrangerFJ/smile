package test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Find implements Runnable {

	private int ip;
	
	public Find(int ip) {
		super();
		this.ip = ip;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Socket socket = new Socket("192.168.92."+ip, 8080);
			System.out.println("192.168.92."+ip+"连接成功");
			OutputStream out = socket.getOutputStream();
			out.write("hello".getBytes());
			out.flush();
			synchronized (out) {
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		for(int i = 2 ;i<255;i++){
			new Thread(new Find(i)).start();
			System.out.println("线程"+i+"已启动");
		}
	}

}
