T-FusionTool
----------

### Documentation

* see [Plugin Documentation](./doc/About.md)
* see [Plugin Documentation](./doc/About_sk.md) (in Slovak)

### Technical Documentation

* Limitation: The DPU does not take into account metadata about the input data sources, such as the ‘overall quality’ of the input source. Also the output of the DPU does not produces the ‘quality score of the fused triple’, nor provenance information about the sources contributing to the preferred value. This limitation is due to the  fact that Virtuoso (and possibly also other store) cannot effectively handle big amount of named graphs, which are necessary to store provenance and other metadata of the fused triples - roughly every fused triple needs its own graph, so that we may define metadata about that fused triple. 
* Possible extensions: Use VirtualGraph to deal with metadata about the input named graphs. Add one extra output, which will output quads in the files - such quads will include not only fused triples, but also quality scores and other metadata about the resulting fused triples. 
* The DPU also requires the following changes to config.properties:
 * module.frontend.expose = sun.misc,sun.io
 * module.backend.expose = sun.misc,sun.io

### Version history

* see [Changelog](./CHANGELOG.md)
