# T-RdfToFiles #
----------

###General###

|                              |                                                               |
|------------------------------|---------------------------------------------------------------|
|**Name:**                     |T-RdfToFiles                                              |
|**Description:**              |Transforms RDF graphs into files. |
|**Status:**                   |Supported in Plugins v2.X. Updated to use Plugin-DevEnv v2.X.       |
|                              |                                                               |
|**DPU class name:**           |RdfToFiles     | 
|**Configuration class name:** |RdfToFilesConfig_V1                           |
|**Dialogue class name:**      |RdfToFilesVaadinDialog | 

***

###Configuration parameters###


|Parameter                        |Description                             |                                                        
|---------------------------------|----------------------------------------|
|**RDF format (list):** |Output file format:<BR>- Turtle<BR>- RDF/XML<BR>- N-Triples<BR>- N3<BR>- RDFa  |
|**Generate graph file (checkbox):** |Is graph file desired? |
|**Output graph name:** |Name of the output graph (if graph file is desired).  |
|**File path/name without extension:**|self-descriptive |

***

### Inputs and outputs ###

|Name                |Type       |DataUnit                         |Description                        |
|--------------------|-----------|---------------------------------|-----------------------------------|
|input |i |RDFDataUnit  |RDF graph.  |
|output|o |FilesDataUnit |File containing RDF triples.  |

***

### Version history ###

|Version            |Release notes                                   |
|-------------------|------------------------------------------------|
|2.0.0              |Update for helpers 2.0.0, added statement-based progress log.|
|1.6.0              |fixed per graph select                          |                           
|1.5.0              |N/A                                             |
|1.3.1              |N/A                                             |                                

***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|N/A               |N/A                   | 

