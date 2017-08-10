### Description

This DPU allows executing HTTP requests (GET, POST methods) to web services and passes the response in form of file data unit.

This DPU targets to enable consuming web services, both. REST and SOAP.

For POST HTTP requests, there are 4 possible modes (type of sent data)
* multipart (form data) body
* raw data (content type can be specified: XML, JSON, ...)
* raw data with bodies from input file(s)  - for each input file a separate HTTP request (raw data) is executed
* multipart (form data) with bodies from an input RDF configuration - for each input set of form params a separate HTTP request is executed

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
|Data type | (POST method) Type of sent data in HTTP request: Form-data (multipart), Raw (text), Raw bodies from input files, Form-data bodies from input RDF configuration  |
|Content-type| (POST/raw) Type of sent raw data, set as HTTP header "Content-Type" (e.g. XML, JSON, SOAP, ...)|
|Request body text encoding | (POST/text) Encoding of HTTP request body text |
|Request body | (POST/text) Text sent in HTTP request body |
|Form data | (POST/multipart) Table of sent form data in the form of key - values |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|requestOutput |o| FilesDataUnit | File(s) containing HTTP response(s) |x|
|requestFilesConfig |i| FilesDataUnit | Files sent as content of raw HTTP POST request | |
|rdfConfig |i| RDFDataUnit | RDF configuration used to configure form-data bodies | |

### Advanced configuration

It is also possible to dynamically configure the request body over the input `config` data unit using RDF data.
This is available only for raw mode and you can configure only the request.

Configuration samples:

```turtle
# to dynamically configure request URL and request body (raw data mode)
<http://localhost/resource/config>
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/httpRequest/Config>;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/requestBody> "..." ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/url> "http://semantic-web.com/service/x".
```


```turtle
# two form-param bodies with the same set of three form params
<http://localhost/resource/config>
    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/httpRequest/Config>;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/formParamBody> <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParamBody/1> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/formParamBody> <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParamBody/2> .

<http://unifiedviews.eu/ontology/dpu/httpRequest/FormParamBody/1>  a <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParamBody> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/formParam> <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam1> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/formParam> <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam2> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/formParam> <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam3> .

<http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam1> a <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/param> "corpusId" ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/value>  "corpus:307b420d-43ad-4771-be41-308199da95b1" .

 <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam2> a <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/param> "text" ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/value>  "Test" .

 <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam3> a <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/param> "title" ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/value>  "Test title" .


 <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParamBody/2>  a <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParamBody> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/formParam> <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam1> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/formParam> <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam2> ;
    <http://unifiedviews.eu/ontology/dpu/httpRequest/formParam> <http://unifiedviews.eu/ontology/dpu/httpRequest/FormParam3> .
```
