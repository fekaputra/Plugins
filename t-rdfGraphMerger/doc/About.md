### Description

Merges RDF input graphs into a single new RDF output graph (all triples from input graphs are put into a single new output graph)

### Configuration parameters

| Name | Description |
|:----|:----|
|Output graph name| Name (URI) of graph on output. This name is used in other DPUs (for example when loading RDF graph to external storage as a destination graph name) |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input  |i| RDFDataUnit | DataUnit to which user connects all inputs which has to be merged |x|
|output |o| RDFDataUnit | DataUnit which outputs RDF graph, which contains all triples from input graphs | |
