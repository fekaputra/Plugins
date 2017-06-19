E-FilesDownload
----------

v3.0.0
---
* Added support for custom delay (in ms)  between the HTTP calls
* Adjusted creation of Virtual Path (see the user documentation)
* Updated to use UnifiedViews helpers 3.X with rdf4j support

v2.1.6
---
* Added support for soft failure when downloading files - so in case the soft failure is checked in the configuration dialog, when there is a problem processing certain VFS entry or file, warning is shown but the execution of the DPU continues. If unchecked (default), in case of problem processing any VFS entry/file, the execution fails.
* Added support for optional check for duplicated entries to be downloaded
* Corrected bug https://github.com/UnifiedViews/Plugins/issues/383, so that directories as entries are properly processed

v2.1.5
---
* Adjusted layout of the DPU

v2.1.4
---
* fixes in dialog layout
* Update to helpers 2.1.4

v2.1.3
---
* Allowed protocols may be configured, so that only certain protocols are allowed 
* Update to helpers 2.1.3

v2.1.2
---
* Update to API 2.1.2 (properly displayed about tab in UTF-8 encoding)
* Fixed documentation (About)


v2.1.1
---
* support for ignore TLS errors
* Improved description (About)

v2.1.0
---
* Update to API 2.1.0. Timeout value configurable

v2.0.1
---
* fixes in build dependencies

v2.0.0
---
* Update to helpers 2.0.0

v1.0.0
---
* Initial version
