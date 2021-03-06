package cz.cuni.mff.xrg.uv.transformer.tabular;

import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnInfo_V1;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ValueGeneratorReplace;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParserType;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.unifiedviews.helpers.dpu.config.VersionedConfig;

/**
 *
 * @author Škoda Petr
 */
public class TabularConfig_V1 implements VersionedConfig<TabularConfig_V2> {

    /**
     * Name of column that will be used as a key. If null then first column
     * is used. Can also contains template for constriction of primary subject.
     */
    private String keyColumnName = null;

    /**
     * Base URI that is used to prefix generated URIs.
     */
    private String baseURI = "http://localhost";

    /**
     * Column mapping simple.
     */
    private Map<String, ColumnInfo_V1> columnsInfo = new LinkedHashMap<>();

    /**
     * Advanced column mapping using string templates directly. Based on
     * http://w3c.github.io/csvw/csv2rdf/#
     *
     * If { or } should be used then they can be escaped like: \{ and \}
     * this functionality is secured by
     * {@link ValueGeneratorReplace#compile}
     */
    private Map<String, String> columnsInfoAdv = new LinkedHashMap<>();

    private String quoteChar = "\"";

    private String delimiterChar = ",";

    private Integer linesToIgnore = 0;

    private String encoding = "UTF-8";

    private Integer rowsLimit = null;

    private ParserType tableType = ParserType.CSV;

    private boolean hasHeader = true;

    /**
     * If false only columns from {@link #columnsInfo} are used.
     */
    private boolean generateNew = true;

    /**
     * If false then for blank cells the {@link TabularOntology#URI_BLANK_CELL}
     * is inserted.
     */
    private boolean ignoreBlankCells = false;

    /**
     * If true then {@link #keyColumnName} is interpreted as advanced = tempalte.
     */
    private boolean advancedKeyColumn = false;

    /**
     * If null no class is set.
     */
    private String rowsClass;

    /**
     * If checked same row counter is used for all files. 
     */
    private boolean staticRowCounter = false;

    /**
     * If true then generate labels for properties (basic)
     */
    private boolean generateLabels = true;

    /**
     * If true then triple with row number is generated for each line.
     */
    private boolean generateRowTriple = true;

    public TabularConfig_V1() {
    }

    public String getKeyColumnName() {
        return keyColumnName;
    }

    public void setKeyColumnName(String keyColumnName) {
        this.keyColumnName = keyColumnName;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public Map<String, ColumnInfo_V1> getColumnsInfo() {
        return columnsInfo;
    }

    public void setColumnsInfo(Map<String, ColumnInfo_V1> columnsInfo) {
        this.columnsInfo = columnsInfo;
    }

    public String getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
    }

    public String getDelimiterChar() {
        return delimiterChar;
    }

    public void setDelimiterChar(String delimiterChar) {
        this.delimiterChar = delimiterChar;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Integer getLinesToIgnore() {
        return linesToIgnore;
    }

    public void setLinesToIgnore(Integer numberOfStartLinesToIgnore) {
        this.linesToIgnore = numberOfStartLinesToIgnore;
    }

    public Integer getRowsLimit() {
        return rowsLimit;
    }

    public void setRowsLimit(Integer rowLimit) {
        this.rowsLimit = rowLimit;
    }

    public ParserType getTableType() {
        return tableType;
    }

    public void setTableType(ParserType tabelType) {
        this.tableType = tabelType;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public boolean isGenerateNew() {
        return generateNew;
    }

    public void setGenerateNew(boolean generateNew) {
        this.generateNew = generateNew;
    }

    public Map<String, String> getColumnsInfoAdv() {
        return columnsInfoAdv;
    }

    public void setColumnsInfoAdv(Map<String, String> columnsInfoAdv) {
        this.columnsInfoAdv = columnsInfoAdv;
    }

    public String getRowsClass() {
        return rowsClass;
    }

    public void setRowsClass(String columnClass) {
        this.rowsClass = columnClass;
    }

    public boolean isIgnoreBlankCells() {
        return ignoreBlankCells;
    }

    public void setIgnoreBlankCells(boolean ignoreBlankCells) {
        this.ignoreBlankCells = ignoreBlankCells;
    }

    public boolean isAdvancedKeyColumn() {
        return advancedKeyColumn;
    }

    public void setAdvancedKeyColumn(boolean advancedKeyColumn) {
        this.advancedKeyColumn = advancedKeyColumn;
    }
    
    public boolean isStaticRowCounter() {
        return staticRowCounter;
    }

    public void setStaticRowCounter(boolean staticRowCounter) {
        this.staticRowCounter = staticRowCounter;
    }

    public boolean isGenerateLabels() {
        return generateLabels;
    }

    public void setGenerateLabels(boolean generateLabels) {
        this.generateLabels = generateLabels;
    }

    public boolean isGenerateRowTriple() {
        return generateRowTriple;
    }

    public void setGenerateRowTriple(boolean generateRowTriple) {
        this.generateRowTriple = generateRowTriple;
    }

    @Override
    public TabularConfig_V2 toNextVersion() {
        TabularConfig_V2 v2 = new TabularConfig_V2();
        v2.setAdvancedKeyColumn(advancedKeyColumn);
        v2.setBaseURI(baseURI);
        v2.setColumnsInfo(columnsInfo);
        v2.setDelimiterChar(delimiterChar);
        v2.setEncoding(encoding);
        v2.setGenerateNew(generateNew);
        v2.setGenerateRowTriple(generateRowTriple);
        v2.setHasHeader(hasHeader);
        v2.setIgnoreBlankCells(ignoreBlankCells);
        v2.setKeyColumn(keyColumnName);
        v2.setLinesToIgnore(linesToIgnore);
        v2.setQuoteChar(quoteChar);
        v2.setRowsClass(rowsClass);
        v2.setRowsLimit(rowsLimit);
        v2.setStaticRowCounter(staticRowCounter);
        v2.setTableType(tableType);

        List<TabularConfig_V2.AdvanceMapping> adv = new LinkedList<>();

        if (columnsInfoAdv != null) {
            for (String uri : columnsInfoAdv.keySet()) {
                final String template = columnsInfoAdv.get(uri);
                adv.add(new TabularConfig_V2.AdvanceMapping(uri, template));
            }
        }
        
        v2.setColumnsInfoAdv(adv);

        return v2;
    }

}
