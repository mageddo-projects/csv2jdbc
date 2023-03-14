package com.mageddo.csv2jdbc;

import lombok.Getter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static com.mageddo.csv2jdbc.CopyCsvStatement.Option.GZIP;
import static com.mageddo.csv2jdbc.CopyCsvStatement.Option.ZIP;
import static com.mageddo.csv2jdbc.CopyCsvStatement.Option.BZIP2;

public class CsvExtractor implements Csv2JdbcPreparedStatement.Consumer<ResultSet> {

  private static final Pattern PATTERN = Pattern.compile("\\{([^\\}]+)\\}");
  private final HashMap<String, CSVPrinter> csvPrinterOutput = new HashMap<>(1);
  private final HashMap<String,String> fileReplaceColumns = new HashMap<>();

  private final Collection<OutputStream> lUsedOutputStream = new ArrayList<>();

  private final String filePathAndPattern;
  private final boolean printHeader;
  private final char delimiter;
  private final String compression;
  private final Charset charset;

  @Getter
  private int rowCount = 0;

  public CsvExtractor(String filePathAndPattern, boolean printHeader ) throws SQLException {
    this( filePathAndPattern, printHeader, ';', Charset.defaultCharset(), null );
  }
  public CsvExtractor(String filePathAndPattern, boolean printHeader, char delimiter,
      Charset charset, String compression ) throws SQLException {
    try {
      this.filePathAndPattern = String.format(filePathAndPattern, Calendar.getInstance());
    } catch(Exception e) {
      throw new SQLException(e);
    }
    this.printHeader = printHeader;
    this.delimiter = delimiter;
    this.compression = compression;
    this.charset = charset;
    Matcher m = PATTERN.matcher(filePathAndPattern);
    while (m.find()) {
      fileReplaceColumns.put( m.group(1), Pattern.quote( m.group(0) ) );
    }
  }

  private CSVPrinter createCSVPrinter(String fileName) throws Exception {
    final Path file = Paths.get( fileName );
    if( !Files.exists( file.getParent() ) )
      Files.createDirectories( file.getParent() );
    final Appendable appendableOut;
    if( this.compression.equalsIgnoreCase(GZIP) ) {
      Path gzFile = file.resolveSibling( fileName + ".gzip" );
      final OutputStream outs = java.nio.file.Files.newOutputStream( gzFile );
      final GZIPOutputStream gzipout = new GZIPOutputStream( outs );
      appendableOut = new OutputStreamWriter(gzipout, this.charset );
    } else if( this.compression.equalsIgnoreCase(ZIP) ) {
      Path zFile = file.resolveSibling( fileName + ".zip" );
      //this.csvStm.setFile(file);
      final OutputStream outs = java.nio.file.Files.newOutputStream( zFile );
      //lUsedOutputStream.add(outs);
      final ZipOutputStream zipout = new ZipOutputStream( outs );
      final ZipEntry zeCsv = new ZipEntry( file.getFileName().toString() );
      zipout.putNextEntry(zeCsv);
      appendableOut = new OutputStreamWriter(zipout, this.charset );
    } else if( this.compression.equalsIgnoreCase(BZIP2) ) {
      Constructor<?> constructor = Class.forName("org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream").getConstructor(OutputStream.class);
      Path bz2File = file.resolveSibling( fileName + ".bz2" );
      final OutputStream outs = java.nio.file.Files.newOutputStream( bz2File );
      final OutputStream bz2out = (OutputStream) constructor.newInstance( outs );
      appendableOut = new OutputStreamWriter(bz2out, this.charset );
    } else {
      appendableOut = java.nio.file.Files.newBufferedWriter( file, this.charset );
    }

    final CSVPrinter newCSVPrinter = CSVFormat.Builder
        .create(CSVFormat.DEFAULT)
        .setSkipHeaderRecord(true)
        .setDelimiter(this.delimiter)
        .build()
        .print( appendableOut )
        ;
    this.csvPrinterOutput.put( fileName, newCSVPrinter );
    return newCSVPrinter;
  }

  private CSVPrinter getCSVPrinter(String fileName, ResultSet rs) throws Exception {
    if( !this.csvPrinterOutput.containsKey(fileName) ) {
      final CSVPrinter newCSVPrinter = createCSVPrinter(fileName);
      if( this.printHeader )
        newCSVPrinter.printHeaders(rs);
      return newCSVPrinter;
    }
    return this.csvPrinterOutput.get( fileName );
  }

  @Override
  public void accept(ResultSet rs) throws Exception {
    try {
      if( fileReplaceColumns.isEmpty() ) {
        final CSVPrinter printer = createCSVPrinter(this.filePathAndPattern);
        printer.printRecords(rs, this.printHeader);
        this.rowCount = rs.getRow();
      } else {
        final int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
          String finalFilePath = this.filePathAndPattern;
          for(Map.Entry<String,String> col : fileReplaceColumns.entrySet() ) {
            finalFilePath = finalFilePath.replaceAll( col.getValue(),
                rs.getObject(col.getKey()).toString() );
          }
          CSVPrinter currentCSVPrinter = getCSVPrinter(finalFilePath, rs);
          for (int i = 1; i <= columnCount; i++) {
            final Object object = rs.getObject(i);
            currentCSVPrinter.print(object instanceof Clob ? ((Clob) object).getCharacterStream() :
                object);
          }
          currentCSVPrinter.println();
          rowCount++;
        }
      }
    } finally {
      close();
    }


  }

  private void close() {
    for(CSVPrinter printer : this.csvPrinterOutput.values() ) {
      try {
        printer.close();
      } catch (IOException e) {
        Log.log(e.getMessage(), e);
      }
      System.out.println( printer.getOut().toString() + "\t\t" + printer.getOut().getClass() );
    }
    for(OutputStream output : this.lUsedOutputStream ) {
      try {
        output.close();
      } catch (IOException e) {
        Log.log(e.getMessage(), e);
      }
    }
    this.csvPrinterOutput.clear();
    this.lUsedOutputStream.clear();
  }

  public Collection<String> getFiles() {
    return this.csvPrinterOutput.keySet();
  }

}
