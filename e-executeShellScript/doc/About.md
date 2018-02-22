### Description

Executes shell script with provided configuration

### Configuration parameters

| Name | Description |
|:----|:----|
|Script name|The name of script to execute.|
|Configuration|Text which will be used as configuration for the script.|

### Technical detail

* The executed script must be prepared to expect 3 positional arguments: configuration, list of script input files (not mandatory), and output directory.
* Configuration is textfile with script arguments
* List of script input files provided as the second argument is a plain text document with an absolute file path per line.
* all output files have to be placed into output directory
* Script to run must be placed in directory configured in runtime properties (UnifiedViews UI -> Settings -> Runtime Properties) under the key shell.scripts.path


### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|filesInut |i|FilesDataUnit|DataUnit with script input files. ||
|filesOutput |o|FilesDataUnit|DataUnit which outputs all script output files.|X|

