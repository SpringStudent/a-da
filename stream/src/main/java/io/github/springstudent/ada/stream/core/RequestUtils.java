package io.github.springstudent.ada.stream.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhouNing
 * @date 2025/1/20 16:29
 **/
public class RequestUtils {

    static final int MIN_ARRAY_LEN = 2;
    static final int DIVIDE_INTO_PAIRS = 2;


    public static Map<String, String> parseRequestParam(String url) {
        Map<String, String> map = new HashMap<>();
        if (!url.contains("?")) {
            return null;
        }
        String[] parts = url.split("\\?", DIVIDE_INTO_PAIRS);
        if (parts.length < MIN_ARRAY_LEN) {
            return null;
        }
        String parsedStr = parts[1];
        if (parsedStr.contains("&")) {
            String[] multiParamObj = parsedStr.split("&");
            for (String obj : multiParamObj) {
                parseBasicParam(map, obj);
            }
            return map;
        }
        parseBasicParam(map, parsedStr);
        return map;
    }

    private static void parseBasicParam(Map<String, String> map, String str) {
        String[] paramObj = str.split("=");
        if (paramObj.length < MIN_ARRAY_LEN) {
            return;
        }
        map.put(paramObj[0], paramObj[1]);
    }

}
