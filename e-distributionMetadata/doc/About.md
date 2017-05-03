### Description

Generates CKAN Resource model for distribution metadata on output as graph named "distributionMetadata".

**Deprecated. This DPU was mainly relevant for COMSODE project and will be removed from UnifiedViews Core DPUs in the future**

### Configuration parameters

| Name | Description |
|:----|:----|
|**Distribution title in original language** | A name given to the distribution. If the pipeline contains more distributions (e.g., RDF file dump and SPARQL Endpoint) for the same dataset (e.g., "Legal type of organization"), then pipeline designer must ensure that different distributions are named differently, e.g., "Legal type of organization: RDF dump" and "Legal type of organization: SPARQL endpoint" |
|**Description in original language** | Free-text description of the distribution.  |
|**Format** | The format of the distribution. This field should be specified for sparql endpoint/rest api distributions - for sparql endpoint distributions, the value should be "api/sparql", for rest api distributions, the value should be "api/rest". For file dumps this field should be used only when the file dump has media type not supported by IANA. For file dumps with common media types supported by IANA, **Media (MIME) type** (see below) should be filled and this field should be empty.  |
|**Media (MIME) type** | The media type of the distribution as defined by IANA. For file dumps, it should be filled based on the media type (MIME) of the dump. For sparql endpoint/rest api distributions or for file dumps with media types not supported by IANA, this field should be empty and field **Format** should be filled (see above).  |
|**Date created** | Date of the creation of the distribution in CKAN/publication of the distribution to CKAN. If such date is not specified, the date when the CKAN resource is first created in CKAN is automatically used.  |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|distributionOutput |o |RDFDataUnit | Descriptive data. Resource attached to a graph defined name "distributionMetadata" |x|
