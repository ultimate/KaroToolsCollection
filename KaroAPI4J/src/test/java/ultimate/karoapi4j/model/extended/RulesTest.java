package ultimate.karoapi4j.model.extended;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Options;

public class RulesTest
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger logger = LogManager.getLogger(getClass());

	@ParameterizedTest
	@ValueSource(doubles = { 0, 0.25, 0.5, 0.75, 1.0 })
	public void test_createOptionsWithFree(double preferStandards) throws InterruptedException, ExecutionException
	{
		logger.info("--------------------------------------------------");
		logger.info("testing with preferStandards = " + preferStandards);

		double tolerance = 0.01;
		int samples = 100000;
		int maxTC = 9;

		Rules rules = new Rules(0, maxTC, EnumGameTC.random, null, EnumGameDirection.random);

		Random random = new Random();

		// zzz
		HashMap<Integer, Integer> zzzValues = new HashMap<>();
		for(int zzz = 0; zzz <= maxTC; zzz++)
			zzzValues.put(zzz, 0);

		// cps
		HashMap<Boolean, Integer> cpsValues = new HashMap<>();
		cpsValues.put(true, 0);
		cpsValues.put(false, 0);

		// crashallowed
		HashMap<EnumGameTC, Integer> crashallowedValues = new HashMap<>();
		crashallowedValues.put(EnumGameTC.forbidden, 0);
		crashallowedValues.put(EnumGameTC.allowed, 0);
		crashallowedValues.put(EnumGameTC.free, 0);

		// startdirection
		HashMap<EnumGameDirection, Integer> startdirectionValues = new HashMap<>();
		startdirectionValues.put(EnumGameDirection.classic, 0);
		startdirectionValues.put(EnumGameDirection.formula1, 0);
		startdirectionValues.put(EnumGameDirection.free, 0);

		Options options;

		for(int i = 0; i < samples; i++)
		{
			options = rules.createOptions(random, preferStandards, true);

			// zzz
			zzzValues.put(options.getZzz(), zzzValues.get(options.getZzz()) + 1);

			// cps
			cpsValues.put(options.isCps(), cpsValues.get(options.isCps()) + 1);

			// crashallowed
			crashallowedValues.put(options.getCrashallowed(), crashallowedValues.get(options.getCrashallowed()) + 1);

			// startdirection
			if(!options.isCps())
				assertEquals(EnumGameDirection.classic, options.getStartdirection());
			startdirectionValues.put(options.getStartdirection(), startdirectionValues.get(options.getStartdirection()) + 1);
		}

		logger.debug("zzzValues            = " + zzzValues);
		logger.debug("allowed values = " + (1 / (maxTC + 1.0) * (1 - preferStandards) - tolerance) * samples + " - " + (1 / (maxTC + 1.0) * (1 - preferStandards) + tolerance) * samples);
		logger.debug("cpsValues            = " + cpsValues);
		logger.debug("allowed values = " + (0.5 * (1 - preferStandards) - tolerance) * samples + " - " + (0.5 * (1 - preferStandards) + tolerance) * samples);
		logger.debug("crashallowedValues   = " + crashallowedValues);
		logger.debug("allowed values = " + (0.33 * (1 - preferStandards) - tolerance) * samples + " - " + (0.33 * (1 - preferStandards) + tolerance) * samples);
		logger.debug("startdirectionValues = " + startdirectionValues);
		logger.debug("allowed values = " + (0.33 * (1 - preferStandards) - tolerance) * samples + " - " + (0.33 * (1 - preferStandards) + tolerance) * samples);

		// zzz
		for(int zzz = 0; zzz <= maxTC; zzz++)
		{
			if(zzz == 2)
				assertEquals(preferStandards + 1 / (maxTC + 1.0) * (1 - preferStandards), zzzValues.get(zzz) / ((double) samples), tolerance);
			else
				assertEquals(1 / (maxTC + 1.0) * (1 - preferStandards), zzzValues.get(zzz) / ((double) samples), tolerance);
		}

		// cps
		assertEquals(preferStandards + prop(2, preferStandards), cpsValues.get(true) / ((double) samples), tolerance);
		assertEquals(prop(2, preferStandards), cpsValues.get(false) / ((double) samples), tolerance);

		// crashallowedValues
		assertEquals(preferStandards + prop(3, preferStandards), crashallowedValues.get(EnumGameTC.forbidden) / ((double) samples), tolerance);
		assertEquals(prop(3, preferStandards), crashallowedValues.get(EnumGameTC.allowed) / ((double) samples), tolerance);
		assertEquals(prop(3, preferStandards), crashallowedValues.get(EnumGameTC.free) / ((double) samples), tolerance);

		// startdirectionValues
		// with no cps, always "classic" is returned --> the distribution is shifted
		double propForNoCps = prop(2, preferStandards); // see above
		assertEquals(propForNoCps + (1 - propForNoCps) * (preferStandards + prop(3, preferStandards)), startdirectionValues.get(EnumGameDirection.classic) / ((double) samples), tolerance);
		assertEquals((1 - propForNoCps) * prop(3, preferStandards), startdirectionValues.get(EnumGameDirection.formula1) / ((double) samples), tolerance);
		assertEquals((1 - propForNoCps) * prop(3, preferStandards), startdirectionValues.get(EnumGameDirection.free) / ((double) samples), tolerance);
	}
	
	@ParameterizedTest
	@ValueSource(doubles = { 0, 0.25, 0.5, 0.75, 1.0 })
	public void test_createOptionsWithoutFree(double preferStandards) throws InterruptedException, ExecutionException
	{
		logger.info("--------------------------------------------------");
		logger.info("testing with preferStandards = " + preferStandards);

		double tolerance = 0.01;
		int samples = 100000;
		int maxTC = 9;

		Rules rules = new Rules(0, maxTC, EnumGameTC.random, null, EnumGameDirection.random);

		Random random = new Random();

		// zzz
		HashMap<Integer, Integer> zzzValues = new HashMap<>();
		for(int zzz = 0; zzz <= maxTC; zzz++)
			zzzValues.put(zzz, 0);

		// cps
		HashMap<Boolean, Integer> cpsValues = new HashMap<>();
		cpsValues.put(true, 0);
		cpsValues.put(false, 0);

		// crashallowed
		HashMap<EnumGameTC, Integer> crashallowedValues = new HashMap<>();
		crashallowedValues.put(EnumGameTC.forbidden, 0);
		crashallowedValues.put(EnumGameTC.allowed, 0);
		crashallowedValues.put(EnumGameTC.free, 0);

		// startdirection
		HashMap<EnumGameDirection, Integer> startdirectionValues = new HashMap<>();
		startdirectionValues.put(EnumGameDirection.classic, 0);
		startdirectionValues.put(EnumGameDirection.formula1, 0);
		startdirectionValues.put(EnumGameDirection.free, 0);

		Options options;

		for(int i = 0; i < samples; i++)
		{
			options = rules.createOptions(random, preferStandards, false);

			// zzz
			zzzValues.put(options.getZzz(), zzzValues.get(options.getZzz()) + 1);

			// cps
			cpsValues.put(options.isCps(), cpsValues.get(options.isCps()) + 1);

			// crashallowed
			crashallowedValues.put(options.getCrashallowed(), crashallowedValues.get(options.getCrashallowed()) + 1);

			// startdirection
			if(!options.isCps())
				assertEquals(EnumGameDirection.classic, options.getStartdirection());
			startdirectionValues.put(options.getStartdirection(), startdirectionValues.get(options.getStartdirection()) + 1);
		}

		logger.debug("zzzValues            = " + zzzValues);
		logger.debug("allowed values = " + (1 / (maxTC + 1.0) * (1 - preferStandards) - tolerance) * samples + " - " + (1 / (maxTC + 1.0) * (1 - preferStandards) + tolerance) * samples);
		logger.debug("cpsValues            = " + cpsValues);
		logger.debug("allowed values = " + (0.5 * (1 - preferStandards) - tolerance) * samples + " - " + (0.5 * (1 - preferStandards) + tolerance) * samples);
		logger.debug("crashallowedValues   = " + crashallowedValues);
		logger.debug("allowed values = " + (0.33 * (1 - preferStandards) - tolerance) * samples + " - " + (0.33 * (1 - preferStandards) + tolerance) * samples);
		logger.debug("startdirectionValues = " + startdirectionValues);
		logger.debug("allowed values = " + (0.33 * (1 - preferStandards) - tolerance) * samples + " - " + (0.33 * (1 - preferStandards) + tolerance) * samples);

		// zzz
		for(int zzz = 0; zzz <= maxTC; zzz++)
		{
			if(zzz == 2)
				assertEquals(preferStandards + 1 / (maxTC + 1.0) * (1 - preferStandards), zzzValues.get(zzz) / ((double) samples), tolerance);
			else
				assertEquals(1 / (maxTC + 1.0) * (1 - preferStandards), zzzValues.get(zzz) / ((double) samples), tolerance);
		}

		// cps
		assertEquals(preferStandards + prop(2, preferStandards), cpsValues.get(true) / ((double) samples), tolerance);
		assertEquals(prop(2, preferStandards), cpsValues.get(false) / ((double) samples), tolerance);

		// crashallowedValues
		assertEquals(preferStandards + prop(2, preferStandards), crashallowedValues.get(EnumGameTC.forbidden) / ((double) samples), tolerance);
		assertEquals(prop(2, preferStandards), crashallowedValues.get(EnumGameTC.allowed) / ((double) samples), tolerance);
		assertEquals(0, crashallowedValues.get(EnumGameTC.free) / ((double) samples), tolerance);

		// startdirectionValues
		// with no cps, always "classic" is returned --> the distribution is shifted
		double propForNoCps = prop(2, preferStandards); // see above
		assertEquals(propForNoCps + (1 - propForNoCps) * (preferStandards + prop(2, preferStandards)), startdirectionValues.get(EnumGameDirection.classic) / ((double) samples), tolerance);
		assertEquals((1 - propForNoCps) * prop(2, preferStandards), startdirectionValues.get(EnumGameDirection.formula1) / ((double) samples), tolerance);
		assertEquals(0, startdirectionValues.get(EnumGameDirection.free) / ((double) samples), tolerance);
	}
	
	private static double prop(int options, double preferStandards)
	{
		return 1.0 / (double) options * (1 - preferStandards);
	}
}
