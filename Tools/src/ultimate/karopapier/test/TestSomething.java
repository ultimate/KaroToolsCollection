package ultimate.karopapier.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ultimate.karoapi4j.utils.URLLoaderUtil;

public class TestSomething
{
	public static void main(String[] args)
	{
		String url = "http://api.qrserver.com/v1/create-qr-code/";
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("data", "http://michael-pinkowski-gmbh.de\nTel +49 (30) 4962055");
		parameters.put("format", "eps");
		
		String params = URLLoaderUtil.formatParameters(parameters);
		
		System.out.println(url + "?" + params);
		
		
		HashMap<Integer, String> map = new HashMap<Integer, String>();		
		map.put(2, "2");
		map.put(3, "3");
		map.put(5, "5");
		map.put(7, "7");
		map.put(6, "6");
		map.put(1, "1");
		map.put(4, "4");		
		System.out.println(map.values());
	}
}
