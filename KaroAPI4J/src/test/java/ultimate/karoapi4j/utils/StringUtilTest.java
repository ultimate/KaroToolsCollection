package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class StringUtilTest
{
	@Test
	public void test_trimTags()
	{
		assertEquals("test", StringUtil.trimTags("<b>test</b>"));
		assertEquals("test", StringUtil.trimTags("<b>test<b>"));
		assertEquals("test", StringUtil.trimTags("</b>test</b>"));
		assertEquals("test", StringUtil.trimTags("<tr>     \n      <b>    test      </b>        \n    </tr>"));
	}
	
	@Test
	public void test_trimToNumber()
	{
		assertEquals("12345", StringUtil.trimToNumber("12345"));
		assertEquals("12345", StringUtil.trimToNumber("Spiele: 12345"));
		assertEquals("12345", StringUtil.trimToNumber("12345 Spiele"));
		assertEquals("12345", StringUtil.trimToNumber("Hat 12345 Spiele"));
		assertEquals("0", StringUtil.trimToNumber("keine Zahl dabei"));
	}
	
	@Test
	public void test_processHTMLSection()
	{
		Function<String, String> parser = Function.identity();
		
		assertEquals(Arrays.asList("1", "2", "3"), StringUtil.processHTMLSection("<tr><td>1</td><td>2</td><td>3</td></tr>", "td", parser));
	}
}
