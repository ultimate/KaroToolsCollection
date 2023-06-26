package ultimate.karoapi4j.utils;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
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
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
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
	public static final String				DATE_FORMAT			= "yyyy-MM-dd HH:mm:ss";
	/**
	 * The date format used
	 */
	public static final String				FILTER_UNOFFICIAL	= "unofficial";
	/**
	 * Logger instance
	 */
	private static transient final Logger	logger				= LogManager.getLogger(JSONUtil.class);
	/**
	 * the Jackson {@link ObjectWriter}
	 */
	private static final ObjectWriter		writer;
	/**
	 * the Jackson {@link ObjectWriter} for formatted Output
	 */
	private static final ObjectWriter		prettyWriter;
	/**
	 * the Jackson {@link ObjectReader}
	 */
	private static final ObjectReader		reader;

	private static final UnofficialFilter	unofficialFilter;

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
		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		df.setTimeZone(TimeZone.getTimeZone("CET"));
		mapper.setDateFormat(df);
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

		// add a filter so that we can filter out "unofficial" fields in "official" use cases
		SimpleFilterProvider filterProvider = new SimpleFilterProvider();
		unofficialFilter = new UnofficialFilter(true); // make the filter statically accessible, so we can change it at runtim
		filterProvider.addFilter(FILTER_UNOFFICIAL, unofficialFilter);
		// register the filter
		mapper.setFilterProvider(filterProvider);

		// now create writer and reader
		writer = mapper.writer();
		prettyWriter = mapper.writerWithDefaultPrettyPrinter();
		reader = mapper.reader();
	}

	/**
	 * Serialize any given Object to JSON.<br>
	 * Short for <code>JSONUtil.serialize(o, false);</code>
	 * 
	 * @see JSONUtil#serialize(Object, boolean)
	 * @param o - the object to serialize
	 * @return the json string
	 * @throws SerializationException - if an Exception occurs, wrapping the original Exception
	 */
	public static synchronized String serialize(Object o) throws SerializationException
	{
		return serialize(o, false);
	}

	/**
	 * Serialize any given Object to JSON
	 * 
	 * @param o - the object to serialize
	 * @param prettyPrint - whether to format the JSON
	 * @return the json string
	 * @throws SerializationException - if an Exception occurs, wrapping the original Exception
	 */
	public static synchronized String serialize(Object o, boolean prettyPrint) throws SerializationException
	{
		return serialize(o, false, prettyPrint);
	}

	/**
	 * Serialize any given Object to JSON
	 * 
	 * @param o - the object to serialize
	 * @param includeUnoffical - also include unofficial properties?
	 * @param prettyPrint - whether to format the JSON
	 * @return the json string
	 * @throws SerializationException - if an Exception occurs, wrapping the original Exception
	 */
	public static synchronized String serialize(Object o, boolean includeUnofficial, boolean prettyPrint) throws SerializationException
	{
		boolean previous_includeUnofficial = unofficialFilter.isIncludeUnofficial();
		try
		{
			unofficialFilter.setIncludeUnofficial(includeUnofficial);
			if(prettyPrint)
				return prettyWriter.writeValueAsString(o);
			else
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
		finally
		{
			unofficialFilter.setIncludeUnofficial(previous_includeUnofficial);
		}
	}

	/**
	 * Deserialize a JSON string to a generic object:<br>
	 * <ul>
	 * <li>If the JSON is a Object in the form <code>{...}</code> this method will produce a
	 * <code>Map&lt;String,Object&gt;</code></li>
	 * <li>If the JSON is an Array in the form <code>[...]</code> this method will produce a
	 * <code>List&lt;Object&gt;</code></li>
	 * </ul>
	 * Note: if you know the object type to deserialize consider using
	 * {@link JSONUtil#deserialize(String, TypeReference)} instead.
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
	 * Deserialize a JSON string to a given Type when the desired Object is wrapped into a container
	 * in the form
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

	public static class UnofficialFilter implements PropertyFilter
	{
		private boolean includeUnofficial;

		public UnofficialFilter(boolean initialState)
		{
			this.includeUnofficial = initialState;
		}

		public boolean isIncludeUnofficial()
		{
			return includeUnofficial;
		}

		public void setIncludeUnofficial(boolean includeUnofficial)
		{
			this.includeUnofficial = includeUnofficial;
		}

		@Override
		public void serializeAsField(Object pojo, JsonGenerator gen, SerializerProvider prov, PropertyWriter writer) throws Exception
		{
			if(writer.getAnnotation(JsonFilter.class) != null && writer.getAnnotation(JsonFilter.class).value().equalsIgnoreCase(FILTER_UNOFFICIAL) && !this.includeUnofficial)
			{
				logger.trace("skipping " + writer.getName());
				return;
			}

			writer.serializeAsField(pojo, gen, prov);
		}

		@Override
		public void serializeAsElement(Object elementValue, JsonGenerator gen, SerializerProvider prov, PropertyWriter writer) throws Exception
		{
			// forward to default
			writer.serializeAsElement(elementValue, gen, prov);
		}

		@Override
		@Deprecated
		public void depositSchemaProperty(PropertyWriter writer, ObjectNode propertiesNode, SerializerProvider provider) throws JsonMappingException
		{
			// forward to default
			writer.depositSchemaProperty(propertiesNode, provider);
		}

		@Override
		public void depositSchemaProperty(PropertyWriter writer, JsonObjectFormatVisitor objectVisitor, SerializerProvider provider) throws JsonMappingException
		{
			// forward to default
			writer.depositSchemaProperty(objectVisitor, provider);
		}
	}

	/**
	 * Simple Parser that uses {@link JSONUtil#deserialize(String, TypeReference)} to process JSON
	 * strings.<br>
	 * The Parser implements {@link Function} in order to be usable for example for
	 * {@link CompletableFuture} as produced by the {@link KaroAPI}
	 * 
	 * @author ultimate
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
	 * Simple Parser that uses {@link JSONUtil#deserializeContainer(String, TypeReference, String)}
	 * to process JSON strings.<br>
	 * The Parser implements {@link Function} in order to be usable for example for
	 * {@link CompletableFuture} as produced by the {@link KaroAPI}
	 * 
	 * @author ultimate
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
	 * Simple interface for a look up entity that can be used to look up objects by their ID and
	 * type
	 * 
	 * @author ultimate
	 */
	public static interface IDLookUp
	{
		/**
		 * Get the object of the given type with the given id
		 * 
		 * @param <T> - the type
		 * @param cls - the type class
		 * @param id - the id
		 * @return the object
		 */
		public <T> T get(Class<T> cls, int id);
	}

	/**
	 * The look up entity
	 */
	private static IDLookUp lookUp;

	/**
	 * Get the {@link IDLookUp} entity.<br>
	 * This entity is used to look up entity by their during deserialization.
	 * 
	 * @return The look up entity
	 */
	public static IDLookUp getLookUp()
	{
		return lookUp;
	}

	/**
	 * Set the {@link IDLookUp} entity.<br>
	 * This entity is used to look up entity by their during deserialization.
	 * 
	 * @param lookUp - The look up entity
	 */
	public static void setLookUp(IDLookUp lookUp)
	{
		JSONUtil.lookUp = lookUp;
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
			if(value == null)
				return null;
			return value.getId();
		}
	}

	/**
	 * Custom converter that can be used to convert {@link List} of {@link Identifiable} to IDs only
	 * 
	 * @author ultimate
	 */
	public static class ToIDArrayConverter<T extends Identifiable> extends StdConverter<Collection<T>, int[]>
	{
		@Override
		public int[] convert(Collection<T> value)
		{
			if(value == null)
				return null;
			return CollectionsUtil.toIDArray(value);
		}
	}

	/**
	 * Custom converter that can be used to convert {@link Map} of {@link Identifiable} to IDs only
	 * 
	 * @author ultimate
	 */
	public static class ToIDMapConverter<T extends Identifiable> extends StdConverter<java.util.Map<String, ? extends Collection<T>>, java.util.Map<String, int[]>>
	{
		@Override
		public java.util.Map<String, int[]> convert(java.util.Map<String, ? extends Collection<T>> value)
		{
			if(value == null)
				return null;

			HashMap<String, int[]> map = new HashMap<>();
			for(Entry<String, ? extends Collection<T>> e : value.entrySet())
				map.put(e.getKey(), CollectionsUtil.toIDArray(e.getValue()));
			return map;
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
					logger.trace("lookup " + classRef.getSimpleName() + " #" + id);
					T result = lookUp.get(classRef, id);
					logger.debug("lookup " + classRef.getSimpleName() + " #" + id + " --> " + result);
					return result;
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
				logger.error("could not instantiate Identifiable", e);
			}
			return null;
		}
	}

	/**
	 * Custom converter that can be used to convert IDs back to a {@link Collection} of
	 * {@link Identifiable}
	 * 
	 * @author ultimate
	 */
	public static abstract class FromIDArrayConverter<T extends Identifiable, C extends Collection<T>> extends StdConverter<int[], C>
	{
		private FromIDConverter<T> fromIDConverter;

		public FromIDArrayConverter(Class<T> classRef)
		{
			super();
			this.fromIDConverter = new FromIDConverter<>(classRef);
		}

		@Override
		public C convert(int[] array)
		{
			C list = initCollection(array);
			for(int id : array)
				list.add(fromIDConverter.convert(id));
			return list;
		}

		protected abstract C initCollection(int[] array);
	}

	/**
	 * Custom converter that can be used to convert IDs back to a {@link List} of
	 * {@link Identifiable}
	 * 
	 * @author ultimate
	 */
	public static class FromIDArrayToListConverter<T extends Identifiable> extends FromIDArrayConverter<T, List<T>>
	{
		public FromIDArrayToListConverter(Class<T> classRef)
		{
			super(classRef);
		}

		@Override
		protected List<T> initCollection(int[] array)
		{
			return new ArrayList<T>(array.length);
		}
	}

	/**
	 * Custom converter that can be used to convert IDs back to a {@link Set} of
	 * {@link Identifiable}
	 * 
	 * @author ultimate
	 */
	public static class FromIDArrayToSetConverter<T extends Identifiable> extends FromIDArrayConverter<T, Set<T>>
	{
		public FromIDArrayToSetConverter(Class<T> classRef)
		{
			super(classRef);
		}

		@Override
		protected Set<T> initCollection(int[] array)
		{
			return new LinkedHashSet<>(array.length);
		}
	}

	/**
	 * Custom converter that can be used to convert IDs back to a {@link Map} of {@link Collection}s of
	 * {@link Identifiable}
	 * 
	 * @author ultimate
	 */
	public static abstract class FromIDMapConverter<T extends Identifiable, C extends Collection<T>> extends StdConverter<java.util.Map<String, int[]>, java.util.Map<String, C>>
	{
		private FromIDArrayConverter<T, C> fromIDArrayConverter;

		public FromIDMapConverter(Class<T> classRef, FromIDArrayConverter<T, C> fromIDArrayConverter)
		{
			super();
			this.fromIDArrayConverter = fromIDArrayConverter;
		}

		@Override
		public java.util.Map<String, C> convert(java.util.Map<String, int[]> arrayMap)
		{
			HashMap<String, C> map = new HashMap<>();
			for(Entry<String, int[]> e : arrayMap.entrySet())
				map.put(e.getKey(), fromIDArrayConverter.convert(e.getValue()));
			return map;
		}

		protected abstract C initCollection(int[] array);
	}

	/**
	 * Custom converter that can be used to convert IDs back to a {@link Map} of {@link List}s of
	 * {@link Identifiable}
	 * 
	 * @author ultimate
	 */
	public static class FromIDMapToListConverter<T extends Identifiable> extends FromIDMapConverter<T, List<T>>
	{
		public FromIDMapToListConverter(Class<T> classRef)
		{
			super(classRef, new FromIDArrayToListConverter<>(classRef));
		}

		@Override
		protected List<T> initCollection(int[] array)
		{
			return new ArrayList<T>(array.length);
		}
	}

	/**
	 * Custom converter that can be used to convert IDs back to a {@link Map} of {@link Set}s of
	 * {@link Identifiable}
	 * 
	 * @author ultimate
	 */
	public static class FromIDMapToSetConverter<T extends Identifiable> extends FromIDMapConverter<T, Set<T>>
	{
		public FromIDMapToSetConverter(Class<T> classRef)
		{
			super(classRef, new FromIDArrayToSetConverter<>(classRef));
		}

		@Override
		protected Set<T> initCollection(int[] array)
		{
			return new LinkedHashSet<>(array.length);
		}
	}
}
