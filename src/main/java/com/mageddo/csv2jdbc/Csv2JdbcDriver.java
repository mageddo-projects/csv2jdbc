package com.mageddo.csv2jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class Csv2JdbcDriver implements Driver {

  public static final String PROP_DELEGATE_DRIVER_CLASSNAME = "delegateDriverClassName";
  public static final String PROP_STM_BUFFER_SIZE = "stmBufferSize";

  public static final Set<String> PROPS = new LinkedHashSet<>(Arrays.asList(
      PROP_DELEGATE_DRIVER_CLASSNAME,
      PROP_STM_BUFFER_SIZE
  ));

  public static final String URL_PREFIX = "jdbc:csv2jdbc:";


  static {
    try {
      DriverManager.registerDriver(new Csv2JdbcDriver());
    } catch (SQLException e) {
      throw new RuntimeException("Driver registering failed: " + e.getMessage());
    }
  }

  private PrintWriter log;
  private Driver delegate;

  @Override
  public Connection connect(String url, Properties info) throws SQLException {

    this.log = new PrintWriter(System.out, true);

    final Map<String, List<String>> params = UrlUtils.parseUrlQueryParams(url);
    final String delegateDriverClassName = getOrDefault(params, PROP_DELEGATE_DRIVER_CLASSNAME, "org.h2.Driver");
    this.delegate = Reflections.createInstance(delegateDriverClassName);
    final String delegateUrl = toDelegateUrl(url);

    final String buffSize = Objects.mapOrNull(getOrDefault(params, PROP_STM_BUFFER_SIZE), (it) -> {
      System.setProperty("csv2jdbc.buffSize", it);
      return it;
    });

    Log.log(
        "status=createdProxyDriver, delegateDriverClassName=%s, buffSize=%s, delegateUrl=%s%n",
        delegateDriverClassName, buffSize, delegateUrl
    );
    return new Csv2JdbcConnection(this.delegate.connect(delegateUrl, info));
  }

  private String getOrDefault(Map<String, List<String>> params, String k) {
    return getOrDefault(params, k, null);
  }
  private String getOrDefault(Map<String, List<String>> params, String k, String def) {
    return Optional.ofNullable(params.get(k))
                   .orElse(Collections.emptyList())
                   .stream()
                   .findFirst()
                   .orElse(def);
  }

  private static String toDelegateUrl(String url) {
    final StringBuilder newUrl = new StringBuilder(UrlUtils.findBody(url.replace(URL_PREFIX, "jdbc:")));
    final Map<String, List<String>> params = UrlUtils.parseUrlQueryParams(url);
    PROPS.forEach(params::remove);

    if (!params.isEmpty()) {
      newUrl.append('?');
    }

    params.forEach((k, v) -> {

      for (final String val : v) {
        newUrl.append(UrlUtils.encode(k));
        newUrl.append('=');
        newUrl.append(UrlUtils.encode(val));
        newUrl.append('&');
      }

    });

    return newUrl.toString();
  }

  @Override
  public boolean acceptsURL(String url) {
    return url.startsWith(URL_PREFIX);
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
    return this.delegate.getPropertyInfo(url, info);
  }

  @Override
  public int getMajorVersion() {
    return this.delegate.getMajorVersion();
  }

  @Override
  public int getMinorVersion() {
    return this.delegate.getMinorVersion();
  }

  @Override
  public boolean jdbcCompliant() {
    return this.delegate.jdbcCompliant();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return this.delegate.getParentLogger();
  }
}
