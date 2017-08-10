### Description

Loads RDF data serialized as files into Virtuoso RDF store. Uses Virtuoso internal functions to load directory of RDF data.

This DPU has no input data unit. The input for this DPU (the RDF data serialized in a file) has to be specified in the configuration of this DPU as a directory on the target file system where the target Virtuoso is. All files in this directory must be in the formats Virtuoso supports - the recommended formats are either `application/rdf+xml` or `text/turtle` (filenames suffix `.rdf` or `.ttl` respectively). Please make sure that Virtuoso can load data from such directory, i.e., the directory to be loaded is among 'DirsAllowed' option in virtuoso.ini.

Pipeline designer has to move these RDF files to be loaded into that directory before the loader is executed. This can be achieved by using l-filesUpload with special ‘run after' edge to this DPU. This will make sure that loader will run only when the files have already been copied to the target directory. 

Sample pipeline design:

    --------------------      ----------------      -----------------                     ---------------------
    |                  |      |              |      |               |                     |                   |
    | e-sparqlEndpoint | ---> | t-rdfToFiles | ---> | l-filesUpload | --run after edge--> | l-FilesToVirtuoso |
    |                  |      |              |      |               |                     |                   |
    --------------------      ----------------      -----------------                     ---------------------

### Configuration parameters

| Name | Description |
|:----|:----|
|**Virtuoso JDBC URL** | URL for establishing JDBC session with the target Virtuoso server. If remote machine is specified, please make sure that the given Virtuoso port (typically 1111) is open. |
|**Username** | Username for Virtuoso server |
|**Password** | Password for the username |
|**Clear destination graph before loading (checkbox)** | Self-descriptive |
|**Directory to be loaded** | Path to the directory to be loaded. Please make sure that Virtuoso can load data from such directory, i.e., the directory to be loaded is among DirsAllowed option in virtuoso.ini. |
|**Include subdirectories (checkbox)** | If checked, subdirectories are also loaded to Virtuoso |
|**File name pattern** | A pattern for file names to be included according to SQL standard (characters string is symbolized as ‘%’). For example ‘%.ttl’ |
|**Target Graph** | Target graph URI |
|**Update status interval (s)** | Time period between status updates (in seconds) |
|**Thread count** | Number of concurrently running processes |
|**Skip file on error (checkbox)** | Do not stop the pipeline when error occurs (if checked) |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|config |i| RdfDataUnit | Dynamic DPU configuration, see Advanced configuration | &nbsp; |
|rdfOutput |o| RDFDataUnit | Metadata about the RDF data loaded to Virtuoso | &nbsp; |


### Advanced configuration

It is also possible to dynamically configure the DPU over its input `config` data unit using RDF data.

Configuration samples:

```turtle
<http://localhost/resource/config	
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/filesToVirtuoso/Config>;
    <http://unifiedviews.eu/ontology/dpu/filesToVirtuoso/config/fileName> "dataset.trig";
    <http://unifiedviews.eu/ontology/dpu/filesToVirtuoso/config/graphUri> "http://dataset".
```
