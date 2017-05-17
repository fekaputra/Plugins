### Description

Downloads one or more files from the defined locations. The files to be downloaded may be located at HTTP URLs, on the local filesystem, at certain SFTP/FTP servers, etc.

Individual files and also whole directories may be downloaded. If directory is provided then all files and files in subdirectories are extracted.

If internal name (file name) is specified for the downloaded entry, such name is then used as a symbolic name to internally identify the given file further on the pipeline.
If you specify a directory as an entry then such file name is used as a prefix for the individual files within that directory.
If you do not care about the internal name of the file, e.g., in cases where you just need to iterate over downloaded files later on process every downloaded file in the same way,
you do not need to specify a file name.

This DPU also sets virtual path metadata for each file extracted. In case of files it is equal to the file name (local file name from the file path, e.g. example.txt from a/b/c/example.txt).
In case of directories, virtual path metadata for each extracted file is equal to the relative path to the original directory.


### Configuration parameters

| Name | Description |
|:----|:----|
|**List of files and directories to download** | List of files and directories to be downloaded. Each entry contains location from which the file should be optained and optionally the internal file name.  |
|**Soft failure** | In case the soft failure is checked in the configuration dialog, when there is a problem processing certain VFS entry or file, warning is shown but the execution of the DPU continues. If unchecked (default), in case of problem processing any VFS entry/file, the execution fails.  |
|**Skip redundant input file entries** | If checked, the DPU checks whether it is not trying to process certain file URIs more times (this may happen when the DPU is configured dynamically). If yes, it just skips processing of redundant entries and logs info message.  |
|**Wait between calls for (ms)**| Number of milliseconds the DPU should wait between the HTTP calls (0 by default, thus no delays between calls) |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|output |o| FilesDataUnit | Downloaded files |x|
|config |i| RdfDataUnit | Dynamic DPU configuration, see Advanced configuration | |

### Advanced configuration

It is also possible to dynamically configure the DPU over its input `config` data unit using RDF data.

Configuration samples:

```turtle
<http://localhost/resource/config> 
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/filesDownload/Config>;
    <http://unifiedviews.eu/ontology/dpu/filesDownload/hasFile> <http://localhost/resource/file/0>.
```

```turtle
<http://localhost/resource/file/0>
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/filesDownload/File>;
    <http://unifiedviews.eu/ontology/dpu/filesDownload/file/uri> "http://www.zmluvy.gov.sk/data/att/117597_dokument.pdf";
    <http://unifiedviews.eu/ontology/dpu/filesDownload/file/fileName> "zmluva.pdf".`
```
