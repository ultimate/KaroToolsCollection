package ultimate.karoapi4j.utils;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import ultimate.karoapi4j.exceptions.DeserializationException;
import ultimate.karoapi4j.exceptions.SerializationException;

// TODO javadoc
public abstract class JSONUtil
{
	public static final String			DATE_FORMAT	= "yyyy-MM-dd HH:mm:ss";

	private static final Logger			logger		= LoggerFactory.getLogger(JSONUtil.class);
	private static final ObjectWriter	writer;
	private static final ObjectReader	reader;

	/**
	 * prevent instantiation
	 */
	private JSONUtil()
	{

	}

	static
	{
		ObjectMapper mapper = new ObjectMapper();

		mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		SimpleModule module = new SimpleModule();
		// add serializers and deserializers
		module.addSerializer(Color.class, new ColorSerializer());
		module.addDeserializer(Color.class, new ColorDeserializer());

		mapper.registerModule(module);

		writer = mapper.writer();
		reader = mapper.reader();
	}

	public static String serialize(Object o) throws SerializationException
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
			return reader.forType(typeReference).readValue(serialization);
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

	public static <T> T deserializeContainer(String serialization, TypeReference<T> typeReference, String key) throws DeserializationException
	{
		// if(!serialization.startsWith("{"))
		// throw new DeserializationException("format mismatch");
		// @SuppressWarnings("unchecked")
		// Map<String, Object> container = (Map<String, Object>) JSONUtil.deserialize(serialization);
		// Object entity = container.get(key);
		// String entityReSerialized = JSONUtil.serialize(entity);
		// return JSONUtil.deserialize(entityReSerialized, typeReference);

		String start1 = "{" + key + ":";
		String start2 = "{\"" + key + "\":";
		String end = "}";
		if(!(serialization.startsWith(start1) || serialization.startsWith(start2)) || !serialization.endsWith(end))
			throw new DeserializationException("format mismatch");
		String entity;
		if(serialization.startsWith(start1))
			entity = serialization.substring(start1.length(), serialization.length() - end.length());
		else
			entity = serialization.substring(start2.length(), serialization.length() - end.length());
		return deserialize(entity, typeReference);
	}

	public static class ColorSerializer extends JsonSerializer<Color>
	{
		@Override
		public void serialize(Color value, JsonGenerator gen, SerializerProvider serializers) throws IOException
		{
			String hex = Integer.toHexString(0x00FFFFFF & value.getRGB());
			while(hex.length() < 6)
				hex = "0" + hex;
			gen.writeString(hex);
		}
	}

	public static class ColorDeserializer extends JsonDeserializer<Color>
	{
		@Override
		public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
		{
			return new Color(Integer.parseUnsignedInt(p.getText(), 16));
		}
	}

	// TODO javadoc
	public static class Parser<E> implements Function<String, E>
	{
		/**
		 * A TypeReference for JSON-Deserialization
		 */
		protected TypeReference<E> typeRef = new TypeReference<E>() {};

		public Parser(TypeReference<E> typeRef)
		{
			super();
			this.typeRef = typeRef;
		}

		@Override
		public E apply(String in)
		{
			return JSONUtil.deserialize(in, typeRef);
		}
	}

	public static class ContainerParser<E> extends Parser<E>
	{
		protected String key;

		public ContainerParser(TypeReference<E> typeRef, String key)
		{
			super(typeRef);
			this.key = key;
		}

		@Override
		public E apply(String in)
		{
			return JSONUtil.deserializeContainer(in, typeRef, key);
		}
	}

	public static final int DATE_FACTOR = 1000;

	public static class TimestampConverter implements Converter<Long, Date>
	{
		@Override
		public Date convert(Long value)
		{
			return new Date(value * DATE_FACTOR);
		}

		@Override
		public JavaType getInputType(TypeFactory typeFactory)
		{
			return typeFactory.constructType(Long.class);
		}

		@Override
		public JavaType getOutputType(TypeFactory typeFactory)
		{
			return typeFactory.constructType(Date.class);
		}
	}
}
