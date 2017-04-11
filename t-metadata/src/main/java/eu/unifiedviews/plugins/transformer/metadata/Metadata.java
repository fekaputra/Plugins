package eu.unifiedviews.plugins.transformer.metadata;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.vocabulary.FOAF;

import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.EntityBuilder;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsTransformer
public class Metadata extends AbstractDpu<MetadataConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Metadata.class);

    @DataUnit.AsInput(name = "data", optional = true)
    public RDFDataUnit inRdfData;

    @DataUnit.AsOutput(name = "metadata")
    public WritableRDFDataUnit outRdfData;

    @ExtensionInitializer.Init(param = "outRdfData")
    public WritableSimpleRdf rdfData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.metadata.MetadataConfig__V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    public Metadata() {
        super(MetadataVaadinDialog.class, ConfigHistory.noHistory(MetadataConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        final Date dateStart = new Date();

        generateMetadata();

        final Date dateEnd = new Date();
        ContextUtils.sendShortInfo(ctx, "Done in %d ms", (dateEnd.getTime() - dateStart.getTime()));
    }

    private void generateMetadata() throws DPUException {
        final ValueFactory valueFactory = faultTolerance.execute(
                new FaultTolerance.ActionReturn<ValueFactory>() {

                    @Override
                    public ValueFactory action() throws Exception {
                        return rdfData.getValueFactory();
                    }
                });
        // Set output graph.
        final RDFDataUnit.Entry entry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(outRdfData, DataUnitUtils.generateSymbolicName(Metadata.class));
            }
        });
        faultTolerance.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                rdfData.setOutput(entry);
            }
        });
        // Prepare dataset entity and fill it with data.
        final EntityBuilder dataset = new EntityBuilder(valueFactory.createURI(config.getDatasetURI()),
                valueFactory);
        dataset.property(RDF.TYPE, MetadataVocabulary.VOID_DATASET_CLASS);
        dataset.property(RDF.TYPE, MetadataVocabulary.DCAT_DATASET_CLASS);

        final EntityBuilder distro = new EntityBuilder(valueFactory.createURI(config.getDistroURI()),
                valueFactory);
        distro.property(RDF.TYPE, MetadataVocabulary.DCAT_DISTRO_CLASS);

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // Build metadata ...
        if (!StringUtils.isBlank(config.getComsodeDatasetId())) {
            dataset.property(valueFactory.createURI("http://comsode.eu/ontology/dataset"),
                    valueFactory.createURI("http://comsode.eu/resource/dataset/" + config
                            .getComsodeDatasetId()));
        }
        if (config.isIsQb()) {
            dataset.property(RDF.TYPE, MetadataVocabulary.QB_DATA_SET);
        }
        // Description.
        if (!StringUtils.isBlank(config.getDesc_cs())) {
            dataset.property(DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_cs(),
                    config.getLanguage_cs()));
            distro.property(DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_cs(),
                    config.getLanguage_cs()));
        }
        if (!StringUtils.isBlank(config.getDesc_en())) {
            dataset.property(DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_en(), "en"));
            distro.property(DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_en(), "en"));
        }
        // Title.
        if (!StringUtils.isBlank(config.getTitle_cs())) {
            dataset.property(DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_cs(),
                    config.getLanguage_cs()));
            distro.property(DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_cs(),
                    config.getLanguage_cs()));
        }
        if (!StringUtils.isBlank(config.getTitle_en())) {
            dataset.property(DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_en(), "en"));
            distro.property(DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_en(), "en"));
        }
        //
        if (!StringUtils.isBlank(config.getDataDump())) {
            dataset.property(valueFactory.createURI(MetadataVocabulary.VOID + "dataDump"),
                    valueFactory.createURI(config.getDataDump()));

            distro.property(MetadataVocabulary.DCAT_DOWNLOAD_URL, valueFactory.createURI(config.getDataDump()));
            distro.property(MetadataVocabulary.DCAT_MEDIA_TYPE, valueFactory.createLiteral(config.getMime()));
        }
        //
        if (!StringUtils.isBlank(config.getSparqlEndpoint())) {
            dataset.property(valueFactory.createURI(MetadataVocabulary.VOID + "sparqlEndpoint"),
                    valueFactory.createURI(config.getSparqlEndpoint()));
        }
        // Lists ...
        for (String author : config.getAuthors()) {
            dataset.property(DCTERMS.CREATOR, valueFactory.createURI(author));
        }
        for (String publisherName : config.getPublishers()) {
            final EntityBuilder publisher = new EntityBuilder(valueFactory.createURI(publisherName),
                    valueFactory);
            publisher.property(DCTERMS.TYPE, FOAF.AGENT);
            rdfData.add(publisher.asStatements());
            // TODO Add more data about publisher?

            dataset.property(DCTERMS.PUBLISHER, publisher);
        }
        for (String licence : config.getLicenses()) {
            dataset.property(DCTERMS.LICENSE, valueFactory.createURI(licence));
            distro.property(DCTERMS.LICENSE, valueFactory.createURI(licence));
        }
        for (String resourceUri : config.getExampleResources()) {
            dataset.property(MetadataVocabulary.VOID_EXAMPLE_RESOURCE, valueFactory.createURI(resourceUri));
        }
        for (String source : config.getSources()) {
            dataset.property(DCTERMS.SOURCE, valueFactory.createURI(source));
        }
        for (String keyword : config.getKeywords()) {
            dataset.property(MetadataVocabulary.DCAT_KEYWORD, valueFactory.createLiteral(keyword));
        }
        for (String language : config.getLanguages()) {
            dataset.property(DCTERMS.LANGUAGE, valueFactory.createURI(language));
        }
        for (String temeUri : config.getThemes()) {
            final EntityBuilder theme = new EntityBuilder(valueFactory.createURI(temeUri),
                    valueFactory);
            theme.property(RDF.TYPE, SKOS.CONCEPT);
            theme.property(SKOS.IN_SCHEME,
                    valueFactory.createURI("http://linked.opendata.cz/resource/catalog/Themes"));
            rdfData.add(theme.asStatements());

            dataset.property(MetadataVocabulary.DCAT_THEME, theme);
        }
        // ...
        if (config.isUseNow()) {
            dataset.property(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(new Date()),
                    MetadataVocabulary.XSD_DATE));
            distro.property(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(new Date()),
                    MetadataVocabulary.XSD_DATE));
        } else {
            dataset.property(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(config
                    .getModified()),
                    MetadataVocabulary.XSD_DATE));
            distro.property(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(new Date()),
                    MetadataVocabulary.XSD_DATE));
        }
        //
        dataset.property(MetadataVocabulary.DCAT_DISTRIBUTION, distro);
        // Check for cancel.
        if (ctx.canceled()) {
            return;
        }
        // Compute statistics.
        ContextUtils.sendShortInfo(ctx, "Computing statistic ...");
        dataset.property(MetadataVocabulary.VOID_TRIPLES, valueFactory.createLiteral(
                executeCountQuery("SELECT (COUNT (*) as ?count) WHERE {?s ?p ?o}", "count")));
        if (ctx.canceled()) {
            return;
        }
        dataset.property(MetadataVocabulary.VOID_ENTITIES, valueFactory.createLiteral(
                executeCountQuery("SELECT (COUNT (distinct ?s) as ?count) WHERE {?s a ?t}", "count")));
        if (ctx.canceled()) {
            return;
        }
        dataset.property(MetadataVocabulary.VOID_CLASSES, valueFactory.createLiteral(
                executeCountQuery("SELECT (COUNT (distinct ?t) as ?count) WHERE {?s a ?t}", "count")));
        if (ctx.canceled()) {
            return;
        }
        dataset.property(MetadataVocabulary.VOID_PROPERTIES, valueFactory.createLiteral(
                executeCountQuery("SELECT (COUNT (distinct ?p) as ?count) WHERE {?s ?p ?o}", "count")));
        if (ctx.canceled()) {
            return;
        }
        dataset.property(MetadataVocabulary.VOID_D_SUBJECTS, valueFactory.createLiteral(
                executeCountQuery("SELECT (COUNT (distinct ?s) as ?count) WHERE {?s ?p ?o}", "count")));
        if (ctx.canceled()) {
            return;
        }
        dataset.property(MetadataVocabulary.VOID_D_OBJECTS, valueFactory.createLiteral(
                executeCountQuery("SELECT (COUNT (distinct ?o) as ?count) WHERE {?s ?p ?o}", "count")));
        rdfData.add(dataset.asStatements());
        rdfData.add(distro.asStatements());
        //
        ContextUtils.sendShortInfo(ctx, "Computing statistic ... done");



    }

    private Integer executeCountQuery(final String queryAsString, String bindingName) throws DPUException {
        // Prepare SPARQL update query.
        final SparqlUtils.SparqlSelectObject query = faultTolerance.execute(
                new FaultTolerance.ActionReturn<SparqlUtils.SparqlSelectObject>() {

                    @Override
                    public SparqlUtils.SparqlSelectObject action() throws Exception {
                        return SparqlUtils.createSelect(queryAsString,
                                DataUnitUtils.getEntries(inRdfData, RDFDataUnit.Entry.class));
                    }
                });
        final SparqlUtils.QueryResultCollector result = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(inRdfData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                result.prepare();
                SparqlUtils.execute(connection, ctx, query, result);
            }
        });
        if (result.getResults().size() == 1) {
            try {
                return Integer.parseInt(result.getResults().get(0).get(bindingName).stringValue());
            } catch (NumberFormatException ex) {
                throw new DPUException(ex);
            }
        } else {
            throw new DPUException("Unexpected number of results!");
        }
    }

}
