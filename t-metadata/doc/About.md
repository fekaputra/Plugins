### Description

Generates metadata on output which describes input.

**Deprecated. This DPU was mainly relevant for COMSODE project and will be removed from UnifiedViews Core DPUs in the future**

### Configuration parameters

| Name | Description |
|:----|:----|
|**Output graph name** | self-descriptive |
|**COMSODE Dataset ID (will be used as part of the URI)** | self-descriptive |
|**Dataset URI** | self-descriptive |
|**Distribution URI** | self-descriptive |
|**Data dump URL** | self-descriptive |
|**Media Type (list)** | = application/zip<BR>- text/csv<BR>- application/rdf+xml<BR>- text/plain<BR>- application/x-turtle |
|**Sparql Endpoint URI** | self-descriptive |
|**Contact Point URL** | self-descriptive |
|**Original language - RDF language tag** | self-descriptive |
|**Title original language** | self-descriptive |
|**Title in English** | self-descriptive |
|**Description original language** | self-descriptive |
|**Description in English** | self-descriptive |
|**Dataset is RDF Data Cube (checkbox)** | self-descriptive |
|**Modified(calendar)** | self-descriptive |
|**Always use current date instead (checkbox)** |s elf-descriptive |
|**Periodicity (list)** | - Monthly<BR>- Annual<BR>- Daily-business week<BR>- Daily<BR>- Minutely<BR>- Quarterly<BR>- Half Yearly, semester<BR>- Weekly |
|**Available licenses/Selected licenses** | self-descriptive |
|**Available example resources/Selected example resources** | self-descriptive |
|**Available sources/Selected sources** | self-descriptive |
|**Available keywords/Selected keywords** | self-descriptive |
|**Available themes/Selected themes** | self-descriptive |
|**Available languages/Selected languages** | self-descriptive |
|**Available authors/Selected authors** | self-descriptive |
|**Available publishers/Selected publishers** | self-descriptive |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|data     |i| RDFDataUnit | Data to be described |x|
|metadata |o| RDFDataUnit | Descriptive data |x| 
