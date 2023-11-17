package org.example.support.converter;

import org.flowable.common.engine.impl.de.odysseus.el.misc.TypeConverterImpl;

/**
 * @author renc
 */
public class NullableTypeConverter extends TypeConverterImpl {

    @Override
    protected String coerceToString(Object value) {
        if (value == null) {
            return null;
        }
        return super.coerceToString(value);
    }
}
