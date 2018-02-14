/**
 * 
 */
package connecter.response;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import connecter.buffer.IBuffer;

/**
 * @author CoolStranger
 * @date 2017年12月28日
 * @time 下午4:52:34
 *
 */
public class ServletOutputStreamImpl extends ServletOutputStream {

	private IBuffer buffer;

	public ServletOutputStreamImpl(IBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 * @param b
	 * @throws IOException
	 */
	@Override
	public void write(int b) throws IOException {
		buffer.put(b);
	}

	@Override
	public void write(byte[] src) throws IOException {
		buffer.put(src);
	}

	@Override
	public void write(byte[] src, int offset, int length) throws IOException {
		buffer.put(src, offset, length);
	}

}
