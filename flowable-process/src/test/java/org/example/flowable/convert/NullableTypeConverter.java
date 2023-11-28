package org.example.flowable.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.flowable.common.engine.impl.de.odysseus.el.misc.TypeConverterImpl;

public class NullableTypeConverter extends TypeConverterImpl {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @SneakyThrows
    @Override
    protected String coerceToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String)value;
        }
        if (value instanceof Enum<?>) {
            return ((Enum<?>)value).name();
        }
        return JSON_MAPPER.writeValueAsString(value);
    }
}
