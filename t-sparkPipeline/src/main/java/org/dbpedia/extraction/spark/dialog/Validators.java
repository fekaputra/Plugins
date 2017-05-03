package org.dbpedia.extraction.spark.dialog;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chile on 26.04.17.
 */
public class Validators {

    private static Pattern sparkKeyPattern = Pattern.compile("^spark(\\.[a-zA-Z0-9-]+)+$");
    public static Validator SparkKeyValidator = (Validator) value -> {
        try {
            GetRegexValidator(sparkKeyPattern).validate(value);
        } catch (Validator.InvalidValueException e) {
            throw new Validator.InvalidValueException(value.toString() + " is an invalid key. Spark property keys have to match this regular expression: " + sparkKeyPattern);
        }
    };

    public static Validator GetUseCaseKeyValidator(final String useCase) {
        return (Validator) value -> {
            try {
                GetRegexValidator(sparkKeyPattern).validate(value);
            } catch (Validator.InvalidValueException e) {
                throw new Validator.InvalidValueException(value.toString() + " is an invalid key. Spark property keys have to match this regular expression: " + sparkKeyPattern);
            }
            int dotInex = value.toString().indexOf(6, '.');
            if (dotInex <= 6 || !useCase.equals(value.toString().substring(6, dotInex))) //not!
                throw new Validator.InvalidValueException("Every use case specific property key has to start with 'spark." + useCase + "'.");
        };
    }

    public static Validator IntegerValidator = (Validator) value -> {
        try {
            Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new Validator.InvalidValueException(e.getMessage());
        }
    };

    public static Validator PositiveIntegerValidator = (Validator) value -> {
        try {
            int zw = Integer.parseInt(value.toString());
            if(zw < 0)
                throw new Validator.InvalidValueException("This value has to be a positive integer.");
        } catch (NumberFormatException e) {
            throw new Validator.InvalidValueException(e.getMessage());
        }
    };

    /**
     * Provides a Float Validator without a specific interval
     * @return
     */
    public static Validator GetFloatValidator(){
        return GetFloatValidator(Float.MIN_VALUE, Float.MAX_VALUE);
    }

    /**
     * Providing the possibility to define inclusive/exclusive intervals to test a float value against
     * (this function is only needed if at least one threshold is exclusive...)
     * @param min
     * @param exclusiveMin - true = the min value is exclusive
     * @param max
     * @param exclusiveMax - true = the max value is exclusive
     * @return
     */
    public static Validator GetFloatValidator(float min, boolean exclusiveMin, float max, boolean exclusiveMax){
        if(exclusiveMax)
            max = Math.nextDown(max);
        if(exclusiveMin)
            min = Math.nextUp(min);
        return GetFloatValidator(min, max);
    }

    /**
     * Provide a validator which does not only test a given string for a valid Float representation, but also validates against a given interval (inclusive)
     * @param min
     * @param max
     * @return
     */
    public static Validator GetFloatValidator(float min, float max){
        return (Validator) value -> {
            try {
                float zw = Float.parseFloat(value.toString());
                if(zw < min)
                    throw new Validator.InvalidValueException("The given value is below the defined minimum of " + min);
                if(zw > max)
                    throw new Validator.InvalidValueException("The given value is above the defined maximum of " + max);
            } catch (NumberFormatException e) {
                throw new Validator.InvalidValueException(e.getMessage());
            }
        };
    }

    public static Validator BooleanValidator = (Validator) value -> {
        try {
            Boolean.parseBoolean(value.toString());
        } catch (NumberFormatException e) {
            throw new Validator.InvalidValueException(e.getMessage());
        }
    };

    private static Pattern versionNumberPattern = Pattern.compile("^\\d+(\\.\\d)+$");
    public static Validator VersionNumberValidator = GetRegexValidator(versionNumberPattern);

    private static Pattern appNamePattern = Pattern.compile("^[a-z0-9-]$");
    public static Validator AppNameValidator = GetRegexValidator(appNamePattern);

