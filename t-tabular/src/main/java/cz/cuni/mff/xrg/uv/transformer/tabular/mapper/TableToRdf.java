package cz.cuni.mff.xrg.uv.transformer.tabular.mapper;

import java.util.ArrayList;

import cz.cuni.mff.xrg.uv.transformer.tabular.TabularOntology;

import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.plugins.transformer.tabular.column.ValueGenerator;

/**
 * Parse table data into rdf. Before usage this class must be configured by
 * {@link TableToRdfConfigurator}.
 *
 * @author Škoda Petr
 */
public class TableToRdf {

    private static final Logger LOG = LoggerFactory.getLogger(TableToRdf.class);

    /**
     * Data output.
     */
    final WritableSimpleRdf outRdf;

    final ValueFactory valueFactory;

    final TableToRdfConfig config;

    ValueGenerator[] infoMap = null;

    ValueGenerator keyColumn = null;

    String baseUri = null;

    Map<String, Integer> nameToIndex = null;

    URI rowClass = null;

    private final URI typeUri;

    URI tableSubject = null;

    boolean tableInfoGenerated = false;

    public TableToRdf(TableToRdfConfig config, WritableSimpleRdf outRdf,
            ValueFactory valueFactory) {
        this.config = config;
        this.outRdf = outRdf;
        this.valueFactory = valueFactory;
        this.typeUri = valueFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    }

    public void paserRow(List<Object> row, int rowNumber) throws DPUException {
        if (row.size() < nameToIndex.size()) {
            LOG.warn("Row is smaller ({} instead of {}) - ignore.",
                    row.size(), nameToIndex.size());
            return;
        } else if (row.size() > nameToIndex.size()) {
            LOG.warn("Row is too big, some data may be invalid! (size: {} expected: {})",
                    row.size(), nameToIndex.size());
        }
        //
        // trim string values
        //
        if (config.trimString) {
            List<Object> newRow = new ArrayList<>(row.size());
            for (Object item : row) {
                if (item instanceof String) {
                    final String itemAsString = (String)item;
                    newRow.add(itemAsString.trim());
                } else {
                    newRow.add(item);
                }
            }
            row = newRow;
        }
        //
        // get subject - key
        //
        final URI subj = prepareUri(row, rowNumber);
        if (subj == null) {
            LOG.error("Row ({}) has null key, row skipped.", rowNumber);
        }
        //
        // parse the line, based on configuration
        //
        for (ValueGenerator item : infoMap) {
            final URI predicate = item.getUri();
            final Value value = item.generateValue(row, valueFactory);
            if (value == null) {
                if (config.ignoreBlankCells) {
                    // ignore
                } else {
                    // insert blank cell URI
                    outRdf.add(subj, predicate, TabularOntology.BLANK_CELL);
                }
            } else {
                // insert value
                outRdf.add(subj, predicate, value);
            }
        }
        // add row data - number, class, connection to table
        if (config.generateRowTriple) {
            outRdf.add(subj, TabularOntology.ROW_NUMBER, valueFactory.createLiteral(rowNumber));
        }
        if (rowClass != null) {
            outRdf.add(subj, typeUri, rowClass);
        }
        if (tableSubject != null) {
            outRdf.add(tableSubject, TabularOntology.TABLE_HAS_ROW, subj);
        }
        // Add table statistict only for the first time.
        if (!tableInfoGenerated && tableSubject != null) {
            tableInfoGenerated = true;
            if (config.generateTableClass) {
                outRdf.add(tableSubject, RDF.TYPE, TabularOntology.TABLE_CLASS);
            }
        }
    }

    /**
     * Set subject that will be used as table subject.
     *
     * @param newTableSubject Null to turn this functionality off.
     */
    public void setTableSubject(URI newTableSubject) {
        tableSubject = newTableSubject;
        tableInfoGenerated = false;
    }

    /**
     * Return key for given row.
     *
     * @param row
     * @param rowNumber
     * @return
     */
    protected URI prepareUri(List<Object> row, int rowNumber) {
        if (keyColumn == null) {
            return valueFactory.createURI(baseUri + Integer.toString(rowNumber));
        } else {
            return (URI)keyColumn.generateValue(row, valueFactory);
        }
    }

}
