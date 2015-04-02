# T-SparqlUpdate #
----------

###General###

|                              |                                                                              |
|------------------------------|------------------------------------------------------------------------------|
|**Name:**                     |T-SparqlUpdate                                                                |
|**Description:**              |Transform input using SPARQL construct.                                       |
|**Status:**                   |Supported in Plugins v2.X. Updated to use Plugin-DevEnv v2.X.       |
|                              |                                                                              |
|**DPU class name:**           |SparqlUpdate                                                                  | 
|**Configuration class name:** |SparqlUpdate_V1                                                               |
|**Dialogue class name:**      |SparqlUpdateDialog                                                            |

***

###Configuration parameters###

|Parameter                           |Description                                                              |
|------------------------------------|-------------------------------------------------------------------------|
|**Per-graph execution:**            |If checked query is executed per-graph.                                  |
|**SPARQL update query:**            |SPARQL update query.                                                     |

***

### Inputs and outputs ###

|Name           |Type           |DataUnit           |Description                                  |
|---------------|---------------|-------------------|---------------------------------------------|
|input          |i              |RDFDataUnit        |RDF input                                    |
|output         |o              |RDFDataUnit        |RDF output (transformed)                     |

***

### Version history ###

|Version          |Release notes               |
|-----------------|----------------------------|
|2.0.0   |Imported from the repository https://github.com/mff-uk/DPUs, using helpers 2.0.0                         |


***

### Developer's notes ###

|Author           |Notes                           |
|-----------------|--------------------------------|
|N/A              |N/A                             | 
