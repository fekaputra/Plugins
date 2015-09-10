package eu.unifiedviews.plugins.extractor.filesdownload;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

public class EasySSL implements SecureProtocolSocketFactory {

    SSLSocketFactory sf;

    Exception exception = null;

    /**
     * Constructor for SSLProtocolSocketFactory.
     */
    public EasySSL() {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");

            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sslContext.init(null, trustAllCerts, null);
            sf = sslContext.getSocketFactory();//new SSLSocketFactory(sslContext); 
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            exception = ex;
        }

        //        super();
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(
            String host,
            int port,
            InetAddress clientHost,
            int clientPort)
            throws IOException, UnknownHostException {
        if (exception != null) {
            throw new IOException(exception);
        }
        return sf.createSocket(
                host,
                port,
                clientHost,
                clientPort
                );
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * This method employs several techniques to circumvent the limitations of older JREs that do not support connect timeout. When running in JRE 1.4 or above
     * reflection is used to call Socket#connect(SocketAddress endpoint, int timeout) method. When executing in older JREs a controller thread is executed. The
     * controller thread attempts to create a new socket within the given limit of time. If socket constructor does not return until the timeout expires, the
     * controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     * 
     * @param host
     *            the host name/IP
     * @param port
     *            the port on the host
     * @param localAddress
     *            the local host name/IP to bind the socket to
     * @param localPort
     *            the port on the local machine
     * @param params
     *            {@link HttpConnectionParams Http connection parameters}
     * @return Socket a new socket
     * @throws IOException
     *             if an I/O error occurs while creating the socket
     * @throws UnknownHostException
     *             if the IP address of the host cannot be
     *             determined
     * @since 3.0
     */
    public Socket createSocket(
            final String host,
            final int port,
            final InetAddress localAddress,
            final int localPort,
            final HttpConnectionParams params
            ) throws IOException, UnknownHostException, ConnectTimeoutException {
        if (exception != null) {
            throw new IOException(exception);
        }
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            Socket socket = createSocket(host, port, localAddress, localPort);
            return socket;
        } else {
            // To be eventually deprecated when migrated to Java 1.4 or above
            Socket socket = ReflectionSocketFactory.createSocket(
                    sf, host, port, localAddress, localPort, timeout);
            if (socket == null) {
                socket = ControllerThreadSocketFactory.createSocket(
                        this, host, port, localAddress, localPort, timeout);
            }
            return socket;
        }
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
        if (exception != null) {
            throw new IOException(exception);
        }
        return sf.createSocket(
                host,
                port
                );
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(
            Socket socket,
            String host,
            int port,
            boolean autoClose)
            throws IOException, UnknownHostException {
        if (exception != null) {
            throw new IOException(exception);
        }
        return sf.createSocket(
                socket,
                host,
                port,
                autoClose
                );
    }

    /**
     * All instances of SSLProtocolSocketFactory are the same.
     */
    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(getClass()));
    }

    /**
     * All instances of SSLProtocolSocketFactory have the same hash code.
     */
    public int hashCode() {
        return getClass().hashCode();
    }

}
