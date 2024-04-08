package ultimate.karoapi4j.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Kezzer
{
	private String seed;

	public Kezzer(String seed)
	{
		super();
		this.seed = seed;
	}

	public Kezzer(long seed)
	{
		this("" + seed);
	}

	public Kezzer()
	{
		this(System.currentTimeMillis());
	}

	/**
	 * generate a random int between min and max.
	 * Note: maximum range is 12 bytes.
	 *
	 * @param min - min value (inclusive)
	 * @param max - max value (inclusive)
	 *
	 * @return number
	 */
	public long rnd(long min, long max)
	{
		if(min > max)
		{
			long m = max;
			max = min;
			min = m;
		}
		long span = max - min + 1;
		this.seed = md5(this.seed);

		return Long.parseLong(seed.substring(0, 12), 16) % span + min;
	}

	/**
	 * generate a random float between 0 and 1.
	 * Note: it only uses 12 bytes precision - see rnd(min,max).
	 *
	 * @return number
	 */
	public double rnd()
	{
		// range 0 .. 1
		return rnd(0, 0xFFFFFFFFFFFFL) / (double) 0xFFFFFFFFFFFFL;
	}

	/**
	 * https://www.javatpoint.com/java-md5-hashing-example
	 * 
	 * @param input
	 * @return
	 */
	public static String md5(String input)
	{
		try
		{
			// static getInstance() method is called with hashing MD5
			MessageDigest md = MessageDigest.getInstance("MD5");
			// calculating message digest of an input that return array of byte
			byte[] messageDigest = md.digest(input.getBytes());
			// converting byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);
			// converting message digest into hex value
			String hashtext = no.toString(16);
			while(hashtext.length() < 32)
			{
				hashtext = "0" + hashtext;
			}
			return hashtext;
		}
		// for specifying wrong message digest algorithms
		catch(NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
}
