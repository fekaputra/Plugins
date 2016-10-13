### Description

Downloads one or more files from the defined locations. The files to be downloaded may be located at HTTP URLs, on the local filesystem, at certain SFTP/FTP servers, etc.

If internal file name is specified for the downloaded entry, such file name is then used as a symbolic name to internally identify the given file further on the pipeline and also such value is used as the value of 
VirtualPath (target location of the file when loaded outside of UnifiedViews at the end of pipeline). If you do not care about the internal name of the file or value of VirtualPath, e.g., 
in cases where you just need to iterate over downloaded files later on process every downloaded file in the same way, you do not need to specify a file name. 

Individual files and also whole directories may be downloaded. If directory is provided then all files and files in subdirectories are extracted.

### Configuration parameters

| Name | Description |
|:----|:----|
|**List of files and directories to download** | List of files and directories to be downloaded. Each entry contains location from which the file should be optained and optionally the internal file name.  |
|**Soft failure** | In case the soft failure is checked in the configuration dialog, when there is a problem processing certain VFS entry or file, warning is shown but the execution of the DPU continues. If unchecked (default), in case of problem processing any VFS entry/file, the execution fails.  |
|**Skip redundant input file entries** | If checked, the DPU checks whether it is not trying to process certain file URIs more times (this may happen when the DPU is configured dynamically). If yes, it just skips processing of redundant entries and logs info message.  |

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
