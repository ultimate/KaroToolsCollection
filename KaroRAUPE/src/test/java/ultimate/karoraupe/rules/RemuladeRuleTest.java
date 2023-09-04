package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class RemuladeRuleTest extends KaroRAUPETestcase
{	
	@Test
	public void test_isRemuladeGame_byTitle()
	{
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde §", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde § some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde §some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade §", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade § some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade §some title", null));
		
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde §", null));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde § some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde §some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade §", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade § some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade §some title", null));

		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§ some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§ some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§some title", null));

		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§", null));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§ some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§ some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§some title", null));
		
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§", null));
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§ some title", null));
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§some title", null));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§", null));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§ some title", null));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§some title", null));
		
		assertFalse(RemuladeRule.isRemuladeGame("some title § REmulAde §", null));
		assertFalse(RemuladeRule.isRemuladeGame("some title §REmulAde §", null));
		assertFalse(RemuladeRule.isRemuladeGame("some title § REmulAde§", null));
		assertFalse(RemuladeRule.isRemuladeGame("some title §REmulAde§", null));
	}

	@Test
	public void test_isRemuladeGame_byTag()
	{		
		assertTrue(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"§RE§"}))));
		assertTrue(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"aaaa", "§RE§"}))));
		assertTrue(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"§RE§", "aaaa"}))));

		assertFalse(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"§rE§"}))));
		assertFalse(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"aaaa", "§Re§"}))));
		assertFalse(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"§re§", "aaaa"}))));
	}
}
