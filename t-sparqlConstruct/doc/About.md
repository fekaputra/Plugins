### Description

Transforms input using SPARQL Construct query provided. The result of the SPARQL Construct - the created triples - is stored to the output. 

Note: Internally, the query is translated to SPARQL Update query before it is executed.

It supports [RDF Validation extension](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).

### Configuration parameters

| Name | Description |
|:----|:----|
|**Per-graph execution** | If checked query is executed per-graph |
|**SPARQL construct query** | SPARQL construct query |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input|i|RDFDataUnit|RDF input|x|
|output|o|RDFDataUnit|transformed RDF output|x|
