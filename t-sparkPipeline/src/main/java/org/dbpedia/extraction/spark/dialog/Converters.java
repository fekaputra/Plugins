package org.dbpedia.extraction.spark.dialog;

import com.vaadin.data.util.converter.Converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by chile on 03.05.17.
 *
 * This class contains a static selection of all non default vaadin property converters between String and a target type.
 * These are use for display purposes in the Config dialog.
 */
public class Converters {
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
}
