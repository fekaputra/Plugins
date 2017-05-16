package org.dbpedia.extraction.spark.utils;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import org.apache.commons.io.FileUtils;
import org.dbpedia.extraction.spark.dialog.SparkDpuConfig;

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
        this.sparkWorkingDir = this.config.getByStringKey(this.configPrefix + "filemanager.inputdir").getValue().toString();
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

    public void copyDirectoryToOutputDirectory(String uri) throws DataUnitException {
        File dir = new File(uri.replaceAll("file:(/)+", "/"));
        String pattern = this.config.getByStringKey("spark." + this.config.getAppName() + ".filemanager.filePattern").toString();
        pattern = pattern.substring(0, pattern.indexOf("$key") >= 0 ? pattern.indexOf("$key") : pattern.length());
        if(dir.isDirectory()) {
            for(File file : dir.listFiles())
                if(file.isFile() && file.toString().contains(pattern))
                    copyToOutputDirectory(file.toURI().toString());
        }
        else
            throw new IllegalArgumentException("The provided uri is not a local directory: " + uri);
    }

    public void copyToOutputDirectory(String uri) throws DataUnitException {
        copyToLocalhost(uri, this.output.getBaseFileURIString());
    }

    /**
     * Will copy or download a given file to a local directory
     * @param uri - the source uri
     * @param targetDirectory - the local directory
     * @throws DataUnitException
     */
    public void copyToLocalhost(String uri, String targetDirectory) throws DataUnitException {
        File result = null;
        try {
            URI source = new URI(uri);
            String fileName = source.getPath().substring(source.getPath().contains("/") ? source.getPath().lastIndexOf('/') +1 : 0);
            File targetDir = new File(removeScheme(targetDirectory));
            URI target = null;

            if(targetDir.isDirectory())
                target = new URI("file:" + targetDir.toString() + "/" + fileName);
            else
                target = new URI("file:" + targetDir.toString());

            //copy on localhost -> if SPARK driver is on same machine
            if(source.getScheme().contains("file")){
                //TODO ask Tomas if just providing a location on localhost is enough here (otherwise copy files around)
                FileUtils.copyFile(new File(source), new File(target));
                result = new File(target.getPath());
            }
            //fetch source from (s)ftp source
            else if(source.getScheme().contains("ftp")){
                throw new DataUnitException("The (s)ftp download operations are not yet implemented.");
            }
            //fetch source from hadoop file system
            else if(source.getScheme().contains("hdfs")){
                //TODO So that we can copy data from hdfs on the remote server with spark master
                throw new DataUnitException("The hdfs download operations are not yet implemented.");
            }
            //fetch from the web
            else if(source.getScheme().contains("http")){
                result = new File(target);
                FileUtils.copyURLToFile(source.toURL(), result);
            }
            else
                throw new DataUnitException("This file cannot be transferred to the executing machine, since its uri scheme is not supported: " + source);
        } catch (URISyntaxException | IOException e) {
            throw new DataUnitException(e);
        }

        //new File(this.output.getBaseFileURIString().replace("file:", ""), uri.substring(uri.lastIndexOf('/')+1));
        //TODO This should be called for the resulting files - but it is called also when files are copied to the remote master, which is incorrect
        FilesHelper.addFile(this.output, result, result.toString());
    }

    /**
     * Uploads a given file to a desired location on the web using different schemes
     * @param source uri
     * @param target uri
     */
    public void uploadLocalFile(String source, String target) throws DataUnitException {
        try{
            File localFile = new File(source);
            if(!localFile.toURI().getScheme().contains("file"))
                throw new IllegalArgumentException("The provided source is not a file on the local file system: " + source);

            URI targetUri = new URI(target);

            if(targetUri.getScheme().contains("file")){
                copyToLocalhost(source, target);
            }
            else{
                //TODO not implemented!
                throw new DataUnitException("Uploading to remote locations is not yet implemented.");
            }
        } catch (URISyntaxException e) {
            throw new DataUnitException(e);
        }
    }

    private String removeScheme(String uri){
        return uri.replaceAll("^\\w+:(/)+", "/");
    }
}
