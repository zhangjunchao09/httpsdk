package com.zvos.app.api.sdk.utils;

import com.zvos.app.api.sdk.Constants.AppExcCodesEnum;
import com.zvos.app.api.sdk.Constants.Constants;
import com.zvos.app.api.sdk.exception.AppBizRuntimeException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

public class WebUtils {

    private static final String DEFAULT_CHARSET = Constants.CHARSET_UTF8;
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_PUT = "PUT";

    private static SSLContext ctx = null;

    private static HostnameVerifier verifier = null;

    private static SSLSocketFactory socketFactory = null;

    private static int keepAliveTimeout = 0;

    /**
     * 是否校验SSL服务端证书，默认为不需要校验
     */
    private static volatile boolean needCheckServerTrusted = false;

    /**
     * 设置是否校验SSL服务端证书
     *
     * @param needCheckServerTrusted true：需要校验（默认，推荐）；
     *                               <p>
     *                               false：不需要校验（仅当部署环境不便于进行服务端证书校验，且已有其他方式确保通信安全时，可以关闭SSL服务端证书校验功能）
     */
    public static void setNeedCheckServerTrusted(boolean needCheckServerTrusted) {
        WebUtils.needCheckServerTrusted = needCheckServerTrusted;
    }

    /**
     * 设置KeepAlive连接超时时间，一次HTTP请求完成后，底层TCP连接将尝试尽量保持该超时时间后才关闭，以便其他HTTP请求复用TCP连接
     * <p>
     * KeepAlive连接超时时间设置为0，表示使用默认的KeepAlive连接缓存时长（目前为5s）
     * <p>
     * 连接并非一定能保持指定的KeepAlive超时时长，比如服务端断开了连接
     * <p>
     * 注：该方法目前只在JDK8上测试有效
     *
     * @param timeout KeepAlive超时时间，单位秒
     */
    public static void setKeepAliveTimeout(int timeout) {
        if (timeout < 0 || timeout > 60) {
            throw new RuntimeException("keep-alive timeout value must be between 0 and 60.");
        }
        keepAliveTimeout = timeout;
    }

