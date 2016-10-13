### Description

Extracts RDF data from input files (any file format) and produces RDF graphs as the output.  

By default, RDF format of the input files is estimated automatically based on the extensions of the input file names. If that does not work correctly, please specify the RDF format of the input files explicitely in the configuration.

Based on the selected policy for creation of the output RDF graphs, output RDF data unit contains either 1) one output RDF graph for each processed input file (by default) OR 2) single output RDF graph for all processed input files. In the former case, the symbolic names for output RDF graphs are created based on the symbolic names of input files; in the latter case, the symbolic name of the single output RDF graph may be specified in the configuration.

It supports [RDF Validation extension](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).

### Configuration parameters

| Name | Description |
|:----|:----|
|**RDF format of the input files** | RDF format of the data in the input files. AUTO = automatic selection of the RDF format of the input files (default) |
|**What to do if the RDF extraction from certain file fails** | Stop execution (default) OR Skip that file and continue |
|**Policy for creation of the output RDF graphs** | One output RDF graph for each processed input file (default) OR Single output RDF graph for all processed input files |
|**Symbolic name of the single output RDF graph** | The desired symbolic name of the single output RDF graph may be specified here (applicable only if the policy for the creation of output RDF graphs is 'Single output RDF graph'. |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|filesInput |i| FilesDataUnit | Input file containing data |x|
|rdfOutput  |o| RDFDataUnit | RDF data extracted |x|
