package eu.unifiedviews.plugins.extractor.httprequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.helpers.dataunit.resource.Resource;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.RdfConfiguration;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestConfig_V1.DataType;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestConfig_V1.RequestType;
import eu.unifiedviews.plugins.extractor.httprequest.rdfConfig.FormParamBody;

@DPU.AsExtractor
public class HttpRequest extends AbstractDpu<HttpRequestConfig_V1> {

    public static final String[] CHARSETS = { "UTF-8", "windows-1250", "ISO-8859-2", "US-ASCII", "IBM00858", "IBM437", "IBM775", "IBM850", "IBM852", "IBM855", "IBM857", "IBM862",
            "IBM866", "ISO-8859-1", "ISO-8859-4", "ISO-8859-5", "ISO-8859-7", "ISO-8859-9", "ISO-8859-13", "ISO-8859-15", "KOI8-R", "KOI8-U", "UTF-16", "UTF-16BE", "UTF-16LE",
            "UTF-32", "UTF-32BE", "UTF-32LE", "x-UTF-32BE-BOM", "x-UTF-32LE-BOM", "windows-1251", "windows-1252", "windows-1253", "windows-1254", "windows-1257",
            "x-IBM737", "x-IBM874", "x-UTF-16LE-BOM" };

    private DPUContext context;

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

    @DataUnit.AsOutput(name = "requestOutput")
    public WritableFilesDataUnit requestOutput;

    @DataUnit.AsInput(name = "requestFilesConfig", optional = true)
    public FilesDataUnit requestFilesConfig;

    @RdfConfiguration.ContainsConfiguration
    @DataUnit.AsInput(name = "rdfConfig", optional = true)
    public RDFDataUnit rdfConfiguration;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init
    public RdfConfiguration _rdfConfiguration;

    private HttpRequestExecutor requestExecutor;

    protected CloseableHttpClient client;

    protected HttpStateWrapper httpWrapper;

    public HttpRequest() {
        super(HttpRequestVaadinDialog.class, ConfigHistory.noHistory(HttpRequestConfig_V1.class));
        this.requestExecutor = new HttpRequestExecutor();
    }

    @Override
    protected void innerExecute() throws DPUException {
        this.context = this.ctx.getExecMasterContext().getDpuContext();
        String shortMessage = this.ctx.tr("dpu.ckan.starting", this.getClass().getSimpleName());
        String longMessage = String.valueOf(this.config);
        this.context.sendMessage(DPUContext.MessageType.INFO, shortMessage, longMessage);
        CloseableHttpResponse httpResponse = null;

        //TODO Deprecated and should be removed (see line below where client is prepared)
        this.client = HttpClients.custom().disableContentCompression().build();

        //initialize the request executor
        requestExecutor.initialize(config);

        try {
            switch (this.config.getRequestType()) {
                case GET:
                    LOG.info("Going to send HTTP GET request to URL {}", this.config.getRequestURL());
                    httpResponse = this.requestExecutor.sendGetRequest(this.config, this.client);
                    checkResponseAndCreateFile(httpResponse, this.config.getFileName());
                    break;
                case POST:
                    LOG.info("Going to send HTTP POST request to URL {}", this.config.getRequestURL());
                    switch (this.config.getPostRequestDataType()) {
                        case FORM_DATA:
                            httpResponse = this.requestExecutor.sendMultipartPostRequest(this.config, this.client);
                            checkResponseAndCreateFile(httpResponse, this.config.getFileName());
                            break;
                        case RAW_DATA:
                            httpResponse = this.requestExecutor.sendRawDataPostRequest(this.config, this.client);
                            checkResponseAndCreateFile(httpResponse, this.config.getFileName());
                            break;
                        case FILE:
                            executeFileHttpRequests(this.client);
                            break;
                        case FORM_DATA_RDF:
                            executeFormDataHttpRequestsForInputRdfConfiguration(this.client);
                            break;
                        default:
                            String errorMsg = String.format("Unknown data type; Supported data types are: %s", String.valueOf(DataType.values()));
                            LOG.error(errorMsg);
                            throw new DPUException(errorMsg);

                    }

                    break;
                default:
                    ContextUtils.sendError(this.ctx, "dpu.errors.request.unknown", "dpu.errors.request.unknown.long", String.valueOf(RequestType.values()));
                    return;

            }

        } catch (Exception e) {
            LOG.error("Failed to send HTTP request", e);
//            ContextUtils.sendError(this.ctx, "dpu.errors.request", "dpu.errors.request.long");
            throw ContextUtils.dpuException(this.ctx, e, "dpu.errors.request");
        } finally {
            HttpRequestHelper.tryCloseHttpClient(this.client);
            HttpRequestHelper.tryCloseHttpResponse(httpResponse);
        }
    }


