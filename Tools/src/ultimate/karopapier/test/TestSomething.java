package test;

import java.util.HashMap;
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
	}
}
