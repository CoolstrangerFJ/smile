/**
 * 
 */
package test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * @author CoolStranger
 * @date 2018年2月3日
 * @time 下午7:22:00
 *
 */
public class TestUrlEncoder {

	public static void main(String[] args) throws Exception {
//		String url = "http://localhost:80/root/index.html?蛤蛤=92&xixi=68;jsessionid=1531da5s54fasd35adas";
//		String urlAfterEncode = URLEncoder.encode(url,"utf-8");
//		System.out.println(urlAfterEncode);
//		urlAfterEncode = URLEncoder.encode(url);
//		System.out.println(urlAfterEncode);
//		urlAfterEncode = URLEncoder.encode(urlAfterEncode);
//		System.out.println(urlAfterEncode);
//		System.out.println(Locale.getDefault());
//		Locale locale = null;
//		locale = new Locale("zh_CN");
//		System.out.println(locale);
//		locale = new Locale("zh");
//		System.out.println(locale);
//		locale = new Locale("zh_TW");
//		System.out.println(locale);
//		locale = new Locale("zh_HK");
//		System.out.println(locale);
//		locale = new Locale("en_US");
//		System.out.println(locale);
//		locale = new Locale("en");
//		System.out.println(locale);
//		Locale languageTag = Locale.forLanguageTag("en_US");
//		System.out.println("languageTag: "+languageTag);
		Locale[] availableLocales = Locale.getAvailableLocales();
		System.out.println("----availableLocales----");
		for (Locale locale2 : availableLocales) {
			if (locale2.toString().startsWith("zh")) {
				System.out.println(locale2);
			}
		}
		
		
	}
}
