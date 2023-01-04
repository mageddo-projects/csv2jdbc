package com.mageddo.csv2jdbc;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UrlUtils {

  public static final String CHARSET_NAME = StandardCharsets.UTF_8.displayName();

  public static Map<String, List<String>> parseUrlQueryParams(String url) {
    final String[] split = url.split("\\?");
    if (split.length == 2) {
      return parseUrlQueryParams(URI.create("http://acme.com/?" + split[1]));
    }
    return new HashMap<>();
  }

  public static Map<String, List<String>> parseUrlQueryParams(URI url) {
    if (url.getQuery() == null || url.getQuery().trim().isEmpty()) {
      return Collections.emptyMap();
    }
    return Arrays.stream(url.getQuery().split("&"))
        .map(UrlUtils::parseQueryParam)
        .collect(
            Collectors.groupingBy(
                SimpleImmutableEntry::getKey, LinkedHashMap::new,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            )
        );
  }

  private static SimpleImmutableEntry<String, String> parseQueryParam(String it) {
    final int idx = it.indexOf("=");
    final String key = idx > 0 ? it.substring(0, idx) : it;
    final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
    try {
      return new SimpleImmutableEntry<>(
          URLDecoder.decode(key, CHARSET_NAME),
          URLDecoder.decode(value, CHARSET_NAME)
      );
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String encode(String v) {
    try {
      return URLEncoder.encode(v, CHARSET_NAME);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String findBody(String url) {
    return url.split("\\?")[0];
  }
}
