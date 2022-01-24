package ultimate.karoapi4j.utils;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ultimate.karoapi4j.exceptions.DeserializationException;
import ultimate.karoapi4j.exceptions.SerializationException;
import ultimate.karoapi4j.model.base.Identifiable;

public abstract class JSONUtil
{
	public static final String			DATE_FORMAT	= "yyyy-MM-dd HH:mm:ss";

	private static final Logger			logger		= LoggerFactory.getLogger(JSONUtil.class);
	private static final ObjectWriter	writer;
	private static final ObjectReader	reader;

	static
	{
		ObjectMapper mapper = new ObjectMapper();
		
		mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		SimpleModule module = new SimpleModule();
		// add serializers and deserializers
		// color
		module.addSerializer(Color.class, new ColorSerializer());
		module.addDeserializer(Color.class, new ColorDeserializer());
		// player
//		module.addC
//		module.addDeserializer(Color.class, new ColorDeserializer());

		mapper.registerModule(module);

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
//
//	public static class IDOnlySerializer<T extends Identifiable> extends JsonSerializer<T>
//	{
//		@Override
//		public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException
//		{
//			if(value != null)
//				gen.writeNumber(value.getId());
//			else
//				gen.writeNull();
//		}
//	}
//
//	public static class IDOnlyDeserializer<T extends Identifiable> extends JsonDeserializer<T>
//	{
//		private Constructor<T>	constructor;
//
//		public IDOnlyDeserializer(Class<T> cls) throws NoSuchMethodException, SecurityException
//		{
//			this.constructor = cls.getConstructor(Integer.class);
//		}
//
//		@Override
//		public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
//		{
//			T object = null;
//			if(p.currentToken() == JsonToken.VALUE_NUMBER_INT)
//			{
//				try
//				{
//					object = constructor.newInstance(p.getIntValue());
//				}
//				catch(Exception e)
//				{
//					logger.error("unexpected exception", e);
//					e.printStackTrace();
//				}
//			}
//			else
//			{
//				// null
//			}
//			return object;
//		}
//	}
//
//	public static class IDListSerializer<T extends Identifiable> extends JsonSerializer<List<T>>
//	{
//		private IDOnlySerializer<T> objectSerializer = new IDOnlySerializer<>();
//
//		@Override
//		public void serialize(List<T> list, JsonGenerator gen, SerializerProvider serializers) throws IOException
//		{
//			if(list != null)
//			{
//				gen.writeStartArray();
//				for(T object : list)
//					objectSerializer.serialize(object, gen, serializers);
//				gen.writeEndArray();
//			}
//			else
//			{
//				gen.writeNull();
//			}
//		}
//	}
//
//	public static class IDListDeserializer<T extends Identifiable> extends JsonDeserializer<List<T>>
//	{
//		private IDOnlyDeserializer<T>	objectDeserializer;
//
//		public IDListDeserializer(Class<T> cls) throws NoSuchMethodException, SecurityException
//		{
//			this.objectDeserializer = new IDOnlyDeserializer<>(cls);
//		}
//
//		@Override
//		public List<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
//		{
//			if(p.isExpectedStartArrayToken())
//			{
//				List<T> list = new ArrayList<>();
//				
//				JsonToken next = p.nextToken();
//				while(next == JsonToken.VALUE_NUMBER_INT || next == JsonToken.VALUE_NULL)
//				{
//					next = p.nextToken();
//					objectDeserializer.deserialize(p, ctxt);
//				}				
//				return list;
//			}
//			else if(p.currentToken() == JsonToken.VALUE_NULL)
//			{
//				return null;
//			}
//			else
//			{
//				throw new JsonMappingException(p, "unexpected token: " + p.currentToken());
//			}
//		}
//	}

	public static class Parser<E> implements ultimate.karoapi4j.utils.web.Parser<String, E>
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
		public E parse(String in)
		{
			return JSONUtil.deserialize(in, typeRef);
		}
	}

}
