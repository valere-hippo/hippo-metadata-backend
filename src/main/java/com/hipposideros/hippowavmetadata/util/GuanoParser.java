package com.hipposideros.hippowavmetadata.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class GuanoParser {

    public static Map<String, String> parse(String text) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || !line.contains(":")) continue;

            int idx = line.indexOf(':');
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            map.put(key, value);
        }
        return map;
    }

    public static String serialize(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("GUANO|Version: 1.0\n");
        map.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        return sb.toString();
    }
}

