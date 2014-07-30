package ue.unifiedviews.plugins.transformer.tabular;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;
import org.jamel.dbf.structure.DbfHeader;
import org.jamel.dbf.utils.DbfUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataHelper;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

@DPU.AsTransformer
public class Main extends ConfigurableBase<Configuration>
        implements ConfigDialogProvider<Configuration> {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private final String baseODCSPropertyURI = "http://linked.opendata.cz/ontology/odcs/tabular/";

    @DataUnit.AsInput(name = "table")
    public FilesDataUnit inFilesTable;

    @DataUnit.AsOutput(name = "triplifiedTable")
    public WritableRDFDataUnit outRdfTriplifiedTable;

    private RepositoryConnection outConnection;

    public Main() {
        super(Configuration.class);
    }

    @Override
    public AbstractConfigDialog<Configuration> getConfigurationDialog() {
        return new Dialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        //
        // Get file iterator
        //        
        final FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = inFilesTable.getIteration();
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "DPU Failed", "Can't get file iterator.", ex);
            return;
        }
        //
        // Get connection
        //
        try {
            outConnection = outRdfTriplifiedTable.getConnection();
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "DataUnit problem", "Can't get connection.", ex);
            return;
        }
        //
        // Iterate over files
        //
        try {
            while (!context.canceled() && filesIteration.hasNext()) {
                final FilesDataUnit.Entry entry = filesIteration.next();

                String virtualPath = VirtualPathHelpers.getVirtualPath(inFilesTable,
                        entry.getSymbolicName());

                // TODO We can try to use symbolicName here
                if (virtualPath == null) {
                    context.sendMessage(DPUContext.MessageType.WARNING,
                            "No virtual path set for: " + entry
                            .getSymbolicName()
                            + ". File is ignored.");
                    continue;
                }

                final File sourceFile = new File(
                        java.net.URI.create(entry.getFileURIString()));

// TODO Add support for multiple graphs                
                proceedFile(context, sourceFile);

                //
                // Add metadata
                //
            }
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "Problem with DataUnit", "", ex);
        } catch (RepositoryException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "Problem with repository", "", ex);
        }

        try {
            filesIteration.close();

// TODO Remove
            MetadataHelper.dump(outRdfTriplifiedTable);

        } catch (DataUnitException ex) {
            LOG.warn("Error in close.", ex);
        }

        try {
            if (outConnection != null) {
                outConnection.close();
            }
        } catch (RepositoryException ex) {
            LOG.warn("Error in close.", ex);
        }

    }

