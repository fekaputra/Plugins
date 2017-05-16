package org.dbpedia.extraction.spark.plus;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import org.dbpedia.extraction.spark.dialog.*;
import org.dbpedia.extraction.spark.utils.SparkDpuFileManager;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by chile on 04.04.17.
 */
public class ConfigTests {

    @Test
    public void testConfig() throws Exception {

        SparkPipelineVaadinDialog dia = new SparkPipelineVaadinDialog();

        SparkDpuFileManager fm = new SparkDpuFileManager(dia.getConfiguration().getSparkConfig(), null, null);

        fm.copyToLocalhost("file:/home/chile/unifiedviews/backend/working/exec_35/storage/dpu_10/0/inputsToSp4503854928179886128", "file:/home/chile/unifiedviews");


        System.out.println(dia.getConfiguration().getSparkConfig().getItemIds().size());

        SparkConfigEntry ent = dia.getConfiguration().getSparkConfig().getDefaultEntry("spark.usecase.filemanager.outputdir");
        System.out.println(new SparkConfigEntry("spark.lala.filemanager.outputdir", "", ent).getSparkPropertyCategory());

        dia.getConfiguration().getSparkConfig().addContainerFilter(new Container.Filter() {
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


        System.out.println(dia.getConfiguration().getSparkConfig().getItemIds().size());

        List<String> zws = Arrays.asList("spark.executor.instances", "spark.executor", "spark.executor.inst ances");


        for(SparkConfigEntry zw : dia.getConfiguration().getSparkConfig().getItemIds())
            try {
                Validators.GetValueValidator(zw).validate(zw.getValue().getValue());
            } catch (Validator.InvalidValueException e) {
                throw new Exception(zw.getKey() + " - " + zw.getValue().getValue(), e);
            }


        System.out.println(Converters.StringToStringListConverter.convertToPresentation(Arrays.asList("en", "de", "fr"), String.class, Locale.getDefault()));
        System.out.println(Converters.StringToStringListConverter.convertToModel("en,de,fr", List.class, Locale.getDefault()));

    }
}
