package ultimate.karoapi4j.utils;

import java.util.Objects;

public class Version implements Comparable<Version>
{
	public static final String	DELIMITERS	= "[\\.-]";

	private int					major;
	private int					minor;
	private int					patch;

	public Version(String version)
	{
		version = version.replace("a", ".1").replace("b", ".2");

		String[] parts = version.split(DELIMITERS);

		major = safeParse(parts[0]);
		minor = (parts.length > 1 ? safeParse(parts[1]) : 0);
		patch = (parts.length > 2 ? safeParse(parts[2]) : 0);
	}

	private int safeParse(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch(Exception e)
		{
			return 0;
		}
	}

	@Override
	public String toString()
	{
		return major + "." + minor + "." + patch;
	}

	@Override
	public int compareTo(Version o)
	{
		if(this.major == o.major)
		{
			if(this.minor == o.minor)
			{
				return this.patch - o.patch;
			}
			return this.minor - o.minor;
		}
		return this.major - o.major;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(major, minor, patch);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		return major == other.major && minor == other.minor && patch == other.patch;
	}
}