package org.dbpedia.extraction.spark.dialog;

/**
 * Created by chile on 10.05.17.
 */
public enum SparkPropertyCategory {
    SparkMandatory,
    SparkRecommended,
    SparkOptional,
    UsecaseMandatory,
    UsecaseRecommended,
    UsecaseOptional;

    @Override
    public String toString(){
        return "spark." + super.toString();
    }
}