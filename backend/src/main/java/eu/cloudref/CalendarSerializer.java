package eu.cloudref;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarSerializer extends StdSerializer<Calendar> {

    // date format used at frontend
    private static final DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - HH:mm");

    public static DateFormat getDateFormat() {
        return dateFormat;
    }

    public CalendarSerializer() {
        this(null);
    }

    protected CalendarSerializer(Class<Calendar> t) {
        super(t);
    }

    @Override
    public void serialize(Calendar value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(dateFormat.format(value.getTime()));
    }
}
