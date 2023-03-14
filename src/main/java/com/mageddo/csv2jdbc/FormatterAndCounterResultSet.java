package com.mageddo.csv2jdbc;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FormatterAndCounterResultSet extends DelegateResultSet {
  private int count = 0;

  private NumberFormat numberFormat;
  private NumberFormat decimalFormat;

  private DateFormat dateTimeFormat;
  private DateFormat dateFormat;
  private DateFormat timeFormat;

  public FormatterAndCounterResultSet(ResultSet delegate, CopyCsvStatement csvStmt) {
    super(delegate);
    configureFormatters( csvStmt.getLanguage(), csvStmt.getNumberFormat(),
        csvStmt.getDecimalFormat(), csvStmt.getDateFormat(), csvStmt.getTimeFormat(),
        csvStmt.getDateTimeFormat() );
  }

  private void configureFormatters(String sLanguage, String sNumberFormat, String sDecimalFormat,
      String sDateFormat, String sTimeFormat, String sDateTimeFormat) {
    final Locale locale = sLanguage != null ? Locale.forLanguageTag(sLanguage) : Locale.getDefault();
    if( sNumberFormat != null ) {
      numberFormat = new DecimalFormat(sNumberFormat, DecimalFormatSymbols.getInstance(locale) );
    } else if( sLanguage!=null ) {
      numberFormat = NumberFormat.getInstance(locale);
    }
    if( sDecimalFormat != null ) {
      decimalFormat = new DecimalFormat(sDecimalFormat, DecimalFormatSymbols.getInstance(locale) );
    } else if( sLanguage!=null ) {
      decimalFormat = NumberFormat.getNumberInstance(locale);
    }

    if( sDateFormat != null ) {
      dateFormat = new SimpleDateFormat( sDateFormat );
    } else if( sLanguage!=null ) {
      dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
    }
    if( sTimeFormat != null ) {
      timeFormat = new SimpleDateFormat( sTimeFormat );
    } else if( sLanguage!=null ) {
      timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
    }
    if( sDateTimeFormat != null ) {
      dateTimeFormat = new SimpleDateFormat( sDateTimeFormat );
    } else if( sLanguage!=null ) {
      dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);
    }
  }

  @Override
  public boolean next() throws SQLException {
    if( super.next() ) {
      count++;
      return true;
    }
    return false;
  }

  @Override
  public int getRow() throws SQLException {
    return count;
  }

  @Override
  public Object getObject(int columnIndex) throws SQLException {
    final Object obj = super.getObject(columnIndex);
    if( obj == null ) {
      return null;
    } else if( obj instanceof Time && timeFormat != null ) {
      return timeFormat.format( obj );
    } else if( obj instanceof java.sql.Date && dateFormat != null ) {
      return dateFormat.format( obj );
    } else if( obj instanceof java.util.Date && dateTimeFormat != null ) {
      return dateTimeFormat.format( obj );
    } else if( (obj instanceof Integer || obj instanceof Long) && numberFormat != null ) {
      return numberFormat.format( obj );
    } else if( (obj instanceof Double || obj instanceof Float || obj instanceof BigDecimal ) && decimalFormat != null ) {
      return decimalFormat.format( obj );
    }
    return obj;
  }
}
