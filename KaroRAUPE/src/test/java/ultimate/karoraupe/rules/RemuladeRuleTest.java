package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class RemuladeRuleTest extends KaroRAUPETestcase
{	
	@Test
	public void test_isRemuladeGame()
	{
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde §"));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde § some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde §some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade §"));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade § some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade §some title"));
		
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde §"));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde § some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde §some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade §"));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade § some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade §some title"));

		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§"));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§ some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§"));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§ some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§some title"));

		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§"));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§ some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§"));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§ some title"));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§some title"));
		
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§"));
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§ some title"));
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§some title"));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§"));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§ some title"));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§some title"));
		
		assertFalse(RemuladeRule.isRemuladeGame("some title § REmulAde §"));
		assertFalse(RemuladeRule.isRemuladeGame("some title §REmulAde §"));
		assertFalse(RemuladeRule.isRemuladeGame("some title § REmulAde§"));
		assertFalse(RemuladeRule.isRemuladeGame("some title §REmulAde§"));
	}
}
