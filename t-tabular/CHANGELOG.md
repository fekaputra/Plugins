T-Tabular
----------

v3.0.1
---
* Added support for [RDF Profiler extension](https://grips.semantic-web.at/display/UDDOC/RDF+Profiler)


v3.0.0
---
* Updated to use UnifiedViews helpers 3.X with rdf4j support

v2.3.3
---
* Added support for [RDF Validation extension](https://grips.semantic-web.at/display/UDDOC/RDF+Validation)
* Update to helpers 2.1.7

v2.3.2
---
* Added copy and paste
* Fixed row width guard - row from xls-like files have same size as header.

v2.3.1
---
* Improved slovak translations

v2.3.0
---
* Space trailing is now accessible for all input types.
* Dialog layout update, to provide more space for check boxes.
* For XSL: Option to use advanced value detection for better handling of integers and dates.
* Added option for ignoring missing 'named' column (info log message is used instead of error).
* Update to helpers 2.1.4

v2.2.2
---
* For DBF: Added possibility to trim trailing and leading spaces from values, dialog adjustments.
* Update to helpers 2.1.3

v2.2.1
---
* Fixed bug with wrongly moved NamedCell_V1 class causing corruption of tabular configuration for xls files with named cells.
* Fixed documentation (About)
* Update to API 2.1.2 (properly displayed about tab in UTF-8 encoding)

v2.2.0
---
* Fixed bug with missing trailing columns in xls-like files with empty leading columns and header autogeneration
* Added possibility to strip null value in header
* For XSL: If row is shorter than header then row is expanded

v2.1.1
---
* Improved handling of dependency on POI
* Improved description (About)

v2.1.0
---
* For XLS files: Added possibility to strip null value in header. If row is shorter than header then row is expanded
* Updated to API 2.1.0
* Combobox for selection of various encodings in DPU's dialog
* Fixed bug with :Skip n first lines: for XLS files, where empty text box makes configuration invalid
* Added option "Generate labels"
* Fixed bug with wrong initial column. First column wrongly named as "col2" instead of "col1"

v2.0.1
---
* fixes in build dependencies

v2.0.0
---
* Replaced with the DPU taken from the repository https://github.com/mff-uk/DPUs.

v1.5.0
---
* N/A

v1.4.0
---
* N/A

v1.3.1
---
* N/A

v1.0.0
---
* N/A

