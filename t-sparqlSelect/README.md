# T-SPARQLSelect #
----------

###General###

|                              |                                                               |
|------------------------------|---------------------------------------------------------------|
|**Name:**                     |T-SPARQLSelect                                              |
|**Description:**              |Transforms SPARQL SELECT query result to CSV. Does not validate query. |
|**Status:**                   |Supported in Plugins v2.X. Updated to use Plugin-DevEnv v2.X.       |
|                              |                                                               |
|**DPU class name:**           |SparqlSelect     | 
|**Configuration class name:** |SparqlSelectConfig                           |
|**Dialogue class name:**      |SparqlSelectVaadinDialog | 

***

###Configuration parameters###


|Parameter                        |Description                             |                                                        
|---------------------------------|----------------------------------------|
|**Target path:*** |Path and target CSV file name.  |
|**SPARQL query:**|Text area dedicated for SPARQL SELECT query.  | 

***

### Inputs and outputs ###

|Name                |Type       |DataUnit                         |Description                        |
|--------------------|-----------|---------------------------------|-----------------------------------|
|input  |i |RDFDataUnit  |RDF graph.  |
|output |o |FilesDataUnit  |CSV file containing SPARQL SELECT query result.  |

***

### Version history ###

|Version            |Release notes                                   |
|-------------------|------------------------------------------------|
|1.5.1              |Imported from the repository https://github.com/mff-uk/DPUs.                         |

***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|N/A               |N/A                   | 