    /**
     * Executes HTTP request for each input file
     * 
     * @param client
     * @throws DPUException
     */
    private void executeFileHttpRequests(CloseableHttpClient client) throws DPUException {
        List<FilesDataUnit.Entry> files;
        int errorCounter = 0;
        try {
            files = FaultToleranceUtils.getEntries(this.faultTolerance, this.requestFilesConfig, FilesDataUnit.Entry.class);
            if (files.isEmpty()) {
                ContextUtils.sendError(this.ctx, "dpu.errors.files.empty", "dpu.errors.files.empty.long");
                return;
            }
            CloseableHttpResponse httpResponse = null;
            int counter = 1;

            File inputFile = null;
            for (FilesDataUnit.Entry entry : files) {
                try {
                    inputFile = new File(URI.create(entry.getFileURIString()));
                    String targetFileName = String.format("%03d", counter++) + "_" + this.config.getFileName();
                    httpResponse = this.requestExecutor.sendFilePostRequest(this.config, inputFile, client);
                    checkResponseAndCreateFile(httpResponse, targetFileName);
                } catch (Exception e) {
                    ContextUtils.sendShortWarn(this.ctx, "dpu.errors.request.file", (inputFile != null) ? inputFile.getName() : "NULL");
                    errorCounter++;
                } finally {
                    HttpRequestHelper.tryCloseHttpResponse(httpResponse);
                }
            }

            if (errorCounter == files.size()) {
                ContextUtils.sendError(this.ctx, "dpu.errors.files.all", "dpu.errors.files.all.long");
                return;
            }
        } catch (Exception e) {
            throw ContextUtils.dpuException(this.ctx, "dpu.errors.files.input");
        }

    }

    /**
     * Executes HTTP request for each input file
     *
     * @param client
     * @throws DPUException
     */
    private void executeFormDataHttpRequestsForInputRdfConfiguration(CloseableHttpClient client) throws DPUException {

        CloseableHttpResponse httpResponse = null;
        int errorCounter = 0;
        int counter = 1;

        if (config.getFormParamBodies() == null || config.getFormParamBodies().isEmpty()) {
            throw new DPUException("RDF configuration with form param bodies is missing. The processing ends with an error");
        }
        LOG.info("Sending POST requests with bodies coming over RDF data unit");
        LOG.info("Number of bodies: {}", config.getFormParamBodies().size());

        for (FormParamBody paramBody : config.getFormParamBodies()) {
            LOG.info("ParamBody: {}", paramBody);
            try {
                String targetFileName = String.format("%03d", counter++);
                httpResponse = this.requestExecutor.sendMultipartFormRequestFromRdf(this.config, paramBody, client);
                checkResponseAndCreateFile(httpResponse, targetFileName);
            } catch (Exception e) {
                ContextUtils.sendShortWarn(this.ctx, "dpu.errors.request.formParamRdf", paramBody);
                errorCounter++;
            } finally {
                HttpRequestHelper.tryCloseHttpResponse(httpResponse);
            }
        }

        if (errorCounter == config.getFormParamBodies().size()) {
            ContextUtils.sendError(this.ctx, "dpu.errors.files.all", "dpu.errors.files.all.long");
            return;
        }

    }


    private void checkResponseAndCreateFile(CloseableHttpResponse httpResponse, String fileName) throws DPUException {
        if (httpResponse == null) {
            throw ContextUtils.dpuException(this.ctx, "dpu.errors.response");
        }

        LOG.debug("Going to create file from HTTP response body");
        createFileFromResponse(httpResponse, fileName);
        LOG.debug("File from HTTP response successfully created");
    }

    /**
     * Creates file data unit from HTTP response
     * 
     * @param response
     *            HTTP response
     * @param fileName
     *            File name of result file
     * @throws DPUException
     */
    private void createFileFromResponse(CloseableHttpResponse response, final String fileName) throws DPUException {
        LOG.debug("Filename is: {}", fileName);
        // Prepare new output file record.
        final FilesDataUnit.Entry destinationFile = this.faultTolerance.execute(new FaultTolerance.ActionReturn<FilesDataUnit.Entry>() {

            @SuppressWarnings("unqualified-field-access")
            @Override
            public FilesDataUnit.Entry action() throws Exception {
                return FilesDataUnitUtils.createFile(HttpRequest.this.requestOutput, fileName);
            }
        });

        this.faultTolerance.execute(new FaultTolerance.Action() {

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void action() throws Exception {
                final Resource resource = ResourceHelpers.getResource(HttpRequest.this.requestOutput, fileName);
                final Date now = new Date();
                resource.setCreated(now);
                resource.setLast_modified(now);
                ResourceHelpers.setResource(HttpRequest.this.requestOutput, fileName, resource);
            }
        }, "dpu.errors.resource");

        try {
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(),
                    FaultToleranceUtils.asFile(this.faultTolerance, destinationFile));
        } catch (IOException ex) {
            LOG.error("Failed to create file from response input stream", ex);
            throw ContextUtils.dpuException(this.ctx, ex, "dpu.errors.response.store");
        }
    }

    protected void setRequestExecutor(HttpRequestExecutor executor) {
        this.requestExecutor = executor;
    }

}
