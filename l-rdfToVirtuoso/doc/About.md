### Description

Loads RDF graphs to external Virtuoso instance (using OpenRDF Repository API (VirtuosoRepository))
Optionally, it copies input data to optional output data unit `rdfOutput`, including metadata about stored graphs, target graph names.

### Configuration parameters

| Name | Description |
|:----|:----|
|**Virtuoso JDBC URL** | URL for establishing JDBC session with Virtuoso server |
|**Username** | Username for Virtuoso server |
|**Password** | Password for the username |
|**Clear destination graph before loading (checkbox)** | Self-descriptive |
|**Target Graph** | Target graph URI. May be empty to indicate per-graph loading. In per-graph mode, each graph on input is loaded into separate graph on output, graph name is taken from VirtualGraph, if VirtualGraph is not set, internal RDF store graph name is used |
|**Thread count** | How many threads may be used to speed up loading|
|**Skip file on error (checkbox)** | Do not stop the pipeline when error occurs (if checked) |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|rdfInput  |i| RDFDataUnit | RDF graphs to be loaded |x|
|rdfOutput |o| RDFDataUnit | Metadata about graphs stored into Virtuoso | |
