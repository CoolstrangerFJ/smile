/**
 * 
 */
package connector.response;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * @author CoolStranger
 * @date 2017年12月27日
 * @time 上午11:47:32
 *
 */
public interface ResponseWriter {

	void ready4Write();
	
	void write(String s) throws UnsupportedEncodingException;
	
	boolean bufferToChannel();

	void put(ByteBuffer buffer);
	
	boolean isConnectionKeepALive();
}
