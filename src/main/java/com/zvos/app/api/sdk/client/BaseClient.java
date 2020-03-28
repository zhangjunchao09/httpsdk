package com.zvos.app.api.sdk.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zvos.app.api.sdk.Constants.Constants;
import com.zvos.app.api.sdk.utils.SignatureUtils;
import com.zvos.app.api.sdk.utils.StringUtils;

import java.security.Security;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BaseClient {

    public static SignatureUtils.SignatureMethod signMethod = SignatureUtils.SignatureMethod.valueOf(Constants.SIGN_TYPE_HMAC_SHA256);
    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private String serverUrl;
    private String appId;
    private String appSecret;
    private int connectTimeout = 3000;
    private int readTimeout = 15000;

    /**
     * 清除安全设置
     */
    static {
        Security.setProperty("jdk.certpath.disabledAlgorithms", "");
    }

    public Map<String, String> signHeaders(Object obj) {
        //时间戳
        String timestamp = String.valueOf(System.currentTimeMillis());
        //进行签名
        Map<String, String> params = new HashMap<>(4);
        params.put(Constants.APP_ID, getAppId());
        params.put(Constants.TIMESTAMP, timestamp);
        params.put(Constants.SIGN_TYPE, signMethod.name());
        params.put(Constants.PAYLOAD, gson.toJson(obj));
        String sign = SignatureUtils.sign(signMethod, getAppSecret(), params);
        //设置请求头
        Map<String, String> headers = new HashMap<>(3);
        headers.put(Constants.HEADER_APP_ID, getAppId());
        headers.put(Constants.HEADER_TIMESTAMP, timestamp);
        headers.put(Constants.HEADER_SIGN, sign);
        headers.put("session", "{\"loginName\":\"00751949\",\"unitId\":\"1\"}");
        return headers;
    }

    public String pathParam(String uri, Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (StringUtils.isEmpty(buffer.toString())) {
                    buffer.append("?");
                } else {
                    buffer.append("&");
                }
                buffer.append(entry.getKey()).append("=").append(entry.getValue());
            }
            uri += buffer.toString();
        }
        return uri;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
