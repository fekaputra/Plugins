E-HttpRequest
----------

v2.1.0
---
* Support for HTTP PUT/DELETE added
* Accept also empty list of files in case of HTTP POST, do not fail because of that
* Fixed encoding in case of multipart form HTTP POST request (#422)

v2.0.0
---
* Updated to use UnifiedViews helpers 3.X with rdf4j support
* Support for HTTPS
* Support for dynamic configuration (only URL of the request and request body for raw mode)
* Content type may be customized in the file mode
* Support for multiple request bodies with form formParams (configured via RDF dynamic configuration)

v1.0.0
---
* Initial release, compatible with API 2.1.4
