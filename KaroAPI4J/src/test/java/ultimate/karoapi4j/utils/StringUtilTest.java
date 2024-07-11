package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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

	@Test
	public void test_parseRanges()
	{
		assertArrayEquals(new int[] { 1 }, StringUtil.parseRanges("1"));
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, StringUtil.parseRanges("1-5"));
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5, 7 }, StringUtil.parseRanges("1-5,7"));
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5, 7, 9, 13, 14, 15, 16, 17, 18, 20 }, StringUtil.parseRanges("1-5,7,9,13-18,20"));
		
		try
		{
			StringUtil.parseRanges("a");
			fail("exception not occurred");
		}
		catch(IllegalArgumentException e)
		{
			
		}
		try
		{
			StringUtil.parseRanges("1-a");
			fail("exception not occurred");
		}
		catch(IllegalArgumentException e)
		{
			
		}
		try
		{
			StringUtil.parseRanges("1,,3");
			fail("exception not occurred");
		}
		catch(IllegalArgumentException e)
		{
			
		}
	}
}
