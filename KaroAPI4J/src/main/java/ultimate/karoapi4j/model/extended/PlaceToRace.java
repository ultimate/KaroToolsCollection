package ultimate.karoapi4j.model.extended;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;

public interface PlaceToRace
{
	public static class Serializer extends JsonSerializer<PlaceToRace>
	{
		@Override
		public void serialize(PlaceToRace value, JsonGenerator gen, SerializerProvider serializers) throws IOException
		{
			if(value instanceof Map)
				serializers.defaultSerializeValue(((Map) value).getId(), gen);
			else if(value instanceof Generator)
				serializers.defaultSerializeValue(((Generator) value), gen);
			else
				serializers.defaultSerializeNull(gen);
		}
	}

	public static class Deserializer extends JsonDeserializer<PlaceToRace>
	{
		@Override
		public PlaceToRace deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
		{
			final JsonToken token = p.getCurrentToken();

			if(JsonToken.START_OBJECT.equals(token))
				return (Generator) ctxt.findRootValueDeserializer(ctxt.constructType(Generator.class)).deserialize(p, ctxt);
			else if(JsonToken.VALUE_NUMBER_INT.equals(token))
				return (Map) ctxt.findRootValueDeserializer(ctxt.constructType(Map.class)).deserialize(p, ctxt);
			return (PlaceToRace) ctxt.handleUnexpectedToken(PlaceToRace.class, p);
		}
	}

	public static class ListSerializer extends JsonSerializer<List<PlaceToRace>>
	{
		@Override
		public void serialize(List<PlaceToRace> value, JsonGenerator gen, SerializerProvider serializers) throws IOException
		{
			gen.writeStartArray();
			for(PlaceToRace ptr : value)
			{
				if(ptr instanceof Map)
					serializers.defaultSerializeValue(((Map) ptr).getId(), gen);
				else if(ptr instanceof Generator)
					serializers.defaultSerializeValue(((Generator) ptr), gen);
				else
					serializers.defaultSerializeNull(gen);
			}
			gen.writeEndArray();
		}
	}

	public static class ListDeserializer extends JsonDeserializer<List<PlaceToRace>>
	{
		@Override
		public List<PlaceToRace> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
		{
			JsonToken token = p.getCurrentToken();
			if(JsonToken.START_ARRAY.equals(token))
			{
				List<PlaceToRace> list = new LinkedList<>();
				token = p.nextToken(); // go to first entry in list
				while(!JsonToken.END_ARRAY.equals(token))
				{
					if(JsonToken.START_OBJECT.equals(token))
					{
						list.add((Generator) ctxt.findRootValueDeserializer(ctxt.constructType(Generator.class)).deserialize(p, ctxt));
					}
					else if(JsonToken.VALUE_NUMBER_INT.equals(token))
					{
						list.add((Map) ctxt.findRootValueDeserializer(ctxt.constructType(Map.class)).deserialize(p, ctxt));
					}
					token = p.nextToken();
				}
				return list;
			}
			else
			{
				@SuppressWarnings("unchecked")
				List<PlaceToRace> unhandled = (List<PlaceToRace>) ctxt.handleUnexpectedToken(ctxt.getTypeFactory().constructCollectionType(List.class, PlaceToRace.class), p);
				return unhandled;
			}
		}
	}

	public static class ListMapSerializer extends JsonSerializer<java.util.Map<String, List<PlaceToRace>>>
	{
		private ListSerializer listSerializer = new ListSerializer();

		@Override
		public void serialize(java.util.Map<String, List<PlaceToRace>> value, JsonGenerator gen, SerializerProvider serializers) throws IOException
		{
			gen.writeStartObject();
			Iterator<Entry<String, List<PlaceToRace>>> iter = value.entrySet().iterator();
			Entry<String, List<PlaceToRace>> entry;
			while(iter.hasNext())
			{
				entry = iter.next();
				gen.writeFieldName(entry.getKey());
				this.listSerializer.serialize(entry.getValue(), gen, serializers);
			}
			gen.writeEndObject();
		}
	}

	public static class ListMapDeserializer extends JsonDeserializer<java.util.Map<String, List<PlaceToRace>>>
	{
		private ListDeserializer listDeserializer = new ListDeserializer();

		@Override
		public java.util.Map<String, List<PlaceToRace>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
		{
			JsonToken token = p.getCurrentToken();
			if(JsonToken.START_OBJECT.equals(token))
			{
				HashMap<String, List<PlaceToRace>> map = new HashMap<>();
				token = p.nextToken(); // go to first entry in list
				while(JsonToken.FIELD_NAME.equals(token))
				{
					String key = p.getValueAsString();
					token = p.nextToken(); // go to value
					List<PlaceToRace> value = listDeserializer.deserialize(p, ctxt);
					token = p.nextToken(); // go to next element
					map.put(key, value);
				}
				return map;
			}
			else
			{
				@SuppressWarnings("unchecked")
				java.util.Map<String, List<PlaceToRace>> unhandled = (java.util.Map<String, List<PlaceToRace>>) ctxt.handleUnexpectedToken(ctxt.getTypeFactory().constructMapType(java.util.Map.class,
						ctxt.constructType(String.class), (JavaType) ctxt.getTypeFactory().constructCollectionType(List.class, PlaceToRace.class)), p);
				return unhandled;
			}
		}
	}

	public boolean isNight();

	public int getPlayers();

	default public int getPlayersMax()
	{
		return getPlayers();
	};

	default public int getPlayersMin()
	{
		return getPlayers();
	};
}
