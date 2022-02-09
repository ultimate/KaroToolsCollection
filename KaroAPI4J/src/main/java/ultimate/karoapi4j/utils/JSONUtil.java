package ultimate.karoapi4j.utils;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
import com.fasterxml.jackson.databind.util.StdConverter;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.exceptions.DeserializationException;
import ultimate.karoapi4j.exceptions.SerializationException;
import ultimate.karoapi4j.model.base.Identifiable;

/**
 * Util class for abstracting JSON serialization and deserialization using Jackson
 * 
 * @author ultimate
 */
public abstract class JSONUtil
{
	/**
	 * The date format used
	 */
	public static final String			DATE_FORMAT	= "yyyy-MM-dd HH:mm:ss";
	/**
	 * Logger instance
	 */
	private static final Logger			logger		= LoggerFactory.getLogger(JSONUtil.class);
	/**
	 * the Jackson {@link ObjectWriter}
	 */
	private static final ObjectWriter	writer;
	/**
	 * the Jackson {@link ObjectReader}
	 */
	private static final ObjectReader	reader;

	/**
	 * prevent instantiation
	 */
	private JSONUtil()
	{

	}

	/**
	 * Instantiate and configure the static {@link JSONUtil#writer} and {@link JSONUtil#reader}
	 */
	static
	{
		ObjectMapper mapper = new ObjectMapper();

		// set the date format
		mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
		// set the sort order for maps
		mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		// if there are unknown properties in the JSON -> don't fail (they will be ignored)
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// create a module for additional configuration
		SimpleModule module = new SimpleModule();
		// add custom serializers and deserializers
		// we need a custom (de)-serializer for Color, since the color is sent as a hex-String
		module.addSerializer(Color.class, new ColorSerializer());
		module.addDeserializer(Color.class, new ColorDeserializer());
		// register the module
		mapper.registerModule(module);

		// now create writer and reader
		writer = mapper.writer();
		reader = mapper.reader();
	}

	/**
	 * Serialize any given Object to JSON
	 * 
	 * @param o - the object to serialize
	 * @return the json string
	 * @throws SerializationException - if an Exception occurs, wrapping the original Exception
	 */
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

