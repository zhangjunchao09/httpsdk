package com.zvos.app.api.sdk.exception;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppBizRuntimeException extends Exception {
    private String code;
    private String localizedMessage;
    private boolean immutable = false;
    private boolean logged = false;
    private Map<String, String> context = new LinkedHashMap<>();

    public AppBizRuntimeException(String code) {
        this.code = code;
    }

    public AppBizRuntimeException(String code, String message) {
        super(message);
        this.code = code;
        this.localizedMessage = message;
    }

    public AppBizRuntimeException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.localizedMessage = message;
    }

    public AppBizRuntimeException(String code, Throwable t) {
        super(t);
        this.code = code;
    }

    public AppBizRuntimeException(String code, String message, Map<String, String> params) {
        super(message);
        this.code = code;
        this.localizedMessage = message;
        this.context.putAll(params);
    }

    public AppBizRuntimeException(String code, String message, Map<String, String> params, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.localizedMessage = message;
        this.context.putAll(params);
    }

    public AppBizRuntimeException(AppBizExcCodes appBizExcCodes) {
        super(appBizExcCodes.getMessage());
        this.code = appBizExcCodes.getCode();
        this.localizedMessage = appBizExcCodes.getMessage();
    }

    public AppBizRuntimeException(AppBizExcCodes appBizExcCodes, Throwable cause) {
        super(appBizExcCodes.getMessage(), cause);
        this.code = appBizExcCodes.getCode();
        this.localizedMessage = appBizExcCodes.getMessage();
    }

    public AppBizRuntimeException(AppBizExcCodes appBizExcCodes, String concatMessage) {
        super(concatMessage + "," + appBizExcCodes.getMessage());
        this.code = appBizExcCodes.getCode();
        this.localizedMessage = this.getMessage();
    }

    public AppBizRuntimeException(AppBizExcCodes appBizExcCodes, String concatMessage, Throwable cause) {
        super(concatMessage + "," + appBizExcCodes.getMessage(), cause);
        this.code = appBizExcCodes.getCode();
        this.localizedMessage = this.getMessage();
    }

    public AppBizRuntimeException(AppBizExcCodes appBizExcCodes, Object... replacements) {
        super(MessageFormat.format(appBizExcCodes.getMessage(), replacements));
        this.code = appBizExcCodes.getCode();
        this.localizedMessage = this.getMessage();
    }

    public AppBizRuntimeException(AppBizExcCodes appBizExcCodes, Throwable cause, Object... replacements) {
        super(MessageFormat.format(appBizExcCodes.getMessage(), replacements), cause);
        this.code = appBizExcCodes.getCode();
        this.localizedMessage = this.getMessage();
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }

    public String getLocalizedMessage() {
        return this.localizedMessage;
    }

    public Map<String, String> getContext() {
        return this.context;
    }

    public void setContext(Map<String, String> args) {
        this.context = args;
    }

    public boolean isImmutable() {
        return this.immutable;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    public boolean isLogged() {
        return this.logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public String toString() {
        String s = this.getClass().getName() + ", code=" + this.code;
        String message = this.getLocalizedMessage();
        return message != null ? s + ": " + message : s;
    }
}
