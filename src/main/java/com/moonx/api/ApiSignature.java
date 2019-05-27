package com.moonx.api;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ApiSignature {

    private static final Comparator<Map.Entry<String, Object>> keyComparator = new Comparator<Map.Entry<String, Object>>() {
        @Override
        public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    };

    public static String generateSign(Map<String, Object> requestData, long timestamp, String nonceStr, String apiSecret) {
        Map<String, Object> dataJson = new HashMap<>();
        Set<String> keys = requestData.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object data = requestData.get(key);

            if (data != null) {
                dataJson.put(key, requestData.get(key));

            }

        }
        dataJson.put("nonceStr", nonceStr);
        dataJson.put("timestamp", timestamp);
        dataJson.put("apiSecret", apiSecret);
        String result = dataJson.entrySet().stream().sorted(keyComparator)
                .map(entry -> entry.getKey() + "=" + entry.getValue().toString()).collect(Collectors.joining("&"));

        String sign = DigestUtils.md5Hex(result).toUpperCase();
        return sign;
    }

    public static boolean checkSign(String response, String apiSecret) {
        JSONObject responseJson = JSONObject.parseObject(response);
        JSONObject dataJson = responseJson.getJSONObject("data");
        long timestamp = responseJson.getLongValue("timestamp");
        String nonceStr = responseJson.getString("nonceStr");
        String responseSign = responseJson.getString("sign");
        String sign = generateSign(dataJson, timestamp, nonceStr, apiSecret);
        return responseSign.equals(sign);
    }
}