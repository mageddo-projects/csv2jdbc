package com.mageddo.csv2jdbc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

public class Csv2JdbcExecutor {

  //public static final int HEADER_COUNT = 1;

  private final Connection connection;
  private final CopyCsvStatement csvStm;

  public Csv2JdbcExecutor(Connection conn, String sql) {
    this.connection = conn;
    this.csvStm = Csv2JdbcConverter.of(sql);
  }

  public int execute() throws SQLException {
    try {
      switch (csvStm.getCommand()) {
        case FROM:
          return this.loadCsvIntoTable();
        case TO:
          return this.extractQueryToCsv();
        default:
          throw new UnsupportedOperationException(
              String.format("invalid option: %s", this.csvStm.getCommand()));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private int extractQueryToCsv() throws SQLException {
    final AtomicInteger rowCount = new AtomicInteger();
    try {
      CsvTableDaos.streamSelect(this.connection, this.csvStm.getExtractSql(), (rs) -> {
        try (final CSVPrinter printer = this.createCsvPrinter()) {
          Log.log("status=printingRecords");
          final ProxiedResultSet prs = new ProxiedResultSet(rs);
          printer.printRecords(prs, this.csvStm.hasHeader());
          rowCount.set( prs.getRow() - 1 );
        }
      });
    } catch (Exception e) {
      throw new SQLException(e);
    }
    Log.log("status=csvWritten");
    Log.log("status=linesCount, lines={}", rowCount.get());
    return rowCount.get();
    /*try {
      final int lines = Files.countLines(this.csvStm.getFile()) - HEADER_COUNT;
      Log.log("status=linesCount, lines={}", lines);
      return lines;
    } catch (IOException e) {
      throw new SQLException(e);
    }/**/
  }

  private int extractQueryToCsv0() throws SQLException {
    final AtomicInteger rowCount = new AtomicInteger(0);
    try {
      final BufferedWriter out = java.nio.file.Files.newBufferedWriter(this.csvStm.getFile());
      CsvTableDaos.streamSelect(this.connection, this.csvStm.getExtractSql(), (rs) -> {
//        try (final CSVPrinter printer = this.createCsvPrinter()) {
        final int columns = rs
            .getMetaData()
            .getColumnCount();
        while (rs.next()){
          for (int i = 1; i <= columns; i++) {
            out.write(rs.getString(i));
            out.write(", ");
          }
          out.write('\n');
          rowCount.incrementAndGet();
        }
//          printer.printRecords(rs, true);
//        }
      });
    } catch (Exception e) {
      throw new SQLException(e);
    }
    return rowCount.get();
    /*try {
      return Files.countLines(this.csvStm.getFile()) - HEADER_COUNT;
    } catch (IOException e) {
      throw new SQLException(e);
    }/**/
  }

  private int loadCsvIntoTable() throws SQLException {
    try (final CSVParser csvParser = createCsvParser()) {

      final List<String> cols = CsvTableDaos.buildColNames(
          this.connection, this.csvStm.getCols(), csvParser.getHeaderNames(),
          this.csvStm.getTableName()
      );

      this.createTableIfNeedled(cols);

      final CsvTableDaoStrategy dao = CsvWriterFactory.create(this.connection);
      return dao.insertData(this.connection, csvParser, this.csvStm, cols);

    } catch (IOException e) {
      throw new SQLException(e);
    }
  }

  void createTableIfNeedled(List<String> cols) throws SQLException {
    if (this.csvStm.mustCreateTable()) {
      CsvTableDaos.createTable(this.connection, this.csvStm.getTableName(), cols);
    }
  }

  CSVParser createCsvParser() throws IOException {
    final CSVFormat csvFormat = getCsvFormat();
    return CSVParser.parse(this.csvStm.getFile(), this.csvStm.getCharset(), csvFormat);
  }

  private CSVPrinter createCsvPrinter() throws IOException {
    final Appendable appendableOut;
    if( this.csvStm.isGZIP() ) {
      Path file = this.csvStm.getFile();
      file = file.resolveSibling( file.getFileName() + ".gzip" );
      this.csvStm.setFile(file);
      final OutputStream outs = java.nio.file.Files.newOutputStream( file );
      final GZIPOutputStream gzipout = new GZIPOutputStream( outs );
      appendableOut = new OutputStreamWriter(gzipout, this.csvStm.getCharset() );
    } else if( this.csvStm.isZIP() ) {
      final String fileNameInsideZip = this.csvStm.getFile().getFileName().toString();
      Path file = this.csvStm.getFile();
      file = file.resolveSibling( file.getFileName() + ".zip" );
      this.csvStm.setFile(file);
      final OutputStream outs = java.nio.file.Files.newOutputStream( file );
      final ZipOutputStream zipout = new ZipOutputStream( outs );
      final ZipEntry zeCsv = new ZipEntry( fileNameInsideZip );
      zipout.putNextEntry(zeCsv);
      appendableOut = new OutputStreamWriter(zipout, this.csvStm.getCharset() );
    } else {
      appendableOut = java.nio.file.Files.newBufferedWriter( this.csvStm.getFile(),
          this.csvStm.getCharset() );
    }

    return this.getCsvFormat()
        .builder()
        .build()
        .print( appendableOut )
        ;
  }

  private CSVFormat getCsvFormat() {
    return CSVFormat.Builder
        .create(CSVFormat.DEFAULT)
        .setHeader()
        .setSkipHeaderRecord(true)
        .setDelimiter(this.csvStm.getDelimiter())
        .build();
  }

}
