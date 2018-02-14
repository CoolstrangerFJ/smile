package util;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

public class ScreenCapture implements Runnable {

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private Rectangle screenRectangle = new Rectangle(screenSize);
	private Robot robot;
	private AtomicReference<BufferedImage> image = new AtomicReference<>();
	private static ScreenCapture screenCapture = new ScreenCapture();

	private ScreenCapture() {
		super();
		init();
	}

	public static ScreenCapture getScreen() {
		return screenCapture;
	}

	public void init() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	public BufferedImage getImage() {
		return image.get();
	}

	@Override
	public void run() {
		while (true) {
			image.set(robot.createScreenCapture(screenRectangle));
			// System.out.println(++count);
			// if (count >= 10) {
			// count = 0;
			// System.out.println(System.currentTimeMillis() / 1000);
			// }
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		ScreenCapture screenCapture = new ScreenCapture();
		new Thread(screenCapture).start();
	}
}
