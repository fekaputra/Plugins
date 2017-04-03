package org.dbpedia.extraction.spark.utils;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chile on 02.04.17.
 */
public class SparkDpuFileManager {

    final private WritableFilesDataUnit output;
    final private FilesDataUnit input;
    final private SparkDpuConfig config;
    final private String configPrefix;
    final private String sparkWorkingDir;

    public SparkDpuFileManager(SparkDpuConfig config, FilesDataUnit input, WritableFilesDataUnit output){
        this.config = config;
        this.input = input;
        this.output = output;
        this.configPrefix = "spark." + this.config.getAppName() + ".";
        this.sparkWorkingDir = this.config.getSparkConfig().get(this.configPrefix + "filemanager.inputdir");
    }

    public void copyToSparkWorkingDir() throws DataUnitException {
        for(FilesDataUnit.Entry file : FilesHelper.getFiles(this.input))
            uploadLocalFile(file.getFileURIString(), this.sparkWorkingDir);
    }

    public List<String> getInputFiles() throws DataUnitException {
        ArrayList<String> ret = new ArrayList<>();

        for(FilesDataUnit.Entry file : FilesHelper.getFiles(this.input))
            ret.add(file.getFileURIString());

        return ret;
    }

    public void copyToOutputDirectory(String uri) throws DataUnitException {
        copyToLocalhoast(uri, this.output.getBaseFileURIString());
    }

    /**
     * Will copy or download a given file to a local directory
     * @param uri - the source uri (somewhere on the web)
     * @param targetDirectory - the local directory
     * @throws DataUnitException
     */
    public void copyToLocalhoast(String uri, String targetDirectory) throws DataUnitException {
        try {
            URI source = new URI(uri);
            String fileName = source.getPath().substring(source.getPath().contains("/") ? source.getPath().lastIndexOf('/') +1 : 0);
            File targetDir = new File(targetDirectory);
            URI target = null;

            if(targetDir.isDirectory())
                target = new URI(targetDir.toString() + "/" + fileName);
            else
                targetDir.toString();

            //copy on localhost -> if SPARK driver is on same machine
            if(source.getScheme().contains("file")){
                FileUtils.copyFile(new File(source), new File(target));
            }
            //fetch source from (s)ftp source
            else if(source.getScheme().contains("ftp")){
                //TODO not implemented!
                throw new DataUnitException("The (s)ftp download operations are not yet implemented.");
            }
            //fetch source from hadoop file system
            else if(source.getScheme().contains("hdfs")){
                //TODO not implemented!
                throw new DataUnitException("The hdfs download operations are not yet implemented.");
            }
            //fetch from the web
            else if(source.getScheme().contains("http")){
                FileUtils.copyURLToFile(source.toURL(), new File(target));
            }
            else
                throw new DataUnitException("This file cannot be transferred to the executing machine, since its uri scheme is not supported: " + source);
        } catch (URISyntaxException | IOException e) {
            throw new DataUnitException(e);
        }

        File file = new File(this.output.getBaseFileURIString().replace("file:", ""), uri.substring(uri.lastIndexOf('/')+1));
        FilesHelper.addFile(this.output, file, file.toString());
    }

    /**
     * Uploads a given file to a desired location on the web using different schemes
     * @param source uri
     * @param target uri
     */
    public void uploadLocalFile(String source, String target) throws DataUnitException {
        try{
            File localFile = new File(source);
            if(!localFile.isFile() || !localFile.toURI().getScheme().contains("file"))
                throw new IllegalArgumentException("The provided source is not a file on the local file system: " + source);

            URI targetUri = new URI(target);

            if(targetUri.getScheme().contains("file")){
                copyToLocalhoast(source, target);
            }
            else{
                //TODO not implemented!
                throw new DataUnitException("Uploading to remote locations is not yet implemented.");
            }
        } catch (URISyntaxException e) {
            throw new DataUnitException(e);
        }
    }
}
