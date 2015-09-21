### Description

Transforms RDF graphs into files. It transforms all graphs within the input RDF data unit to one file. 

### Configuration parameters

| Name | Description |
|:----|:----|
|**RDF format (list)** | Output file format:<BR>- Turtle<BR>- RDF/XML<BR>- N-Triples<BR>- N3<BR>- RDFa |
|**Generate graph file (checkbox)** | Is graph file desired? |
|**Output graph name** | Name of the output graph (if graph file is desired) |
|**File path/name without extension** | The path/name of the create file containing all RDF data |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input  |i| RDFDataUnit   | RDF graph |x|
|output |o| FilesDataUnit | File containing RDF triples |x|
|config |i| RdfDataUnit | Dynamic DPU configuration, see Advanced configuration | |

### Advanced configuration

It is also possible to dynamically configure the DPU over its input `config` using RDF data.

Configuration samples:

```turtle
<http://localhost/resources/configuration>
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/rdfToFiles/Config>;
    <http://unifiedviews.eu/ontology/dpu/rdfToFiles/config/fileFormat> "TriG";
    <http://unifiedviews.eu/ontology/dpu/rdfToFiles/config/outputUri> "http://output-graph/name";
    <http://unifiedviews.eu/ontology/dpu/rdfToFiles/config/outputFile> "graph-output-file".
```
