/**
 * 
 */
package connecter.request;

import java.io.IOException;

import javax.servlet.ServletInputStream;

import connecter.buffer.IBuffer;

/**
 * @author CoolStranger
 * @date 2017年12月28日
 * @time 下午1:52:19
 *
 */
public class ServletInputStreamImpl extends ServletInputStream {

	private IBuffer buffer;

	public ServletInputStreamImpl(IBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * @see java.io.InputStream#read()
	 * @return
	 * @throws IOException
	 */
	@Override
	public int read() throws IOException {
		return buffer.get();
	}

	@Override
	public int read(byte[] buf) throws IOException {
		return buffer.get(buf);
	}

	@Override
	public int read(byte[] buf, int offset, int length) throws IOException {
		return buffer.get(buf, offset, length);
	}
}