// TODO Rework this method !!    
    private void proceedFile(DPUContext context, File tableFile)
            throws OperationFailedException, RepositoryException {
        final ValueFactory valueFactory = outConnection.getValueFactory();

        String tableFileName = tableFile.getName();

        Map<String, String> columnPropertyMap = this.config
                .getColumnPropertyMap();
        if (columnPropertyMap == null) {
            LOG.warn(
                    "No mapping of table columns to RDF properties have been specified.");
            columnPropertyMap = new HashMap<>();
        }
        String baseURI = this.config.getBaseURI();
        if (baseURI == null || "".equals(baseURI)) {
            LOG.info(
                    "No base for URIs of resources extracted from rows of the table has been specified. Default base will be applied (http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/)");
            baseURI = "http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/";
        }
        String columnWithURISupplement = this.config
                .getColumnWithURISupplement();
        if (columnWithURISupplement == null || ""
                .equals(columnWithURISupplement)) {
            LOG.info(
                    "No column with values supplementing the base for URIs of resources extracted from rows of the table has been specified. Row number (starting at 0) will be used instead.");
            columnWithURISupplement = null;
        }

        URI propertyRow = valueFactory.createURI(baseODCSPropertyURI + "row");
        if (TableType.CSV.equals(this.config.getTableType())) {

            String quoteChar = this.config.getQuoteChar();
            String delimiterChar = this.config.getDelimiterChar();
            String eofSymbols = this.config.getEofSymbols();

            if (quoteChar == null || "".equals(quoteChar)) {
                quoteChar = "\"";
                LOG.info(
                        "No quote char supplied. Default quote char '\"' will be used.");
            }

            if (delimiterChar == null || "".equals(delimiterChar)) {
                delimiterChar = "\"";
                LOG.info(
                        "No delimiter char supplied. Default delimiter char ',' will be used.");
            }

            if (eofSymbols == null || "".equals(eofSymbols)) {
                eofSymbols = "\n";
                LOG.info(
                        "No end of line symbols supplied. Default end of line symbols '\\n' will be used.");
            }

            final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder(
                    quoteChar.charAt(0), delimiterChar.charAt(0), eofSymbols)
                    .build();

            ICsvListReader listReader = null;
            try {

                listReader = new CsvListReader(new BufferedReader(
                        new InputStreamReader(new FileInputStream(tableFile),
                                config.getEncoding())), CSV_PREFERENCE);

                final String[] header = listReader.getHeader(true);
                int columnWithURISupplementNumber = -1;
                URI[] propertyMap = new URI[header.length];
                for (int i = 0; i < header.length; i++) {
                    String fieldName = header[i];
                    if (columnWithURISupplement != null && columnWithURISupplement
                            .equals(fieldName)) {
                        columnWithURISupplementNumber = i;
                    }
                    if (columnPropertyMap.containsKey(fieldName)) {
                        propertyMap[i] = valueFactory.createURI(
                                columnPropertyMap.get(fieldName));
                    } else {
                        fieldName = this.convertStringToURIPart(fieldName);
                        propertyMap[i] = valueFactory.createURI(
                                baseODCSPropertyURI + fieldName);
                    }
                }

                List<String> row = listReader.read();

                int rowno = 0;
                while (row != null) {

                    if (config.getRowLimit() > 0) {
                        if (rowno >= config.getRowLimit()) {
                            break;
                        }
                    }

                    outConnection.begin();

                    String suffixURI;
                    if (columnWithURISupplementNumber >= 0) {
                        suffixURI = this.convertStringToURIPart(row.get(
                                columnWithURISupplementNumber));
                    } else {
                        suffixURI = (new Integer(rowno)).toString();
                    }

                    Resource subj = valueFactory.createURI(baseURI + suffixURI);

                    int i = 0;
                    for (String strValue : row) {
                        if (strValue == null || "".equals(strValue)) {
                            URI obj = valueFactory.createURI(
                                    "http://linked.opendata.cz/ontology/odcs/tabular/blank-cell");
                            outConnection.add(subj, propertyMap[i], obj);
                        } else {
                            Value obj = valueFactory.createLiteral(strValue);
                            outConnection.add(subj, propertyMap[i], obj);
                        }
                        i++;
                    }

                    Value rowvalue = valueFactory.createLiteral(String.valueOf(
                            rowno));
                    outConnection.add(subj, propertyRow, rowvalue);

                    if ((rowno % 1000) == 0) {
                        LOG.debug("Row number {} processed.", rowno);
                    }

                    rowno++;
                    row = listReader.read();

                    outConnection.commit();

                    if (context.canceled()) {
                        LOG.info("DPU cancelled");
                        listReader.close();
                        return;
                    }
                }
            } catch (IOException ex) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "DPU failed",
                        "IO exception during processing the input CSV file.",
                        ex);
            } finally {
                if (listReader != null) {
                    try {
                        listReader.close();
                    } catch (IOException ex) {
                        context.sendMessage(DPUContext.MessageType.ERROR,
                                "DPU failed",
                                "IO exception when closing the reader of the input CSV file.",
                                ex);
                    }
                }

            }

        } else if (TableType.DBF.equals(this.config.getTableType())) {

            String encoding = this.config.getEncoding();
            if (encoding == null || "".equals(encoding)) {
                DbfReaderLanguageDriver languageDriverReader = new DbfReaderLanguageDriver(
                        tableFile);
                DbfHeaderLanguageDriver languageDriverHeader = languageDriverReader
                        .getHeader();
                languageDriverHeader.getLanguageDriver();
                languageDriverReader.close();

                //TODO Make proper mapping of DBF encoding codes to Java codes. Until this is repaired, we set UTF-8. We suppose that DPUs have set the encoding explicitly by the user.
                encoding = "UTF-8";
            }
            if (!Charset.isSupported(encoding)) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Charset " + encoding + " is not supported.");
                return;
            }

            DbfReader reader = new DbfReader(tableFile);
            DbfHeader header = reader.getHeader();

            int columnWithURISupplementNumber = -1;
            URI[] propertyMap = new URI[header.getFieldsCount()];
            for (int i = 0; i < header.getFieldsCount(); i++) {
                DbfField field = header.getField(i);
                String fieldName = field.getName();

                LOG.debug("Filed: {} type: {} len: {}", field.getName(), field
                        .getDataType(), field.getFieldLength());

                if (columnWithURISupplement != null && columnWithURISupplement
                        .equals(fieldName)) {
                    columnWithURISupplementNumber = i;
                }
                if (columnPropertyMap.containsKey(fieldName)) {
                    propertyMap[i] = valueFactory.createURI(columnPropertyMap
                            .get(fieldName));
                } else {
                    fieldName = this.convertStringToURIPart(fieldName);
                    propertyMap[i] = valueFactory.createURI(
                            baseODCSPropertyURI + fieldName);
                }
            }

            Object[] row = null;
            int rowno = 0;

            while ((row = reader.nextRecord()) != null) {

                if (config.getRowLimit() > 0) {
                    if (rowno >= config.getRowLimit()) {
                        break;
                    }
                }

                String suffixURI;
                if (columnWithURISupplementNumber >= 0) {
                    suffixURI = this.convertStringToURIPart(this.getCellValue(
                            row[columnWithURISupplementNumber], encoding));
                } else {
                    suffixURI = (new Integer(rowno)).toString();
                }

                Resource subj = valueFactory.createURI(baseURI + suffixURI);

                outConnection.begin();

                for (int i = 0; i < row.length; i++) {

                    String strValue = this.getCellValue(row[i], encoding);
                    if (strValue == null || "".equals(strValue)) {
                        URI obj = valueFactory.createURI(
                                "http://linked.opendata.cz/ontology/odcs/tabular/blank-cell");
                        outConnection.add(subj, propertyMap[i], obj);
                    } else {
                        Value obj = valueFactory.createLiteral(this
                                .getCellValue(row[i], encoding));
                        outConnection.add(subj, propertyMap[i], obj);
                    }

                }

                Value rowvalue = valueFactory.createLiteral(this.getCellValue(
                        rowno, encoding));
                outConnection.add(subj, propertyRow, rowvalue);

                outConnection.commit();

                if ((rowno % 1000) == 0) {
                    LOG.debug("Row number {} processed.", rowno);
                }
                rowno++;

                if (context.canceled()) {
                    LOG.info("DPU cancelled");
                    reader.close();
                    return;
                }

            }

            reader.close();
        }
    }

    private String getCellValue(Object cell, String encoding) {
        if (cell instanceof Date) {
            return ((Date) cell).toString();
        } else if (cell instanceof Float) {
            return ((Float) cell).toString();
        } else if (cell instanceof Boolean) {
            return ((Boolean) cell).toString();
        } else if (cell instanceof Number) {
            return ((Number) cell).toString();
        } else {
            try {
                return new String(DbfUtils.trimLeftSpaces((byte[]) cell),
                        encoding);
            } catch (UnsupportedEncodingException ex) {
                //	ignored, solved earlier when reading encoding of the file
                return "";
            }
        }
    }

    private String convertStringToURIPart(String part) {
        return part.replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9-_]", "");
    }

}
