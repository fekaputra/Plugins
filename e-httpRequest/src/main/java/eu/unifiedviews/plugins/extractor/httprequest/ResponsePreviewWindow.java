package eu.unifiedviews.plugins.extractor.httprequest;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.UserDialogContext;
import org.apache.http.Header;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ResponsePreviewWindow extends Window {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ResponsePreviewWindow.class);

    private HttpRequestExecutor requestExecutor;



    private TextArea httpHeadersArea;

    private Label httpCodeLabel;

    private TextArea responseArea;

    private Button showHeadersButton;

    private UserDialogContext context;

    private VerticalLayout mainLayout;

    public ResponsePreviewWindow(UserDialogContext context, HttpRequestConfig_V1 config) {
        this.context = context;
        this.requestExecutor = new HttpRequestExecutor();
        try {
            this.requestExecutor.initialize(config);
        } catch (DPUException ex) {
            LOG.error("Cannot run test service call because: {}, details: {}", ex.getLocalizedMessage(), ex.getStackTrace());
        }
        //prepare host, http client, context (to cache credentials)
        //furthermore such objects should be used when calling services
        buildLayout();
        setCaption(config.getRequestType() + " | " + config.getRequestURL());
        sendHttpRequest(config);
    }

    private void buildLayout() {
        setClosable(true);
        center();
        setModal(true);
        this.mainLayout = new VerticalLayout();
        this.mainLayout.setMargin(true);
        this.mainLayout.setSpacing(true);
        this.mainLayout.setWidth("100%");
        this.mainLayout.setHeight("-1px");

        this.httpCodeLabel = new Label();
        this.mainLayout.addComponent(this.httpCodeLabel);

        this.responseArea = new TextArea();
        this.responseArea.setWidth("800px");
        this.responseArea.setHeight("250px");
        this.responseArea.setNullRepresentation("");
        this.mainLayout.addComponent(this.responseArea);

        this.showHeadersButton = new Button(this.context.tr("dialog.preview.show.headers"));
        this.showHeadersButton.addClickListener(createShowHeadersButtonListener());
        this.mainLayout.addComponent(this.showHeadersButton);

        this.httpHeadersArea = new TextArea();
        this.httpHeadersArea.setVisible(false);
        this.httpHeadersArea.setNullRepresentation("");
        this.httpHeadersArea.setWidth("650px");
        this.mainLayout.addComponent(this.httpHeadersArea);
        setContent(this.mainLayout);
    }

    private ClickListener createShowHeadersButtonListener() {
        ClickListener listener = new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void buttonClick(ClickEvent event) {
                if (httpHeadersArea.isVisible()) {
                    httpHeadersArea.setVisible(false);
                } else {
                    httpHeadersArea.setVisible(true);
                }
            }
        };

        return listener;
    }

    private void sendHttpRequest(HttpRequestConfig_V1 config) {
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient client = HttpClients.custom().disableContentCompression().build();
        try {
            switch (config.getRequestType()) {
                case GET:
                    httpResponse = this.requestExecutor.sendGetRequest(config, client);
                    break;
                case POST:
                    switch (config.getPostRequestDataType()) {
                        case RAW_DATA:
                            httpResponse = this.requestExecutor.sendRawDataPostRequest(config, client);
                            break;
                        case FORM_DATA:
                            httpResponse = this.requestExecutor.sendMultipartPostRequest(config, client);
                            break;
                        default:
                            return;
                    }
                    break;
                default:
                    return;

            }
            getHttpStatusFromResponse(httpResponse);
            getHeadersFromResponse(httpResponse);
            getHttpContentFromResponse(httpResponse);
        } catch (Exception e) {
            LOG.error("Failed to execute HTTP request", e);
            if (httpResponse != null) {
                getHttpStatusFromResponse(httpResponse);
                getHeadersFromResponse(httpResponse);
                getHttpContentFromResponse(httpResponse);
            } else {
                String errorMsg = "ERROR: " + e.getMessage();
                if (e.getCause() != null) {
                    errorMsg += "\n";
                    errorMsg += e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
                }
                this.responseArea.setValue(errorMsg);
            }
        } finally {
            HttpRequestHelper.tryCloseHttpClient(client);
            HttpRequestHelper.tryCloseHttpResponse(httpResponse);
        }
    }

    private void getHeadersFromResponse(CloseableHttpResponse response) {
        StringBuilder responseAsString = new StringBuilder();
        responseAsString.append(response.getStatusLine().toString()).append('\n');
        for (Header h : response.getAllHeaders()) {
            responseAsString.append(h.toString()).append('\n');
        }
        this.httpHeadersArea.setValue(responseAsString.toString());
    }

    private void getHttpStatusFromResponse(CloseableHttpResponse response) {
        String status = response.getStatusLine().toString();
        this.httpCodeLabel.setValue(status);
    }

    private void getHttpContentFromResponse(CloseableHttpResponse response) {
        try {
            String content = EntityUtils.toString(response.getEntity());
            this.responseArea.setValue(content);
        } catch (ParseException | IOException e) {
            LOG.error("Failed to retrieve HTTP response content", e);
            this.responseArea.setValue(this.context.tr("dialog.preview.content.error"));
        }
    }

}
