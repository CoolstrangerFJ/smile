package launcher;

import connecter.HttpConnecterNIO;

public class Launcher {

	public static void main(String[] args) {
		HttpConnecterNIO server = new HttpConnecterNIO();
		try {
			server.launch();
		} catch (Throwable e) {
			System.out.println("启动失败");
			e.printStackTrace();
		}
	}
}
