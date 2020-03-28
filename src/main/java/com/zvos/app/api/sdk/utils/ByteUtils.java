package com.zvos.app.api.sdk.utils;

import java.util.HashMap;
import java.util.Map;

public class ByteUtils {

    private static final char[] HEX_CHAR_TABLE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final Map<Character, Byte> HEX_CHAR_MAP = new HashMap<>();

    static {
        for (int i = 0; i < HEX_CHAR_TABLE.length; i++) {
            char c = HEX_CHAR_TABLE[i];
            HEX_CHAR_MAP.put(c, (byte) i);
        }
    }

    public static String byte2Hex(byte value) {
        return String.format("%02X", new Object[]{Byte.valueOf(value)});
    }

    public static String bytes2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(HEX_CHAR_TABLE[(b & 0xf0) >> 4]);
            sb.append(HEX_CHAR_TABLE[b & 0x0f]);
        }
        return sb.toString();
    }

    public static byte[] hex2Bytes(String hexString) {
        //单数长度补全
        if ((hexString.length() & 1) == 1) {
            hexString = "0" + hexString;
        }
        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < result.length; i++) {
            char hi = hexString.charAt(i * 2);
            char lo = hexString.charAt(i * 2 + 1);
            result[i] = (byte) ((HEX_CHAR_MAP.get(hi) << 4) + HEX_CHAR_MAP.get(lo));
        }
        return result;
    }

}