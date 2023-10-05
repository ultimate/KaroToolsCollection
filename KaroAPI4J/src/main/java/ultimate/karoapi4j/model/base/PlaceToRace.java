package ultimate.karoapi4j.model.base;

import java.io.IOException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;

public class PlaceToRace
{
	//@formatter:off
    public static class PlaceToRaceDeserializer extends JsonDeserializer<PlaceToRace>
    {
        @Override
        public PlaceToRace deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
        {
            final JsonToken token= p.getCurrentToken();

            if (JsonToken.START_OBJECT.equals(token))
                return new PlaceToRace((Generator) ctxt.findRootValueDeserializer(ctxt.constructType(Generator.class)).deserialize(p, ctxt));
            else if (JsonToken.VALUE_NUMBER_INT.equals(token))
                return new PlaceToRace((Map) ctxt.findRootValueDeserializer(ctxt.constructType(Map.class)).deserialize(p, ctxt));
            return (PlaceToRace) ctxt.handleUnexpectedToken(PlaceToRace.class, p);
        }
    }

    public static class PlaceToRaceSerializer extends JsonSerializer<PlaceToRace>
    {
        @Override
        public void serialize(PlaceToRace value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            if(value.isMap())
                serializers.defaultSerializeValue(value.getMap().getId(), gen);
            else if(value.isGenerator())
                serializers.defaultSerializeValue(value.getGenerator(), gen);
            else
                serializers.defaultSerializeNull(gen);
        }
    }

	//public static class FromIDConverter extends JSONUtil.FromIDConverter<Map> { public FromIDConverter() { super(PlaceToRace.class); } };
	//public static class FromIDArrayToListConverter extends JSONUtil.FromIDArrayToListConverter<Map> { public FromIDArrayToListConverter() { super(Map.class); } };
	//public static class FromIDMapToListConverter extends JSONUtil.FromIDMapToListConverter<Map> { public FromIDMapToListConverter() { super(Map.class); } };
	//@formatter:on

    private final Map map;
    private final Generator generator;

    public PlaceToRace(Map map)
    {
        if (map == null)
            throw new IllegalArgumentException("map must not be null!");
        this.map = map;
        this.generator = null;
    }

    public PlaceToRace(Generator generator)
    {
        if (generator == null)
            throw new IllegalArgumentException("generator must not be null!");
        this.map = null;
        this.generator = generator;
    }

    public boolean isMap()
    {
        return this.map != null;
    }

    public boolean isGenerator()
    {
        return this.generator != null;
    }

    public Map getMap()
    {
        return map;
    }

    public Generator getGenerator()
    {
        return generator;
    }

    public boolean isNight()
    {
        if(isMap())
            return map.isNight();
        else
            return generator.isNight();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((map == null) ? 0 : map.hashCode());
        result = prime * result + ((generator == null) ? 0 : generator.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlaceToRace other = (PlaceToRace) obj;
        if (map == null) {
            if (other.map != null)
                return false;
        } else if (!map.equals(other.map))
            return false;
        if (generator == null) {
            if (other.generator != null)
                return false;
        } else if (!generator.equals(other.generator))
            return false;
        return true;
    }
}
