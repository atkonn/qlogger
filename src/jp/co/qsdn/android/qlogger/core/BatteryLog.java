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

import java.sql.Timestamp;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class BatteryLog
  implements Parcelable
{
  long id;
  Timestamp timestamp;
  long timestampLong;
  String yyyymmdd;
  long level;

  public BatteryLog() {
  }

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
    if (!(object instanceof BatteryLog)) {
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

  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public Timestamp getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }
  public long getTimestampLong() {
    return timestampLong;
  }
  public void setTimestampLong(long timestampLong) {
    this.timestampLong = timestampLong;
  }
  public long getLevel() {
    return level;
  }
  public void setLevel(long level) {
    this.level = level;
  }
  public String getYyyymmdd() {
    return yyyymmdd;
  }
  public void setYyyymmdd(String yyyymmdd) {
    this.yyyymmdd = yyyymmdd;
  }

  public int describeContents() {
    return 0;
  }
  public void writeToParcel(Parcel out, int flags) {
    out.writeLong(getId());
    out.writeLong(getTimestamp().getTime());
    out.writeLong(getTimestampLong());
    out.writeString(getYyyymmdd());
    out.writeLong(getLevel());
  }
  private BatteryLog(Parcel in) {
    setId(in.readLong());
    setTimestamp(new Timestamp(in.readLong()));
    setTimestampLong(in.readLong());
    setYyyymmdd(in.readString());
    setLevel(in.readLong());
  }

  public static final Parcelable.Creator<BatteryLog> CREATOR = new Parcelable.Creator<BatteryLog>() {
    public BatteryLog createFromParcel(Parcel in) {
      return new BatteryLog(in);
    }
    public BatteryLog[] newArray(int size) {
      return new BatteryLog[size];
    }
  };

  public String toCsv() {
    return ToStringBuilder.reflectionToString(this, new CsvSimpleToStringStyle());
  }
  public String toCsvTitle() {
    return ToStringBuilder.reflectionToString(this, new CsvColumnTitleToStringStyle());
  }
}
