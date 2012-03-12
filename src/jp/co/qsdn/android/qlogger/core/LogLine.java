/*
 * Copyright (C) 2012 QSDN,Inc.
 * Copyright (C) 2012 Atsushi Konno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.qsdn.android.qlogger.core;

import android.os.Parcel;
import android.os.Parcelable;

import android.text.TextUtils;

import java.sql.Timestamp;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class LogLine
  implements Parcelable 
{
  Timestamp timestamp;
  String logLevel;
  int intLogLevel;
  String tag;
  String message;
  String rawLine;  
  boolean stackTraceFlag;
  static Pattern basePattern = null;
  static Pattern stackTracePattern = null;
  static Pattern fatalMainPattern = null;
  static Pattern anrFirstLinePattern = null;
  static Pattern anrDataLinePattern = null;
  static Pattern updateContactPattern = null;


  public static final String LOG_LEVEL = "VDIWEFS";

  /* *MUST NOT* use enum */
  public static final int LOG_LEVEL_VERBOSE     = 0;
  public static final int LOG_LEVEL_DEBUG       = 1;
  public static final int LOG_LEVEL_INFORMATION = 2;
  public static final int LOG_LEVEL_WARNING     = 3;
  public static final int LOG_LEVEL_ERROR       = 4;
  public static final int LOG_LEVEL_FATAL       = 5;
  public static final int LOG_LEVEL_SILENT      = 6;

  static {
    basePattern = Pattern.compile("^([^\\s]+)\\s([^\\s]+)\\s([VDIWEFS])/([^:]+):\\s(.*)$");
    stackTracePattern = Pattern.compile("^((\\s+at\\s.*)|(Caused\\sby:.*)|(\\s+\\.\\.\\.\\s[0-9]+\\smore.*))$");
    fatalMainPattern = Pattern.compile("^FATAL\\sEXCEPTION:.*$");
    anrFirstLinePattern = Pattern.compile("^ANR\\sin\\s.*$");
    anrDataLinePattern = Pattern.compile("^((Reason:\\s.*)|(Load:\\s.*)|(CPU\\susage\\sfrom\\s.*)|(100%\\sTOTAL:\\s.*)|(\\s*[0-9.+]+%\\s.*))$");
    updateContactPattern = Pattern.compile("^(START|END)\\s+UPDATE\\sCONTACT\\s+:.*$");
  };

  public boolean isUpdateContactLine() {
    if (TextUtils.isEmpty(getMessage())) {
      return false;
    }
    Matcher matcher = updateContactPattern.matcher(getMessage());
    return matcher.matches();
  }

  public boolean isAnrFirstLine() {
    if (TextUtils.isEmpty(getMessage())) {
      return false;
    }
    Matcher matcher = anrFirstLinePattern.matcher(getMessage());
    boolean result = matcher.matches();
    if (! result) {
      result = isAnrDataLine();
    }
    return result;
  }
  public boolean isAnrDataLine() {
    if (TextUtils.isEmpty(getMessage())) {
      return false;
    }
    Matcher matcher = anrDataLinePattern.matcher(getMessage());
    return matcher.matches();
  }

  public boolean isFatalException() {
    if (TextUtils.isEmpty(getMessage())) {
      return false;
    }
    Matcher matcher = fatalMainPattern.matcher(getMessage());
    return matcher.matches();
  }
  public int describeContents() {
    return 0;
  }
  public void writeToParcel(Parcel out, int flags) {
    out.writeLong(getTimestamp().getTime());
    out.writeString(getLogLevel());
    out.writeInt(getIntLogLevel());
    out.writeString(getTag());
    out.writeString(getMessage());
    out.writeString(getRawLine());
    out.writeInt((getStackTraceFlag()) ? 1: 0);
  }

  public LogLine(String line) {
    Matcher matcher = basePattern.matcher(line);
    if (matcher.matches()) {
      setLogLevel(matcher.group(3));
      setIntLogLevel(LOG_LEVEL.indexOf(getLogLevel()));
      setMessage(matcher.group(5));
      setTag(matcher.group(4));
      GregorianCalendar calendar = new GregorianCalendar();
      setTimestamp(Timestamp.valueOf("" + calendar.get(Calendar.YEAR) + "-" + matcher.group(1) + " " + matcher.group(2)));

      matcher = stackTracePattern.matcher(getMessage());
      if (matcher.matches()) {
        setStackTraceFlag(true);
      } 
      else {
        setStackTraceFlag(false);
      }
    }
    this.rawLine = line;
  }

  private LogLine(Parcel in) {
    setTimestamp(new Timestamp(in.readLong()));
    setLogLevel(in.readString());
    setIntLogLevel(in.readInt());
    setTag(in.readString());
    setMessage(in.readString());
    setRawLine(in.readString());
    setStackTraceFlag((in.readInt() == 1) ? true : false);
  }

  public static final Parcelable.Creator<LogLine> CREATOR = new Parcelable.Creator<LogLine>() {
    public LogLine createFromParcel(Parcel in) {
      return new LogLine(in);
    }
    public LogLine[] newArray(int size) {
      return new LogLine[size];
    }
  };

  /**
   * @see java.lang.Object#toString
   * @see org.apache.commons.lang.builder.ToStringBuilder
   * @see org.apache.commons.lang.builder.ToStringBuilder#reflectionToString
   */
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * @see java.lang.Object#equals
   * @see org.apache.commons.lang.builder.EqualsBuilder
   * @see org.apache.commons.lang.builder.EqualsBuilder#reflectionEquals
   */
  public boolean equals(Object object) {
    if (!(object instanceof LogLine)) {
      return false;
    }
    return EqualsBuilder.reflectionEquals(this, object);
  }

  /**
   * @see java.lang.Object#hashCode
   * @see org.apache.commons.lang.builder.HashCodeBuilder
   * @see org.apache.commons.lang.builder.HashCodeBuilder#reflectionHashCode
   */
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(17, 37, this);
  }
  public Timestamp getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }
  public String getLogLevel() {
    return logLevel;
  }
  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }
  public String getTag() {
    return tag;
  }
  public void setTag(String tag) {
    this.tag = tag;
  }
  public String getRawLine() {
    return rawLine;
  }
  public void setRawLine(String rawLine) {
    this.rawLine = rawLine;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public int getIntLogLevel() {
    return intLogLevel;
  }
  public void setIntLogLevel(int intLogLevel) {
    this.intLogLevel = intLogLevel;
  }
  
  public boolean getStackTraceFlag() {
    return stackTraceFlag;
  }
  public void setStackTraceFlag(boolean stackTraceFlag) {
    this.stackTraceFlag = stackTraceFlag;
  }
}
