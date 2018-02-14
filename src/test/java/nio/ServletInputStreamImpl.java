package nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import javax.servlet.ServletInputStream;

public class ServletInputStreamImpl extends ServletInputStream {

	private ByteChannel channel;
	private ByteBuffer buffer;
	private boolean isEnd;
	private int lastChar;
	private int cacheSize;

	public ServletInputStreamImpl(ByteChannel channel) {
		this(channel, 8192);
	}

	public ServletInputStreamImpl(ByteChannel channel, int cacheSize) {
		this.channel = channel;
		this.cacheSize = cacheSize;
		init();
	}

	private void init() {
		buffer = ByteBuffer.allocate(cacheSize);
		try {
			readFromChannel();
			buffer.flip();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public int read() throws IOException {
		int value = -1;
		if (isEnd) {
			return -1;
		}
		if (buffer.hasRemaining()) {
			value = buffer.get();
		} else {
			buffer.clear();
			int n = readFromChannel();
			if (n != -1) {
				buffer.flip();
				value = buffer.get();
			} else {
				isEnd = true;
				// System.out.println("已读到尽头！");
			}
		}
		return value;
	}

	public String readLine() throws IOException {

		int c = -1;

		StringBuilder sb = new StringBuilder();
		while ((c = read()) != -1) {
			if (c == '\r' || c == '\n') {
				if (c == '\n' && lastChar == '\r') {
					lastChar = '\n';
					return readLine();
				}
				lastChar = c;
				break;
			}
			sb.append((char) c);
		}
		if (c == -1) {
			return null;
		}
		return sb.toString();
	}

	private int readFromChannel() throws IOException {
		int n = 0;
		while ((n = channel.read(buffer)) == 0) {
			System.out.println("读!");
		}

		// System.out.println("成功读取一次！");
		return n;
	}
}
