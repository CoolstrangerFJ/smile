/**
 * 
 */
package manage;

/**
 * 此接口用于manager项目获取服务器运行参数
 * 
 * @author CoolStranger
 *
 */
public interface Moniter {
	
	/**
	 * 获取运行时长
	 * @return 运行时长
	 */
	long getRuntime();

	/**
	 * 获取当前连接数
	 * @return 当前连接数
	 */
	int getConcurrency();

	/**
	 * 获取当前QPS
	 * @return QPS
	 */
	int getQPS();
	
	/**
	 * 获取当前总处理请求数
	 * @return 当前总处理请求数
	 */
	long getTotalCount();
}
