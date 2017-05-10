package org.dbpedia.extraction.spark.utils;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import org.dbpedia.extraction.spark.dialog.SparkConfigEntry;
import org.dbpedia.extraction.spark.dialog.SparkPropertyCategory;

import java.util.List;

/**
 * Created by chile on 01.04.17.
 */
public class SparkPipelineUtils {

    public static String prettyMilliseconds(Long millis){
        String minutes = String.valueOf(millis / 60000);
        minutes = minutes.length() < 2 ? padLeft(minutes, 2 - minutes.length(), '0') : minutes;
        String seconds = String.valueOf(millis % 60000 / 1000);
        seconds = seconds.length() < 2 ? padLeft(seconds, 2 - seconds.length(), '0'): seconds;
        String ms = String.valueOf(millis % 1000);
        ms = ms.length() < 3 ? padLeft(ms, 3 - ms.length(), '0'): ms;

        return minutes+':'+seconds+'.'+ms+'s';
    }

    public static String padLeft(String s, int n, char c){
        return String.format("%1$" + n + "s", s).replace(' ', c);
    }



    public static Container.Filter getContainerFilter(List<SparkPropertyCategory> categories, String appName){
        return new Container.Filter() {
            @Override
            public boolean passesFilter(Object o, Item item) throws UnsupportedOperationException {
                String possibleUseCase = ((SparkConfigEntry) o).getKey();
                if(possibleUseCase.length() > 7 && possibleUseCase.indexOf('.', 6) >= 0)
                    possibleUseCase = possibleUseCase.substring(6, possibleUseCase.indexOf('.', 6));
                boolean categoryTest = categories.contains(((SparkConfigEntry) o).getSparkPropertyCategory());
                if(appName != null && !appName.trim().isEmpty())
                    return categoryTest && appName.trim().equals(possibleUseCase.trim());
                else
                    return categoryTest;
            }

            @Override
            public boolean appliesToProperty(Object o) {
                return false; //TODO
            }
        };
    }
}
