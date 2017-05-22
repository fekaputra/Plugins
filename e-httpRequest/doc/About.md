### Description

This DPU allows executing HTTP requests (GET, POST methods) to web services and passes the response in form of file data unit.

This DPU targets to enable consuming web services, both. REST and SOAP.

For POST HTTP requests, there are 3 possible modes (type of sent data)
* multipart (form data)
* raw text data (content type can be specified: XML, JSON, ...)
* binary (file) - for each input file a separate HTTP request is executed

If sent data are multipart or raw, DPU offers possibility to preview the HTTP response in design time.

It also supports HTTPS requests.

### Configuration parameters

|Parameter | Description                                                              |
|:----|:----|
|HTTP method | HTTP request method. Supported: GET, POST. Based on method additional configuration is shown |
|URL address | URL address of the target web service, where HTTP or HTTPS request will be sent |
|Target file name| Name of created file where the content of the HTTP response is stored |
|Target files suffix | (POST / file mode) Suffix of created files containing the content of HTTP responses. Names of files are 001_suffix, 002_suffix,...|
|Basic authentication | Sets BASIC authentication (user name, password) for HTTP request |
|User name | (if authentication is on) User name for basic authentication |
|Password | (if authentication is on) Password for basic authentication |
|Data type | (POST method) Type of sent data in HTTP request: Form-data (multipart), Raw (text), File  |
|Typ obsahu| (POST/raw) Type of sent raw data, set as HTTP header "Content-Type" (e.g. XML, JSON, SOAP, ...)|
|Request body text encoding | (POST/text) Encoding of HTTP request body text |
|Requesst body | (POST/text) Text sent in HTTP request body |
|Form data | (POST/multipart) Table of sent form data in form of key - values |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|requestOutput |o| FilesDataUnit | File(s) containing HTTP response(s) |x|
|requestFilesConfig |i| FilesDataUnit | Files sent as HTTP request content | |
|config |i| RDFDataUnit | RDF config input | |

### Advanced configuration

It is also possible to dynamically configure the request body over the input `config` data unit using RDF data.
This is available only for raw mode and you can configure only the request.

Configuration samples:

```turtle
<http://localhost/resource/config>
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/httpRequest/Config>;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/requestBody> "..." ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/url> "http://semantic-web.com/service/x".
```