	/**
	 * Deserialize a JSON string to a generic object:<br>
	 * <ul>
	 * <li>If the JSON is a Object in the form <code>{...}</code> this method will produce a <code>Map&lt;String,Object&gt;</code></li>
	 * <li>If the JSON is an Array in the form <code>[...]</code> this method will produce a <code>List&lt;Object&gt;</code></li>
	 * </ul>
	 * Note: if you know the object type to deserialize consider using {@link JSONUtil#deserialize(String, TypeReference)} instead.
	 * 
	 * @see JSONUtil#deserialize(String, TypeReference)
	 * @param serialization - the JSON string
	 * @return the deserialized object or list
	 * @throws DeserializationException - if an Exception occurs, wrapping the original Exception
	 */
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
			return null;
		}
	}

	/**
	 * Deserialize a JSON string to a given Type
	 * 
	 * @param <T> - the object type
	 * @param serialization - the JSON string
	 * @param typeReference - the {@link TypeReference} to set the type
	 * @return the deserialized object in the desired type
	 * @throws DeserializationException
	 */
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

	/**
	 * Deserialize a JSON string to a given Type when the desired Object is wrapped into a container in the form
	 * <code>{"key":{ .. actual object .. }}
	 * 
	 * @param <T> - the object type
	 * @param serialization - the JSON string
	 * @param typeReference - the {@link TypeReference} to set the type
	 * @param key - the key that is containing the object
	 * @return the deserialized object in the desired type
	 * @throws DeserializationException
	 */
	public static <T> T deserializeContainer(String serialization, TypeReference<T> typeReference, String key) throws DeserializationException
	{
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

	/**
	 * A custom {@link JsonSerializer} for {@link Color} writing the color to a hex String
	 * 
	 * @author ultimate
	 */
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

	/**
	 * A custom {@link JsonDeserializer} for {@link Color} reading the color from a hex String
	 * 
	 * @author ultimate
	 */
	public static class ColorDeserializer extends JsonDeserializer<Color>
	{
		@Override
		public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
		{
			return new Color(Integer.parseUnsignedInt(p.getText(), 16));
		}
	}

	public static interface IDLookUp
	{
		public <T> T get(Class<T> cls, int id);
	}

	private static IDLookUp lookUp;

	public static IDLookUp getLookUp()
	{
		return lookUp;
	}

	public static void setLookUp(IDLookUp lookUp)
	{
		JSONUtil.lookUp = lookUp;
	}

	/**
	 * A custom {@link JsonSerializer} for {@link List}s containing {@link Identifiable} pojos writing only the IDs
	 * 
	 * @author ultimate
	 */
	public static class IDSerializer<T extends Identifiable> extends JsonSerializer<T>
	{
		@Override
		public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException
		{
			if(value != null && value.getId() != null)
				gen.writeNumber(value.getId());
			else
				gen.writeNull();
		}
	}

	/**
	 * A custom {@link JsonDeserializer} for {@link List}s containing {@link Identifiable} pojos reading the objects from IDs only
	 * 
	 * @author ultimate
	 */
	public static class IDDeserializer<T extends Identifiable> extends JsonDeserializer<T>
	{
		private Class<T> classRef;

		public IDDeserializer(Class<T> classRef)
		{
			super();
			this.classRef = classRef;
		}

		@Override
		public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
		{
			int id = p.getNumberValue().intValue();
			return get(id);
		}

		public T get(int id)
		{
			try
			{
				if(lookUp != null)
				{
					return lookUp.get(classRef, id);
				}
				else
				{
					T t = classRef.getDeclaredConstructor().newInstance();
					t.setId(id);
					return t;
				}
			}
			catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
			{
				logger.error("could instantiate Identifiable", e);
			}
			return null;
		}
	}

	/**
	 * A custom {@link JsonSerializer} for {@link List}s containing {@link Identifiable} pojos writing only the IDs
	 * 
	 * @author ultimate
	 */
	public static class IDListSerializer<T extends Identifiable> extends JsonSerializer<List<T>>
	{
		@Override
		public void serialize(List<T> value, JsonGenerator gen, SerializerProvider serializers) throws IOException
		{
			int[] idArray = CollectionsUtil.toIDArray(value);
			gen.writeArray(idArray, 0, idArray.length);
		}
	}

	/**
	 * A custom {@link JsonDeserializer} for {@link List}s containing {@link Identifiable} pojos reading the objects from IDs only
	 * 
	 * @author ultimate
	 */
	public static class IDListDeserializer<T extends Identifiable> extends JsonDeserializer<List<T>>
	{
		private IDDeserializer<T> idDeserializer;

		public IDListDeserializer(Class<T> classRef)
		{
			super();
			this.idDeserializer = new IDDeserializer<>(classRef);
		}

		@Override
		public List<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
		{
			int[] array = p.readValueAs(int[].class);
			ArrayList<T> list = new ArrayList<>(array.length);
			for(int id : array)
				list.add(idDeserializer.get(id));
			return list;
		}
	}

	/**
	 * Simple Parser that uses {@link JSONUtil#deserialize(String, TypeReference)} to process JSON strings.<br>
	 * The Parser implements {@link Function} in order to be usable for example for {@link CompletableFuture} as produced by the {@link KaroAPI}
	 * 
	 * @author ultimate
	 *
	 * @param <E> - the Type
	 */
	public static class Parser<E> implements Function<String, E>
	{
		/**
		 * A TypeReference for JSON-Deserialization
		 */
		protected TypeReference<E> typeRef = new TypeReference<E>() {};

		/**
		 * Create a new Parser for the given {@link TypeReference}
		 * 
		 * @param typeRef - the {@link TypeReference}
		 */
		public Parser(TypeReference<E> typeRef)
		{
			super();
			this.typeRef = typeRef;
		}

		/**
		 * @see JSONUtil#deserialize(String, TypeReference)
		 */
		@Override
		public E apply(String in)
		{
			return JSONUtil.deserialize(in, typeRef);
		}
	}

	/**
	 * Simple Parser that uses {@link JSONUtil#deserializeContainer(String, TypeReference, String)} to process JSON strings.<br>
	 * The Parser implements {@link Function} in order to be usable for example for {@link CompletableFuture} as produced by the {@link KaroAPI}
	 * 
	 * @author ultimate
	 *
	 * @param <E> - the Type
	 */
	public static class ContainerParser<E> extends Parser<E>
	{
		protected String key;

		/**
		 * Create a new Parser for the given {@link TypeReference} and the container key
		 * 
		 * @param typeRef - the {@link TypeReference}
		 */
		public ContainerParser(TypeReference<E> typeRef, String key)
		{
			super(typeRef);
			this.key = key;
		}

		/**
		 * @see JSONUtil#deserializeContainer(String, TypeReference, String)
		 */
		@Override
		public E apply(String in)
		{
			return JSONUtil.deserializeContainer(in, typeRef, key);
		}
	}

	/**
	 * Constant for Time-Stamp Conversion as required for the {@link KaroAPI}
	 */
	public static final int DATE_FACTOR = 1000;

	/**
	 * Custom converter that can be used to convert timestamps to {@link Date}.<br>
	 * Uses {@link JSONUtil#DATE_FACTOR} for the conversion
	 * 
	 * @author ultimate
	 */
	public static class TimestampConverter extends StdConverter<Long, Date>
	{
		@Override
		public Date convert(Long value)
		{
			return new Date(value * DATE_FACTOR);
		}
	}

	/**
	 * Custom converter that can be used to convert {@link Identifiable} to IDs only
	 * 
	 * @author ultimate
	 */
	public static class ToIDConverter<T extends Identifiable> extends StdConverter<T, Integer>
	{
		@Override
		public Integer convert(T value)
		{
			if(value != null)
				return value.getId();
			return null;
		}
	}

	/**
	 * Custom converter that can be used to convert IDs back to {@link Identifiable}
	 * 
	 * @author ultimate
	 */
	public static class FromIDConverter<T extends Identifiable> extends StdConverter<Integer, T>
	{
		private Class<T> classRef;

		public FromIDConverter(Class<T> classRef)
		{
			super();
			this.classRef = classRef;
		}
		
		@Override
		public T convert(Integer id)
		{
			try
			{
				if(lookUp != null)
				{
					return lookUp.get(classRef, id);
				}
				else
				{
					T t = classRef.getDeclaredConstructor().newInstance();
					t.setId(id);
					return t;
				}
			}
			catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
			{
				logger.error("could instantiate Identifiable", e);
			}
			return null;
		}
	}
}
