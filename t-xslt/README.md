﻿# T-Xslt #
----------

###General###

|                              |                                                               |
|------------------------------|---------------------------------------------------------------|
|**Name:**                     |T-Xslt                                              |
|**Description:**              |Does XSL Transformation over files and outputs Files |
|**Status:**                   |Supported in Plugins v2.X. Updated to use Plugin-DevEnv v2.X.       |
|                              |                                                               |
|**DPU class name:**           |Xslt     | 
|**Configuration class name:** |XsltConfig_V1                           |
|**Dialogue class name:**      |XsltVaadinDialog | 

***

###Configuration parameters###


|Parameter                        |Description                             |                                                        
|---------------------------------|----------------------------------------|


DPU supports random UUID generation using '''randomUUID()''' function in namespace '''uuid-functions'''. Example of usage:
'''xml
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:uuid="uuid-functions"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="2.0">
    <xsl:template match="/">
        <xsl:value-of select="uuid:randomUUID()"/>
    </xsl:template>
</xsl:stylesheet>
'''

***

### Inputs and outputs ###

|Name                |Type       |DataUnit                         |Description                        |
|--------------------|-----------|---------------------------------|-----------------------------------|
|files  |i |FilesDataUnit  |File to be transformed.  |
|files |o |FilesDataUnit  |Transformed file of given type.  |
|config |i |RDFDataUnit   | Configuration (template parameters). |

***

### Version history ###

|Version            |Release notes                                   |
|-------------------|------------------------------------------------|
|2.1.0              | Added support for generation of UUIDs and fixes in build dependencies |
|2.0.0              | Replaces with the DPU taken from the repository https://github.com/mff-uk/DPUs |
|1.5.1              | fix in localization                            |
|1.5.0              | N/A                                            |
|1.4.0              | N/A                                            |
|1.3.1              | N/A                                            |                                
|1.3.0              | N/A                                            |                                

***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|Petr Škoda        |DPU fail if virtual path is not set. |