    private static class DefaultTrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    }

    static {

        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()},
                    new SecureRandom());

            ctx.getClientSessionContext().setSessionTimeout(15);
            ctx.getClientSessionContext().setSessionCacheSize(1000);

            socketFactory = ctx.getSocketFactory();
        } catch (Exception e) {

        }

        verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return false; //不允许URL的主机名和服务器的标识主机名不匹配的情况
            }
        };

    }

    private WebUtils() {
    }

    /**
     * 执行HTTP POST请求，可使用代理proxy。
     *
     * @param url            请求地址
     * @param headers        请求头
     * @param payload        请求参数
     * @param charset        字符集，如UTF-8, GBK, GB2312
     * @param connectTimeout 连接超时时间
     * @param readTimeout    请求超时时间
     * @param proxyHost      代理host，传null表示不使用代理
     * @param proxyPort      代理端口，传0表示不使用代理
     * @return 响应字符串
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> headers, String payload, String charset,
                                int connectTimeout, int readTimeout, String proxyHost,
                                int proxyPort) throws IOException, AppBizRuntimeException {
        String contentType = "application/json;charset=" + charset;
        headers.put("Content-Type", contentType);
        byte[] content = {};
        if (!StringUtils.isEmpty(payload)) {
            content = payload.getBytes(charset);
        }
        return doPost(url, headers, content, connectTimeout, readTimeout, proxyHost, proxyPort);
    }

    public static String doPost(String url, Map<String, String> headers, String payload, String charset, int connectTimeout, int readTimeout) throws IOException, AppBizRuntimeException {
        byte[] content = {};
        if (!StringUtils.isEmpty(payload)) {
            content = payload.getBytes(charset);
        }
        return doPost(url, headers, content, connectTimeout, readTimeout, null, 0);
    }

    public static String doPut(String url, Map<String, String> headers, String payload, String charset, int connectTimeout, int readTimeout) throws IOException, AppBizRuntimeException {
        byte[] content = {};
        if (!StringUtils.isEmpty(payload)) {
            content = payload.getBytes(charset);
        }
        return doPut(url, headers, content, connectTimeout, readTimeout, null, 0);
    }

    public static String doGet(String url, Map<String, String> headers, String charset, int connectTimeout, int readTimeout) throws IOException, AppBizRuntimeException {
        return doGet(url, headers, connectTimeout, readTimeout, null, 0);
    }

    /**
     * 执行HTTP POST请求。
     *
     * @param url            请求地址
     * @param headers        请求头
     * @param content        请求字节数组
     * @param connectTimeout 连接超时时间
     * @param readTimeout    请求超时时间
     * @param proxyHost      代理host，传null表示不使用代理
     * @param proxyPort      代理端口，传0表示不使用代理
     * @return 响应字符串
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> headers, byte[] content, int connectTimeout,
                                int readTimeout, String proxyHost, int proxyPort) throws IOException, AppBizRuntimeException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;
        try {
            try {
                conn = null;
                if (!StringUtils.isEmpty(proxyHost)) {
                    conn = getConnection(new URL(url), METHOD_POST, headers, proxyHost, proxyPort);
                } else {
                    conn = getConnection(new URL(url), METHOD_POST, headers);
                }
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
            } catch (IOException e) {
                throw e;
            }
            try {
                out = conn.getOutputStream();
                out.write(content);
                rsp = getResponseAsString(conn);
            } catch (IOException e) {
                throw e;
            }

        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();

            }
        }

        return rsp;
    }

    public static String doPut(String url, Map<String, String> headers, byte[] content, int connectTimeout,
                               int readTimeout, String proxyHost, int proxyPort) throws IOException, AppBizRuntimeException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;
        try {
            try {
                conn = null;
                if (!StringUtils.isEmpty(proxyHost)) {
                    conn = getConnection(new URL(url), METHOD_PUT, headers, proxyHost, proxyPort);
                } else {
                    conn = getConnection(new URL(url), METHOD_PUT, headers);
                }
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
            } catch (IOException e) {
                throw e;
            }
            try {
                out = conn.getOutputStream();
                out.write(content);
                rsp = getResponseAsString(conn);
            } catch (IOException e) {
                throw e;
            }

        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();

            }
        }

        return rsp;
    }

    public static String doGet(String url, Map<String, String> headers, int connectTimeout,
                               int readTimeout, String proxyHost, int proxyPort) throws IOException, AppBizRuntimeException {
        HttpURLConnection conn = null;
        String rsp = null;
        try {
            try {
                conn = null;
                if (!StringUtils.isEmpty(proxyHost)) {
                    conn = getConnection(new URL(url), METHOD_GET, headers, proxyHost, proxyPort);
                } else {
                    conn = getConnection(new URL(url), METHOD_GET, headers);
                }
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
            } catch (IOException e) {
                throw e;
            }
            try {
                rsp = getResponseAsString(conn);
            } catch (IOException e) {
                throw e;
            }

        } finally {
            if (conn != null) {
                conn.disconnect();

            }
        }

        return rsp;
    }

    public static HttpURLConnection getConnection(URL url, String method, Map<String, String> headers) throws IOException, AppBizRuntimeException {
        return getConnection(url, method, headers, null);
    }

    public static HttpURLConnection getConnection(URL url, String method, Map<String, String> headers,
                                                  String proxyHost, int proxyPort) throws IOException, AppBizRuntimeException {
        Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        return getConnection(url, method, headers, proxy);
    }

    private static HttpURLConnection getConnection(URL url, String method, Map<String, String> headers, Proxy proxy) throws IOException, AppBizRuntimeException {
        HttpURLConnection conn = null;
        if ("https".equals(url.getProtocol())) {
            HttpsURLConnection connHttps = null;
            if (proxy != null) {
                connHttps = (HttpsURLConnection) url.openConnection(proxy);
            } else {
                connHttps = (HttpsURLConnection) url.openConnection();
            }
            if (!needCheckServerTrusted) {
                //设置不校验服务端证书的SSLContext
                connHttps.setSSLSocketFactory(socketFactory);
                connHttps.setHostnameVerifier(verifier);
            }
            conn = connHttps;
        } else {
            if (proxy != null) {
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
        }

        conn.setRequestMethod(method);
        if ("GET".equals(method)) {

        } else {
            conn.setDoInput(true);
            conn.setDoOutput(true);
        }
        conn.setRequestProperty("Accept", "application/json,text/plain,text/xml,text/javascript,text/html");
        conn.setRequestProperty("User-Agent", "zvos-app-api-sdk");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        for (Entry<String, String> e : headers.entrySet()) {
            conn.setRequestProperty(e.getKey(), e.getValue());
        }
        return conn;
    }

    protected static String getResponseAsString(HttpURLConnection conn) throws IOException, AppBizRuntimeException {
        String charset = getResponseCharset(conn.getContentType());

        //此时设置KeepAlive超时所需数据结构才刚初始化完整，可以通过反射修改
        //同时也不宜将修改时机再滞后，因为可能后续连接缓存类已经消费了默认的KeepAliveTimeout值，再修改已经无效
        setKeepAliveTimeout(conn);

        InputStream es = conn.getErrorStream();
        if (es == null) {
            return getStreamAsString(conn.getInputStream(), charset);
        } else {
            String responseCode = conn.getResponseCode() + "";
            String msg = AppExcCodesEnum.getDescByCode(responseCode);
            if (StringUtils.isEmpty(msg)) {
                throw new AppBizRuntimeException(responseCode, conn.getResponseMessage());
            } else {
                throw new AppBizRuntimeException(responseCode, msg);
            }
        }
    }

    private static String getStreamAsString(InputStream stream, String charset) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringWriter writer = new StringWriter();

            char[] chars = new char[256];
            int count = 0;
            while ((count = reader.read(chars)) > 0) {
                writer.write(chars, 0, count);
            }

            return writer.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static String getResponseCharset(String ctype) {
        String charset = DEFAULT_CHARSET;

        if (!StringUtils.isEmpty(ctype)) {
            String[] params = ctype.split(";");
            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        if (!StringUtils.isEmpty(pair[1])) {
                            charset = pair[1].trim();
                        }
                    }
                    break;
                }
            }
        }

        return charset;
    }

    /**
     * 使用指定的字符集编码请求参数值。
     *
     * @param value   参数值
     * @param charset 字符集
     * @return 编码后的参数值
     */
    public static String encode(String value, String charset) {
        String result = null;
        if (!StringUtils.isEmpty(value)) {
            try {
                result = URLEncoder.encode(value, charset);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 由于HttpUrlConnection不支持设置KeepAlive超时时间，该方法通过反射机制设置
     *
     * @param connection 需要设置KeepAlive的连接
     */
    private static void setKeepAliveTimeout(HttpURLConnection connection) {
        if (keepAliveTimeout == 0) {
            return;
        }
        try {
            Field delegateHttpsUrlConnectionField = Class.forName("sun.net.www.protocol.https.HttpsURLConnectionImpl").getDeclaredField("delegate");
            delegateHttpsUrlConnectionField.setAccessible(true);
            Object delegateHttpsUrlConnection = delegateHttpsUrlConnectionField.get(connection);

            Field httpClientField = Class.forName("sun.net.www.protocol.http.HttpURLConnection").getDeclaredField("http");
            httpClientField.setAccessible(true);
            Object httpClient = httpClientField.get(delegateHttpsUrlConnection);

            Field keepAliveTimeoutField = Class.forName("sun.net.www.http.HttpClient").getDeclaredField("keepAliveTimeout");
            keepAliveTimeoutField.setAccessible(true);
            keepAliveTimeoutField.setInt(httpClient, keepAliveTimeout);
        } catch (Throwable ignored) {
            //设置KeepAlive超时只是一种优化辅助手段，设置失败不应阻塞主链路，设置失败不应影响功能
        }
    }

}
