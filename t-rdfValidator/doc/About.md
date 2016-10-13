### Description

Validates RDF data. 

Supports ASK or SELECT validation queries. In case of ASK query, if the evaluation of such query returns true, validation fails. In case of SELECT query, if the evaluation of such query returns non-zero tuples, validation fails. 

### Configuration parameters

| Name | Description |
|:----|:----|
|**Validation query** | ASK or SELECT SPARQL query. |
|**Fail execution when validation produce any error** | When checked, DPU will throw an exception when validation fails, causing pipeline to fail.<br>Otherwise, the DPU will just log warning (default).|

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|rdfInput |i| RDFDataUnit | Input RDF to be validated |x|
|rdfOutput|o| RDFDataUnit | Copy of rdfInput data | |
