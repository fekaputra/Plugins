# T-Tabular #
----------

###General###

|                              |                                                                             |
|------------------------------|-----------------------------------------------------------------------------|
|**Name:**                     |T-Tabular                                                               |
|**Description:**              |Converts tabular data into RDF data. |
|                              |                                                                             |
|**DPU class name:**           |Tabular                                                                 | 
|**Configuration class name:** |TabularConfig_V1                             |
|**Dialogue class name:**      |TabularVaadinDialog                      |

***

###Configuration parameters###

|Parameter                                       |Description                                                              |                                                        
|------------------------------------------------|-------------------------------------------------------------------------|


***

### Inputs and outputs ###

|Name         |Type           |DataUnit     |Description             |
|-------------|---------------|-------------|------------------------|
|table        |i              |FilesDataUnit|Input file containing tabular data. |  
|triplifiedTable  |o          |RDFDataUnit  |RDF data. |

***

### Version history ###

|Version          |Release notes               |
|-----------------|----------------------------|
|2.1.1 | Fixed bug with :Skip n first lines: for XLS, where empty text box makes configuration invalid. |
|2.1.0 | Added option "Generate labels". |
|2.0.2 | Fixed bug with wrong initial column. First column wrongly named as "col2" instead of "col1". |
|2.0.1 | fixes in build dependencies |
|2.0.0            |Replaced with the DPU taken from the repository https://github.com/mff-uk/DPUs.|
|1.5.0            |N/A                         |
|1.4.0            |N/A                         |
|1.3.1            |N/A                         |
|1.0.0            |N/A                         |

***

### Developer's notes ###

|Author           |Notes                           |
|-----------------|--------------------------------|
|N/A              |N/A                             | 

***

### Runtime dependencies ###
t-Tabular requires runtime dependencies that must be presented in module/lib folder of UnifiedViews prior
the t-Tabular usage. Those dependencies can be found in CUNI repository (https://github.com/mff-uk/DPUs).

|Library          |
|-----------------|
|https://github.com/mff-uk/DPUs/blob/master/dpu/t-tabular/libs/org.apache.poi-bundle-3.10.0.jar |
|https://github.com/mff-uk/DPUs/blob/master/dependencies/org.junit-bundle-4.11.0.jar |


