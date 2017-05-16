### Description

Description of the DPU intended for the user of the DPU. 

### Configuration parameters

| Name | Description |
|:----|:----|
|**Param** | Param which ...|

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input |i |FilesDataUnit |Input files |x|
|output |o |RdfDataUnit |Produced RDF data |x|

### Limitations

* SparkDpuFileManager - copying of files to remote server (currently it is expected master runs on the same server as UV, which is not the case in BDE project)
* SparkDpuFileManager - copying the finished files (from remote server) to the output data unit

So UV must run on the same machine as SPARK master (so that file may be copied to the master working space
and we can point to the resulting file and add it to the output data unit directly)
*
