# T-Zipper #
----------

###General###

|                              |                                             |
|------------------------------|---------------------------------------------|
|**Name:**                     |T-Zipper                                     |
|**Description:**              |Zips input files into zip file of given name. |
|**Status:**                   |Supported in Plugins v2.X. Updated to use Plugin-DevEnv v2.X.       |
|                              |                                             |
|**DPU class name:**           |Zipper                                       | 
|**Configuration class name:** |ZipperConfig_V1                              |
|**Dialogue class name:**      |ZipperVaadinDialog                           |

***

###Configuration parameters###

|Parameter                                       |Description                                                              |                                                        
|------------------------------------------------|-------------------------------------------------------------------------|
|**Zip file path/name (with extension):***       |Specifies the path/name for the output file to be created. Given path/name must be relative ie. /data.zip, /data/out.zip. Absolute path like c:/ must not be used. In case unix system /dir/data.zip is interpreted as a relative path. |

***

### Inputs and outputs ###

|Name    |Type           |DataUnit     |Description          |
|--------|---------------|-------------|---------------------|
|input   |i              |FilesDataUnit|List of files to zip. |
|output  |o              |FilesDataUnit|Name of zip file.     |   

### Version history ###

|Version |Release notes |
|--------|--------------|
|2.0.1   | fixes in build dependencies |
|2.0.0   | Update for helpers 2.0.0 |
|1.6.0   | N/A |
|1.5.0   | N/A |
|1.4.0   | N/A |
|1.3.1   | N/A |
|1.0.0   | N/A |

***

### Developer's notes ###

|Author |Notes |
|-------|------|
|Petr Škoda|VirtualPath is required. | 
