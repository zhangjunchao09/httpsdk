package com.zvos.app.api.sdk.client;

import com.zvos.app.api.sdk.Constants.Constants;
import com.zvos.app.api.sdk.dto.DeviceInfoDto;
import com.zvos.app.api.sdk.dto.ResultDto;
import com.zvos.app.api.sdk.exception.AppBizRuntimeException;
import com.zvos.app.api.sdk.utils.WebUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeviceClient extends BaseClient {

    public DeviceClient(String serverUrl, String appId, String appSecret, Integer connectTimeout, Integer readTimeout) {
        this.setServerUrl(serverUrl);
        this.setAppId(appId);
        this.setAppSecret(appSecret);
        if (connectTimeout != null) {
            this.setConnectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            this.setReadTimeout(readTimeout);
        }
    }


    public ResultDto deviceList(DeviceInfoDto request) throws IOException, AppBizRuntimeException {
        Map headers = signHeaders(request);
        String url = "/v1/vehicle.list";
        String uri = String.format("%s%s", getServerUrl(), url);
        String payload = gson.toJson(request);
        String res = WebUtils.doPost(uri, headers, payload, Constants.CHARSET_UTF8, getConnectTimeout(), getReadTimeout());
        ResultDto resultDto = gson.fromJson(res, ResultDto.class);
        resultDto.setMessage(resultDto.getMsg());
        return resultDto;
    }

    public ResultDto terminalAll(String vehicleNo) throws IOException, AppBizRuntimeException {
        String url = "/v1/vehicle/terminal/all";
        String uri = String.format("%s%s", getServerUrl(), url);
        Map<String, String> params = new HashMap();
        params.put("vehicleNo", vehicleNo);
        params.put("23", "2432");
        Map headers = signHeaders(params);
        uri = pathParam(uri, params);
        String res = WebUtils.doGet(uri, headers, Constants.CHARSET_UTF8, getConnectTimeout(), getReadTimeout());
        ResultDto resultDto = gson.fromJson(res, ResultDto.class);
        resultDto.setMessage(resultDto.getMsg());
        return resultDto;
    }

}
