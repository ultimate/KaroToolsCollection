package ultimate.karoapi4j.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import ultimate.karoapi4j.exceptions.DeserializationException;
import ultimate.karoapi4j.exceptions.SerializationException;

public abstract class JSONUtil
{
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final Logger logger = LoggerFactory.getLogger(JSONUtil.class);
	private static final ObjectWriter	writer;
	private static final ObjectReader	reader;

	static
	{
		ObjectMapper mapper = new ObjectMapper();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		mapper.setDateFormat(dateFormat);

		writer = mapper.writer();
		reader = mapper.reader();// .withType(new TypeReference<Map<String, Object>>() {});
	}

	public static String serialize(Object o)
	{
		try
		{
			return writer.writeValueAsString(o);
		}
		catch(JsonGenerationException e)
		{
			throw new SerializationException("JsonGenerationException", e);
		}
		catch(JsonMappingException e)
		{
			throw new SerializationException("JsonMappingException", e);
		}
		catch(IOException e)
		{
			throw new SerializationException("IOException", e);
		}
	}

	public static Object deserialize(String serialization) throws DeserializationException
	{
		if(serialization.startsWith("{"))
		{
			return deserialize(serialization, new TypeReference<Map<String, Object>>() {});
		}
		else if(serialization.startsWith("["))
		{
			return deserialize(serialization, new TypeReference<ArrayList<Object>>() {});
		}
		else
		{
			logger.error("Could not determine type of serialization");
			System.out.println("oops");
			return null;
		}
	}

	public static <T> T deserialize(String serialization, TypeReference<T> typeReference) throws DeserializationException
	{
		try
		{
			return reader.withType(typeReference).readValue(serialization);
		}
		catch(JsonParseException e)
		{
			throw new DeserializationException("JsonParseException", e);
		}
		catch(JsonMappingException e)
		{
			throw new DeserializationException("JsonMappingException", e);
		}
		catch(IOException e)
		{
			throw new DeserializationException("IOException", e);
		}
	}
}
