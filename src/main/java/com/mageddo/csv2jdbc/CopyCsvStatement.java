package com.mageddo.csv2jdbc;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Syntax is inspired in https://www.postgresql.org/docs/current/sql-copy.html .
 *
 * <ol>
 *   <li><strong><code>,</code></strong> (comma), is the default delimiter</li>
 * </ol>
 * <p>
 * Examples:
 * <pre>
 * <code>
 *   -- loads a csv which has a header to the table filling table columns in the same order as the csv ; is the delimiter
 *   CSV2J COPY :tableName FROM :csvPath WITH HEADER DELIMITER ';' CSV
 *
 *   -- same as previous but the csv haven't a header
 *   CSV2J COPY :tableName FROM :csvPath WITH DELIMITER ';' CSV
 *
 *   -- loads a csv creating the dest table
 *   CSV2J COPY :tableName FROM :csvPath WITH DELIMITER ';' CSV CREATE_TABLE
 * </code>
 * </pre>
 */

@ToString
@Getter
@Builder(access = AccessLevel.PROTECTED)
public class CopyCsvStatement {

  @NonNull
  private Command command;

  private String tableName;

  private List<String> cols;

  private String extractSql;

  @NonNull
  private Path file;

  @NonNull
  private Map<String, Option> options;

  public Charset getCharset() {
    return Charset.forName(this.getOptionOrDefault(Option.ENCODING, Option.DEFAULT_ENCODING).getValue());
  }

  public char getDelimiter() {
    return this.getOptionOrDefault(Option.DELIMITER, Option.DEFAULT_DELIMITER).getValue().charAt(0);
  }

  public boolean hasHeader() {
    return this.getOptionOrDefault(Option.HEADER, null) != null;
  }

  public boolean mustCreateTable() {
    return this.getOptionOrDefault(Option.CREATE_TABLE, null) != null;
  }

  Option getOptionOrDefault(String k, Option defaultV) {
    return this.options.getOrDefault(k, defaultV);
  }

  public CopyCsvStatement validateIsCsv() {
    if (!this.options.containsKey(Option.CSV)) {
      throw new IllegalStateException("Must inform CSV option, ex: ... WITH CSV");
    }
    return this;
  }

  public boolean isZIP() {
    return this.options.containsKey(Option.ZIP);
  }

  public boolean isGZIP() {
    return this.options.containsKey(Option.GZIP);
  }

  protected void setFile(Path file) {
    this.file = file;
  }

  @Getter
  @ToString
  @EqualsAndHashCode(of = "name")
  public static class Option {

    public static final String HEADER = "HEADER";

    public static final String DELIMITER = "DELIMITER";

    public static final String CSV = "CSV";

    public static final String CREATE_TABLE = "CREATE_TABLE";

    public static final String ENCODING = "ENCODING";

    public static final String ZIP = "ZIP";

    public static final String GZIP = "GZIP";


    public static final Option DEFAULT_CSV = new Option(CSV);
    public static final Option DEFAULT_HEADER = new Option(HEADER);

    public static final Option DEFAULT_DELIMITER = new Option(DELIMITER, ",");

    public static final Option DEFAULT_CREATE_TABLE = new Option(CREATE_TABLE);

    public static final Option DEFAULT_ENCODING = new Option(ENCODING, "utf-8");

    public static final Option DEFAULT_ZIP = new Option(ZIP);

    public static final Option DEFAULT_GZIP = new Option(GZIP);

    private final String name;
    private final String value;

    public Option(String name) {
      this.value = null;
      this.name = name;
    }

    public Option(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  public enum Command {
    FROM,
    TO
  }

  public static PreparedStatement of(Connection conn, String sql) {
    return new Csv2JdbcPreparedStatement(new Csv2JdbcExecutor(conn, sql));
  }
}
