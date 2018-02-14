package connecter.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
//import java.util.List;

import launcher.Configuration;

/**
 * 此类是一个自适应大小的缓存类,底层为字节数组,一般用于NIO服务器缓存不大于4MB的请求和响应,可以通过构造方法指定缓存等级设置缓存大小，默认值为4KB
 */
public class IOBuffer implements IBuffer {

	private static final int TINYSIZE = Configuration.tinySize;
	private static final int SMALLSIZE = Configuration.smallSize;
	private static final int MEDIUMSIZE = Configuration.mediumSize;
	private static final int BIGSIZE = Configuration.bigSize;
	private static final int LARGESIZE = Configuration.largeSize;
	private static final int HUGESIZE = Configuration.hugeSize;
	private static final int[] SIZE = new int[] { TINYSIZE, SMALLSIZE, MEDIUMSIZE, BIGSIZE, LARGESIZE, HUGESIZE };
	private int sizeLevel;
	private static final int READ_NOTHING = -1;
	private static final int READ_SOMETHING = 0;
	private static final int HAS_REMAINING = 1;
	private ByteBuffer buf;
	private byte[] arrBuf;
	private int writePos;
	private int readPos;
	private SocketChannel socketChannel;

	public IOBuffer(SocketChannel socketChannel) {
		this(socketChannel, 1);
	}

	public IOBuffer(SocketChannel socketChannel, int sizeLevel) {
		this.socketChannel = socketChannel;
		this.sizeLevel = sizeLevel;
		init();
	}

	public IOBuffer(SocketChannel socketChannel, ByteBuffer byteBuffer) {
		this.socketChannel = socketChannel;
		this.buf = byteBuffer;
		this.writePos = byteBuffer.capacity();
	}

	private void init() {
		int capacity = SIZE[sizeLevel];
		arrBuf = new byte[capacity];
		buf = ByteBuffer.wrap(arrBuf);
	}

	/**
	 * 从缓存块中读取1字节
	 * 
	 * @return 读到尽头则返回-1
	 */
	@Override
	public int get() {
		if (readPos < writePos) {
			return arrBuf[readPos++];
		}
		return -1;
	}

	/**
	 * 从缓存块中读取数据存入给定数组
	 * 
	 * @param dest
	 * @return 读到尽头则返回-1
	 */
	@Override
	public int get(byte[] dest) {
		return get(dest, 0, dest.length);
	}

	/**
	 * 从缓存块中读取数据存入给定数组中的指定位置
	 * 
	 * @param dest
	 * @param offset
	 * @param length
	 * @return 读到尽头则返回-1
	 */
	@Override
	public int get(byte[] dest, int offset, int length) {
		checkBounds(offset, length, dest.length);
		int remain = writePos - readPos;
		if (remain == 0) {
			return -1;
		}
		if (remain < length) {
			length = remain;
		}
		System.arraycopy(arrBuf, readPos, dest, offset, length);
		readPos += length;
		return length;
	}

	/**
	 * 把一个字节存入缓存块,自动扩容
	 *
	 * @param c
	 */
	@Override
	public void put(int c) {
		if (!buf.hasRemaining()) {
			expandOneLevel();
		}
		buf.put((byte) c);
		writePos = buf.position();
	}

	/**
	 * 把字节数组存入缓存块,自动扩容
	 * 
	 * @see connecter.buffer.IOBuffer#put(byte[])
	 * @param src
	 */
	@Override
	public void put(byte[] src) {
		put(src, 0, src.length);
	}

	/**
	 * 把字节数组中指定数据存入缓存块,自动扩容
	 * 
	 * @see connecter.buffer.IOBuffer#put(byte[], int, int)
	 * @param src
	 * @param offset
	 * @param length
	 */
	@Override
	public void put(byte[] src, int offset, int length) {
		checkBounds(offset, length, src.length);
		if (length > buf.remaining()) {
			expand(length + writePos);
		}
		buf.put(src, offset, length);
		writePos = buf.position();
	}

	/**
	 * 缓存区剩余数据大小
	 */
	public int remaining() {
		return writePos - readPos;
	}

	public int size() {
		if (arrBuf != null) {
			return arrBuf.length;
		} else if (buf != null) {
			return buf.capacity();
		} else {
			return 0;
		}
	}

	/**
	 * 把数据往前移动
	 */
	private void compact() {
		buf.position(readPos);
		buf.compact();
		writePos = buf.position();
		readPos = 0;
	}