    public static Validator GetUriValidator(final List<String> schemes) {
        final List<String> scheeme = schemes == null || schemes.isEmpty() ? new ArrayList<>() : schemes;
        return (Validator) value -> {
            if(value == null)
                throw new Validator.InvalidValueException("Please provide an URI.");
            try {
                URI uri = (URI) value;
                boolean passSchemeTest = scheeme.isEmpty();

                for(String sch : scheeme){
                    if(uri.getScheme() == null || uri.getScheme().equals(sch.trim()))   //TODO what is if scheme == null??
                        passSchemeTest = true;
                }
                if(!passSchemeTest)
                    throw new Validator.InvalidValueException("The scheme of this uri does not match one of the expected schemes: " + scheeme);
            } catch (ClassCastException e) {
                throw new Validator.InvalidValueException(e.getMessage());
            }
        };
    }

    private static Pattern byteSizePattern = Pattern.compile("^\\d+(b|k|kb|m|mb|g|gb|t|tb|p|pb)$");
    public static Validator ByteSizeValidator = (Validator) value -> {
        try {
            //lowercase the value, since 1g and 1G is equal
            GetRegexValidator(byteSizePattern).validate(value.toString().toLowerCase());
        } catch (Validator.InvalidValueException e) {
            throw new Validator.InvalidValueException(value.toString() + " is not a valid representation of a byte size. (e.g. 2b, 23kb, 3m, 1G, 1gb, 5t are valid ones)");
        }
    };

    private static Pattern durationPattern = Pattern.compile("^\\d+(ms|s|m|min|h|d|y)$");
    public static Validator DurationValidator = (Validator) value -> {
        try {
            //lowercase the value, since 1h and 1H is equal
            GetRegexValidator(durationPattern).validate(value.toString().toLowerCase());
        } catch (Validator.InvalidValueException e) {
            throw new Validator.InvalidValueException(value.toString() + " is not a valid representation of a time duration. (e.g. 5s, 1H, 1h, 334ms, 3y are valid ones)");
        }
    };

    public static Validator GetRegexValidator(Pattern pattern) throws Validator.InvalidValueException{
        return (Validator) value -> {
            Matcher match = pattern.matcher(value.toString().toLowerCase());
            if(!match.find())
                throw new Validator.InvalidValueException("The tested value does not match the given regex pattern: " + pattern.toString());
        };
    }


    public static Validator GetStringListRegexValidator(Pattern pattern) throws Validator.InvalidValueException{
        return (Validator) value -> {
            List<String> cells = Arrays.asList(value.toString().split(","));
            boolean failed = cells.isEmpty();
            int count = 0;
            for(String cell: cells){
                Matcher matcher = pattern.matcher(cell.trim());
                if(!matcher.find()) {
                    failed = true;
                    break;
                }
                count++;
            }
            if(failed)
                throw new Validator.InvalidValueException("The comma separated List did not match the given regex for every item: see item " + count);
        };
    }

    public static Validator GetValueValidator(SparkConfigEntry id){
        Validator val = null;
        if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.Float)
            val = GetFloatValidator(id.getFloatMin(), id.getFloatMax());
        else if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.Uri)
            val = GetUriValidator(id.getUriSchemes());
        else if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.Boolean)
            val = Validators.BooleanValidator;
        else if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.ByteSize)
            val = Validators.ByteSizeValidator;
        else if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.Duration)
            val = Validators.DurationValidator;
        else if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.Enum)
            val = Validators.GetRegexValidator(id.getRegex());
        else if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.Integer)
            val = Validators.IntegerValidator;
        else if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.NonNegativeInteger)
            val = Validators.PositiveIntegerValidator;
        else if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.String) {
            if (id.getRegex() != null)
                val = Validators.GetRegexValidator(id.getRegex());
        }
        else if (id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.StringList) {
            if (id.getRegex() != null)
                val = Validators.GetStringListRegexValidator(id.getRegex());
        }

        if(val != null){
            final Validator validator = val;
            return new Validator() {
                @Override
                public void validate(Object value) throws InvalidValueException {
                    if(value == null)
                        return;             //TODO check this behaviour!
                    Object obj = value.getClass().isAssignableFrom(Property.class) ? ((Property) value).getValue() : value;
                    if(obj == null)
                        return;             //TODO check this behaviour!

                    try {
                        validator.validate(obj);
                    } catch (InvalidValueException e) {
                        throw new Validator.InvalidValueException("value '" + obj + "' caused an exception for property " + id.getKey() + ": " + e.getMessage());
                    }
                }
            };
        }
        else
            return null;
    }
}
