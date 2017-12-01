package eu.cloudref;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class CalendarDeserializer extends StdDeserializer<Calendar> {

    public CalendarDeserializer() {
        this(null);
    }

    protected CalendarDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Calendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        DateFormat dateFormat = CalendarSerializer.getDateFormat();

        // get date from JSON
        JsonNode node = p.getCodec().readTree(p);
        String date = node.asText();

        if (date != null) {
            Calendar cal = Calendar.getInstance();
            try {
                // format date information
                cal.setTime(dateFormat.parse(date));
                return cal;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
