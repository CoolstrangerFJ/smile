/**
 * 
 */
package test;

import java.util.UUID;

import javax.servlet.http.Cookie;

/**
 * @author CoolStranger
 * @date 2018年1月24日
 * @time 下午6:12:52
 *
 */
public class CookieTest {

	public static void main(String[] args) {
//		Cookie cookie = new Cookie("name", "haha");
//		cookie.setPath("/asd");
//		cookie.setMaxAge(60);
//		String path = cookie.getPath();
//		int age = cookie.getMaxAge();
//		System.out.println("path: " + path);
//		System.out.println("age: " + age);
		
		
//		String s = "dadafs";
//		String[] split = s.split("bb");
//		for (String string : split) {
//			System.out.println(string);
//		}
//		System.out.println(split.length);
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replaceAll("-", "");
		System.out.println(uuid);
	}
}
