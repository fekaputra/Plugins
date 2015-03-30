# L-FilesToLocalFS #
----------

###General###

|                              |                                                               |
|------------------------------|---------------------------------------------------------------|
|**Name:**                     |L-FilesToLocalFS                                             |
|**Description:**              |Loads files to the specified local host directory. |
|**Status:**                   |Deprecated. Not supported in Plugins v2.X. Not updated to use Plugin-DevEnv v2.X. Use l-filesUpload instead.  |
|                              |                                                               |
|**DPU class name:**           |FilesToLocalFS     | 
|**Configuration class name:** |FilesToLocalFSConfig_V1                           |
|**Dialogue class name:**      |FilesToLocalFSVaadinDialog | 

***

###Configuration parameters###


|Parameter                        |Description                             |                                                        
|---------------------------------|----------------------------------------|
|**Destination directory absolute path** |Destination path for file to load.  |
|**Move files instead of copy (checkbox)** | Additional self-descriptive option for load.|
|**Replace existing copy (checkbox)** | Additional self-descriptive option for load. |
|**Skip file on error (checkbox)** | Additional self-descriptive option for load. |

***

### Inputs and outputs ###

|Name                |Type       |DataUnit                         |Description                        |
|--------------------|-----------|---------------------------------|-----------------------------------|
|filesInput |i |FilesDataUnit |File loaded to specified (local host) destiation.  |

***

### Version history ###

|Version            |Release notes                                   |
|-------------------|------------------------------------------------|
|1.6.1              |small corrections in dialog                     |                                
|1.6.0              |N/A                                             |                                
|1.3.1              |N/A                                             |                                


***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|N/A               |N/A                   | 

