package com.zvos.app.api.sdk.Constants;


import com.zvos.app.api.sdk.exception.AppBizExcCodes;

public enum AppExcCodesEnum implements AppBizExcCodes {

    API_NOT_EXIST("404", "接口不存在");

    private String code;

    private String message;

    AppExcCodesEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getDescByCode(final String code) {
        String desc = null;
        for (AppExcCodesEnum tmp : AppExcCodesEnum.values()) {
            if (tmp.getCode().equals(code)) {
                desc = tmp.getMessage();
                break;
            }
        }
        return desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
