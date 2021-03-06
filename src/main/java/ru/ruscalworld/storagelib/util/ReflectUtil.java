package ru.ruscalworld.storagelib.util;

import ru.ruscalworld.storagelib.Converter;
import ru.ruscalworld.storagelib.ConverterProvider;
import ru.ruscalworld.storagelib.DefaultModel;
import ru.ruscalworld.storagelib.annotations.DefaultGenerated;
import ru.ruscalworld.storagelib.annotations.Model;
import ru.ruscalworld.storagelib.annotations.Property;
import ru.ruscalworld.storagelib.exceptions.InvalidModelException;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

public class ReflectUtil {

    /**
     * Retrieves list of declared fields annotated with {@link Property} in given class.
     * @param clazz Class to get fields from
     * @return Map where key is field name and value is a field instance
     */
    public static <T> HashMap<String, Field> getClassFields(Class<T> clazz) {
        return getClassFields(clazz, false);
    }

    /**
     * Retrieves list of declared fields annotated with {@link Property} in given class.
     * @param clazz Class to get fields from
     * @param excludeDefaultGenerated If true, only fields not annotated with {@link DefaultGenerated} will be returned
     * @return Map where key is field name and value is a field instance
     */
    public static <T> HashMap<String, Field> getClassFields(Class<T> clazz, boolean excludeDefaultGenerated) {
        final HashMap<String, Field> fields = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Property.class)) continue;
            if (excludeDefaultGenerated && field.isAnnotationPresent(DefaultGenerated.class)) continue;
            Property property = field.getAnnotation(Property.class);
            fields.put(property.column(), field);
        }

        Class<? super T> superclass = clazz.getSuperclass();
        if (superclass != null) fields.putAll(getClassFields(superclass));

        return fields;
    }

    /**
     * Converts given value to the correct type and updates field's value
     * @param instance Object that should be updated
     * @param field Field of instance to update
     * @param value Value that should be set to the field
     */
    public static void setFieldValue(Object instance, Field field, Object value, ConverterProvider provider) throws IllegalAccessException, IllegalArgumentException {
        Converter<?> converter = provider.getConverter(field.getType());
        if (converter != null) field.set(instance, converter.convert(value));
        else field.set(instance, value);
    }

    /**
     * Checks if given class is annotated with {@link Model} and returns info provided with this annotation
     * @param clazz Class to check
     * @throws InvalidModelException if given class is not a valid model
     * @return Model info
     */
    public static Model getModelInfo(Class<?> clazz) throws InvalidModelException {
        if (!clazz.isAnnotationPresent(Model.class)) throw new InvalidModelException(clazz);
        return clazz.getAnnotation(Model.class);
    }
}
