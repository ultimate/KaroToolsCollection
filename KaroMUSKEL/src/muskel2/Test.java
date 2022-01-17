package muskel2;

import java.io.IOException;

import muskel2.core.karoaccess.KaropapierLoader;

public class Test
{
	public static void main(String[] args) throws IOException
	{		
		System.out.println(KaropapierLoader.createUnlockKey("a"));
		System.out.println(KaropapierLoader.createUnlockKey("A"));
		System.out.println(KaropapierLoader.createUnlockKey("da"));
		System.out.println(KaropapierLoader.createUnlockKey("bc"));
		System.out.println(KaropapierLoader.createUnlockKey("ultimate"));
		System.out.println(KaropapierLoader.createUnlockKey("uLtImATe"));
		System.out.println(KaropapierLoader.createUnlockKey("linkema"));
		System.out.println(KaropapierLoader.createUnlockKey("LINKEMA"));
		System.out.println(KaropapierLoader.createUnlockKey("Robert"));
		System.out.println(KaropapierLoader.createUnlockKey("robert"));
		System.out.println(KaropapierLoader.createUnlockKey("BCM1860"));
		System.out.println(KaropapierLoader.createUnlockKey("bcm1860"));
	}
}
