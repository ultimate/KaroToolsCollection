package muskel2.core.web;

import java.io.Serializable;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Cookie implements Serializable
{

	private static final long	serialVersionUID	= 1L;

	private String				name;
	private String				value;
	private URI					uri;
	private String				domain;
	private Date				expires;
	private String				path;

	private static DateFormat	expiresFormat1		= new SimpleDateFormat("E, dd MMM yyyy k:m:s 'GMT'", Locale.US);

	private static DateFormat	expiresFormat2		= new SimpleDateFormat("E, dd-MMM-yyyy k:m:s 'GMT'", Locale.US);

	/**
	 * Construct a cookie from the URI and header fields
	 * 
	 * @param uri
	 *            URI for cookie
	 * @param header
	 *            Set of attributes in header
	 */
	public Cookie(URI uri, String header)
	{
		String attributes[] = header.split(";");
		String nameValue = attributes[0].trim();
		this.uri = uri;
		this.name = nameValue.substring(0, nameValue.indexOf('='));
		this.value = nameValue.substring(nameValue.indexOf('=') + 1);
		this.path = "/";
		this.domain = uri.getHost();

		for(int i = 1; i < attributes.length; i++)
		{
			nameValue = attributes[i].trim();
			int equals = nameValue.indexOf('=');
			if(equals == -1)
			{
				continue;
			}
			String name = nameValue.substring(0, equals);
			String value = nameValue.substring(equals + 1);
			if(name.equalsIgnoreCase("domain"))
			{
				String uriDomain = uri.getHost();
				if(uriDomain.equals(value))
				{
					this.domain = value;
				}
				else
				{
					if(!value.startsWith("."))
					{
						value = "." + value;
					}
					uriDomain = uriDomain.substring(uriDomain.indexOf('.'));
					if(!uriDomain.equals(value))
					{
						throw new IllegalArgumentException("Trying to set foreign cookie");
					}
					this.domain = value;
				}
			}
			else if(name.equalsIgnoreCase("path"))
			{
				this.path = value;
			}
			else if(name.equalsIgnoreCase("expires"))
			{
				try
				{
					this.expires = expiresFormat1.parse(value);
				}
				catch(ParseException e)
				{
					try
					{
						this.expires = expiresFormat2.parse(value);
					}
					catch(ParseException e2)
					{
						throw new IllegalArgumentException("Bad date format in header: " + value);
					}
				}
			}
		}
	}

	public boolean hasExpired()
	{
		if(expires == null)
		{
			return false;
		}
		Date now = new Date();
		return now.after(expires);
	}

	public String getName()
	{
		return name;
	}

	public URI getURI()
	{
		return uri;
	}

	/**
	 * Check if cookie isn't expired and if URI matches, should cookie be
	 * included in response.
	 * 
	 * @param uri
	 *            URI to check against
	 * @return true if match, false otherwise
	 */
	public boolean matches(URI uri)
	{

		if(hasExpired())
		{
			return false;
		}

		String path = uri.getPath();
		if(path == null)
		{
			path = "/";
		}

		if(!uri.getHost().endsWith(this.domain))
		{
			return false;
		}

		return path.startsWith(this.path);
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder(name);
		result.append("=");
		result.append(value);
		return result.toString();
	}
}
