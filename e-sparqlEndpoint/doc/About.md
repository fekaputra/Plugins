### Description

Extracts RDF statements from external SPARQL endpoint using SPARQL Construct query defined in configuration into a single output graph. 

The given query can be automatically rewritten so that results are chunked in order to avoid timeouts, result sets max rows limitations, etc. While rewriting, the outer most ORDER BY and LIMIT are thrown away.

### Configuration parameters

| Name | Description |
|:----|:----|
|**Endpoint URL** | URL of SPARQL endpoint to extract data from |
|**SPARQL Construct** | SPARQL construct used to extract data |
|**Chunk size:** | The given query can be rewritten so that results are chunked in order to avoid timeouts, result sets max rows limitations, etc. This option allows you to specify the chunk size (supported by Virtuoso endpoints). |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|output |o |RdfDataUnit |Extracted RDF statements |x|
