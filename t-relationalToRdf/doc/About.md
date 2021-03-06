### Description

Converts database tables (SQL) into RDF data

It supports [RDF Validation extension](https://grips.semantic-web.at/display/UDDOC/RDF+Validation).


### Configuration parameters

| Name | Description |
|:----|:----|
|Resource URI base | This value is used as base URI for automatic column property generation and also to create absolute URI if relative URI is provided in 'Property URI' column. |
|Key column | Name of column that will be appended to 'Resource URI base' and used as subject for rows |
|Class for a row entity | This value is used as a class for each row entity |
|Full column mapping | Default mapping is generated for every column |
|Generate row column | Column with row number is generated for each row |
|Generate table/row class | Class is generated for table entities statement with type |
|Generate subject for table | Subject for each table that point to all rows in given table is created |
|Generate labels | rdfs:labels are generated to column URIs |
|Advanced key column | 'Key column' is interpreted as template. Experimental functionality! |
|Auto type as string | All auto types are considered to be strings |


### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|tablesInput |i| RelationalDataUnit| Input database tables |x|
|rdfOutput |o| RDFDataUnit | RDF data |x|
