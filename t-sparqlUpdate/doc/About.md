### Description

Transforms input using SPARQL construct. It supports [RDF Validation extension](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).

### Configuration parameters

| Name | Description |
|:----|:----|
|**Per-graph execution** | If checked query is executed per-graph |
|**SPARQL update query** | SPARQL update query |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input  |i | RDFDataUnit | RDF input |x|
|output |o | RDFDataUnit | RDF output (transformed) |x|
