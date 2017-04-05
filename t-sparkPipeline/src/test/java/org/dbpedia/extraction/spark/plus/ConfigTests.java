package org.dbpedia.extraction.spark.plus;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import org.dbpedia.extraction.spark.SparkPipelineConfig_V1;
import org.dbpedia.extraction.spark.utils.SparkDpuConfig;
import org.junit.Test;

/**
 * Created by chile on 04.04.17.
 */
public class ConfigTests {

    @Test
    public void testConfig() throws Exception {
        SparkPipelineConfig_V1 config = new SparkPipelineConfig_V1();
        SparkDpuConfig dpuConfig = config.getConfig();

        SparkDpuConfig.SparkConfigEntry entry = new SparkDpuConfig.SparkConfigEntry("test.property", "this is a random value!");
        dpuConfig.addItem(entry);

        System.out.println(dpuConfig.getItemIds().size());


        dpuConfig.addContainerFilter(new Container.Filter() {
            @Override
            public boolean passesFilter(Object o, Item item) throws UnsupportedOperationException {
                SparkDpuConfig.SparkConfigEntry entry = (SparkDpuConfig.SparkConfigEntry) o;
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
    }
}
