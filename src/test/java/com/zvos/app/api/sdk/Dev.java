package com.zvos.app.api.sdk;

import com.zvos.app.api.sdk.client.DeviceClient;
import com.zvos.app.api.sdk.dto.DeviceInfoDto;
import com.zvos.app.api.sdk.exception.AppBizRuntimeException;

import java.io.IOException;

public class Dev {

    private static String serverUrl = "http://10.39.52.223:9892";
    private static String appId = "test"; // 用户名
    private static String appSecret = "abcdefg"; //密码

    public static void main(String[] args) {
        DeviceClient client = new DeviceClient(serverUrl, appId, appSecret, null, null);
        DeviceInfoDto deviceInfoDto = new DeviceInfoDto();
        try {
            deviceInfoDto.setVehicleNo("1");
            client.deviceList(deviceInfoDto);
            client.terminalAll("1");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AppBizRuntimeException e) {
            e.printStackTrace();
        }
    }

}
