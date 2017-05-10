package org.dbpedia.extraction.spark.dialog;

import com.vaadin.data.util.converter.Converter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chile on 03.05.17.
 *
 * This class contains a static selection of all non default vaadin property converters between String and a target type.
 * These are use for display purposes in the Config dialog.
 */
public class Converters {


    public static <T> Converter<String, T> getConverter(Class<T> tType){
        return (Converter<String, T>) valueConverters.get(tType);
    }

    /**
     * We reimplement a Locale independent representation of this converter
     */
    public static Converter<String, Integer> StringToIntegerConverter = new Converter<String, Integer>() {
        @Override
        public Integer convertToModel(String value, Class<? extends Integer> targetType, Locale locale) throws ConversionException {
            if(value == null || value.trim().length() == 0)
                return null;
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        public String convertToPresentation(Integer value, Class<? extends String> targetType, Locale locale) throws ConversionException {
            if(value == null)
                return "";
            return value.toString();
        }

        @Override
        public Class<Integer> getModelType() {
            return Integer.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    };

    public static Converter<String, List> StringToStringListConverter = new Converter<String, List>(){
        @Override
        public List convertToModel(String value, Class<? extends List> targetType, Locale locale) throws ConversionException {
            if(value == null || value.trim().length() == 0)
                return new ArrayList();
            return Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toList());
        }

        @Override
        public String convertToPresentation(List value, Class<? extends String> targetType, Locale locale) throws ConversionException {
            if(value == null || value.size() == 0)
                return "";
            return value.stream().reduce(null, (o, o2) -> (o == null ? "" : (o + ",")) + o2).toString();
        }

        @Override
        public Class<List> getModelType() {
            return List.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    };

    public static Converter<String, URI> StringToUriConverter = new Converter<String, URI>() {
        @Override
        public URI convertToModel(String value, Class<? extends URI> targetType, Locale locale) throws ConversionException {
            if(value == null)
                return null;
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        public String convertToPresentation(URI value, Class<? extends String> targetType, Locale locale) throws ConversionException {
            if(value == null)
                return "";
            return value.toString();
        }

        @Override
        public Class<URI> getModelType() {
            return URI.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    };


    private static Map<Class<?>, Converter<String, ? extends Object>> valueConverters = new HashMap<>();

    static{
        for(Field field : Converters.class.getDeclaredFields()){
            if(field.getType().isAssignableFrom(Converter.class)) {
                //get first type parameter of Converter
                Class<?> firstTypeParam = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (firstTypeParam.isAssignableFrom(String.class)){
                    Class<?> secondTypeParam = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                    try {
                        valueConverters.put(secondTypeParam, (Converter<String, ?>) field.get(null));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
