### Description

Transforms input using a single SPARQL Update query.

It supports [RDF Validation extension](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).

It does not support quads - it is always executed either on top of all input graphs, or, if per-graph execution is checked, successively on each graph.

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
