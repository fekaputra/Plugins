package org.dbpedia.extraction.spark.plus;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import org.dbpedia.extraction.spark.SparkPipelineConfig_V1;
import org.dbpedia.extraction.spark.dialog.Converters;
import org.dbpedia.extraction.spark.dialog.SparkConfigEntry;
import org.dbpedia.extraction.spark.dialog.SparkDpuConfig;
import org.dbpedia.extraction.spark.dialog.Validators;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by chile on 04.04.17.
 */
public class ConfigTests {

    @Test
    public void testConfig() throws Exception {
        SparkPipelineConfig_V1 config = new SparkPipelineConfig_V1();
        SparkDpuConfig dpuConfig = config.getConfig();

        dpuConfig.addItem(dpuConfig.GetDefaultEntry("spark.hadoop.cloneConf"));

        System.out.println(dpuConfig.getItemIds().size());


        dpuConfig.addContainerFilter(new Container.Filter() {
            @Override
            public boolean passesFilter(Object o, Item item) throws UnsupportedOperationException {
                SparkConfigEntry entry = (SparkConfigEntry) o;
                if(entry.getKey().contains("spark."))
                    return true;
                return false;
            }

            @Override
            public boolean appliesToProperty(Object o) {
                return false;
            }
        });


        System.out.println(dpuConfig.getItemIds().size());

        List<String> zws = Arrays.asList("spark.executor.instances", "spark.executor", "spark.executor.inst ances");


        for(SparkConfigEntry zw : dpuConfig.getItemIds())
            try {
                Validators.GetValueValidator(zw).validate(zw.getValue().getValue());
            } catch (Validator.InvalidValueException e) {
                throw new Exception(zw.getKey() + " - " + zw.getValue().getValue(), e);
            }


        System.out.println(Converters.StringToStringListConverter.convertToPresentation(Arrays.asList("en", "de", "fr"), String.class, Locale.getDefault()));
        System.out.println(Converters.StringToStringListConverter.convertToModel("en,de,fr", List.class, Locale.getDefault()));

    }
}
