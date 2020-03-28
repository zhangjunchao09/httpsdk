package com.zvos.app.api.sdk.utils;

import com.zvos.app.api.sdk.Constants.Constants;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class SignatureUtils {

    private SignatureUtils() {

    }

    private static final Charset DEFAULT_CHARSET = Charset.forName(Constants.CHARSET_UTF8);

    /**
     * 计算签名
     *
     * @param method
     * @param secret
     * @param params
     * @return
     */
    public static String sign(SignatureMethod method, String secret, Map<String, String> params) {
        String content = getSignContent(params);
        switch (method) {
            case hmacmd5:
            case hmacsha1:
            case hmacsha256:
                return encrypt(method.getMethod(), secret, content);
            default:
                throw new IllegalArgumentException("method is error");
        }
    }

    /**
     * Hmac加密 返回hex格式的结果
     *
     * @param secret
     * @param content
     * @return
     */
    private static String encrypt(String method, String secret, String content) {
        try {
            byte[] data = secret.getBytes(DEFAULT_CHARSET);
            SecretKey secretKey = new SecretKeySpec(data, method);
            // 生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            // 用给定密钥初始化 Mac 对象
            mac.init(secretKey);
            byte[] text = content.getBytes(DEFAULT_CHARSET);
            // 完成 Mac 操作
            byte[] bytes = mac.doFinal(text);
            return ByteUtils.bytes2Hex(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 计算签名content
     *
     * @param params
     * @return
     */
    private static String getSignContent(Map<String, String> params) {
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        int index = 0;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (areNotEmpty(key, value)) {
                content.append((index == 0 ? "" : "&") + key + "=" + value);
                index++;
            }
        }
        return content.toString();
    }

    /**
     * 检查指定的字符串列表是否不为空。
     */
    private static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values == null || values.length == 0) {
            result = false;
        } else {
            for (String value : values) {
                result &= !isEmpty(value);
            }
        }
        return result;
    }

    /**
     * 字符串为空
     *
     * @param value
     * @return
     */
    private static boolean isEmpty(String value) {
        return StringUtils.isEmpty(value);
    }

    public enum SignatureMethod {
        hmacmd5("HmacMD5"),
        hmacsha1("HmacSHA1"),
        hmacsha256("HmacSHA256");

        private String method;

        SignatureMethod(String method) {
            this.method = method;
        }

        public String getMethod() {
            return method;
        }
    }


}