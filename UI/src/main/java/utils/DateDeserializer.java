package utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;

public class DateDeserializer implements JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return new Date(json.getAsJsonPrimitive().getAsLong());
        } catch (NumberFormatException e) {
            throw new JsonParseException("No se pudo convertir a Date: " + json.getAsString(), e);
        }
    }
}

