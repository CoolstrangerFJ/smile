/**
 * 
 */
package connecter.parser;

import connecter.request.Request;

/**
 * @author CoolStranger
 * @date 2017年12月25日
 * @time 下午12:23:41
 *
 */
public interface IRequestParser {

	Request getRequest();

	boolean read();
}