	/**
	 * 创建新的缓存块, 若有粘包, 把粘包的内容转移到新的缓存块, 并返回新的缓存块
	 *
	 * @param contentLength
	 * @return 新缓存块
	 */
	public IOBuffer getAdhering(int contentLength) {
		int adhering = remaining() - contentLength;
		// 若无粘包,直接创建新的缓存块
		if (adhering == 0) {
			return new IOBuffer(socketChannel);
		}
		int sizeLevel = matchSize(adhering);
		// 粘包部分一般会很小,避免开辟只有1KB的缓存导致频繁扩容
		if (sizeLevel == 0) {
			sizeLevel = 1;
		}
		int size = SIZE[sizeLevel];
		byte[] buf = new byte[size];
		System.arraycopy(arrBuf, readPos + contentLength, buf, 0, adhering);
		ByteBuffer wrap = ByteBuffer.wrap(buf);
		wrap.position(adhering);
		IOBuffer adherentBuf = new IOBuffer(this.socketChannel, wrap);
		adherentBuf.setArrBuf(buf);
		adherentBuf.setSizeLevel(sizeLevel);
		adherentBuf.writePos = adhering;
		this.writePos -= adhering;
		return adherentBuf;
	}

	private void setArrBuf(byte[] buf) {
		this.arrBuf = buf;
	}

	private void setSizeLevel(int sizeLevel) {
		this.sizeLevel = sizeLevel;
	}

	/**
	 * 从通道中读取
	 * 
	 * @see connecter.buffer.IOBuffer#readFromChannel()
	 * @return 是否成功读入
	 */
	@Override
	public boolean readFromChannel() {
		try {
			boolean readSomething = false;
			while (tryReadFromChannel() > READ_NOTHING) {
				readSomething = true;
			}
			return readSomething;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 尝试从通道中读取数据, 自动扩容
	 *
	 * @return 此次读入字节数
	 * @throws IOException
	 */
	private int tryReadFromChannel() throws IOException {
		int remain = buf.remaining();

		int len = 0;
		len = socketChannel.read(buf);
		writePos = buf.position();
		if (remain == len) {
			expandOneLevel();
			return HAS_REMAINING;
		}
		if (len > 0) {
			return READ_SOMETHING;
		} else {
			return READ_NOTHING;
		}
	}

	/**
	 *
	 * @see connecter.buffer.IOBuffer#ready4WriteToChannel()
	 */
	@Override
	public void ready4WriteToChannel() {
		if (buf.position() != 0) {
			buf.flip();
		}
	}

	/**
	 * 把数据写入通道
	 * 
	 * @see connecter.buffer.IOBuffer#writeToChannel()
	 * @return 是否全部写入
	 */
	@Override
	public boolean writeToChannel() {
		while (buf.hasRemaining()) {
			try {
				if (tryWriteToChannel()) {
					return false;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return true;
	}

	private boolean tryWriteToChannel() throws IOException {
		int len = socketChannel.write(buf);
		readPos = buf.position();
		return len == 0;
	}

	private void checkBounds(int offset, int length, int size) {
		if ((offset | length | (offset + length) | (size - (offset + length))) < 0)
			throw new IndexOutOfBoundsException();
	}

	private void expandOneLevel() {
		sizeLevel++;
		System.out.println("cacheLevel: " + sizeLevel);
		if (sizeLevel > 4) {
			throw new RuntimeException("OutOfBuffer");
		}
		expand();
	}

	private void expand(int size) {
		sizeLevel = matchSize(size);
		expand();
	}

	/**
	 * 扩容
	 * 
	 */
	private void expand() {
		int capacity = SIZE[sizeLevel];
		byte[] newArrBuf = new byte[capacity];
		int length = writePos - readPos;
		System.arraycopy(arrBuf, readPos, newArrBuf, 0, length);
		arrBuf = newArrBuf;
		buf = ByteBuffer.wrap(arrBuf);
		buf.position(length);
		readPos = 0;
	}

	/**
	 * 匹配合适的缓存大小
	 *
	 * @param size
	 * @return proper sizeLevel
	 */
	public static int matchSize(int size) {
		for (int i = 0; i < 5; i++) {
			if (size <= SIZE[i]) {
				return i;
			}
		}
		throw new RuntimeException("OutOfBuffer");
	}

	/**
	 * 为即将写入的数据大小做好准备
	 * 
	 * @see connecter.buffer.IBuffer#prepare(int)
	 * 
	 * @param size
	 */
	@Override
	public void prepare(int size) {
		if (size > SIZE[sizeLevel]) {
			expand(size);
		} else {
			// (SIZE[sizeLevel] - readPos)
			if ((SIZE[sizeLevel] - readPos) < size) {
				compact();
			}
		}
	}

	@Override
	public String toString() {
		return "IOBuffer [buf=" + buf + "]";
	}
}
