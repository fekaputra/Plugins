### Description

Transformer for Excel files (.XLS, .XLSX) which will extract selected sheet (or sheets) into CSV file. Every sheet is extracted to a separate CSV file.

### Configuration parameters

| Name | Description |
|:----|:----|
|**Sheet names** | Sheet names separated by colon that will be transformed to CSV file. Sheet names are case insensitive. If sheet names are not specified then all sheets are transformed to csv.|
|**CSV file name pattern** | Pattern used to create name of generated CSV file. You may use ${excelFileName} for name of the initial excel file (without extension) and ${sheetName} for outputting name of the processed sheet. If you are processing more input files/sheets, use ${excelFileName}/${sheetName} placeholders, so that each produced CSV file has different name.|

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input |i |FilesDataUnit |Input files |x|
|output |o |FilesDataUnit |Produced CSV files |x|
