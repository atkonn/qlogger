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

public class CpuUtilizationLog
  implements Parcelable
{
  long id;
  String yyyymmdd;
  float user;
  float nice;
  float system;
  float idle;
  long createdOnLong;
  Timestamp createdOn;

  public CpuUtilizationLog() {
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
    if (!(object instanceof CpuUtilizationLog)) {
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


  public int describeContents() {
    return 0;
  }
  public void writeToParcel(Parcel out, int flags) {
    out.writeLong(getId());
    out.writeString(getYyyymmdd());
    out.writeFloat(getUser());
    out.writeFloat(getNice());
    out.writeFloat(getSystem());
    out.writeFloat(getIdle());
    out.writeLong(getCreatedOnLong());
    out.writeLong(getCreatedOn().getTime());
  }
  private CpuUtilizationLog(Parcel in) {
    setId(in.readLong());
    setYyyymmdd(in.readString());
    setUser(in.readFloat());
    setNice(in.readFloat());
    setSystem(in.readFloat());
    setIdle(in.readFloat());
    setCreatedOnLong(in.readLong());
    setCreatedOn(new Timestamp(in.readLong()));
  }

  public static final Parcelable.Creator<CpuUtilizationLog> CREATOR = new Parcelable.Creator<CpuUtilizationLog>() {
    public CpuUtilizationLog createFromParcel(Parcel in) {
      return new CpuUtilizationLog(in);
    }
    public CpuUtilizationLog[] newArray(int size) {
      return new CpuUtilizationLog[size];
    }
  };

  public String toCsv() {
    return ToStringBuilder.reflectionToString(this, new CsvSimpleToStringStyle());
  }
  public String toCsvTitle() {
    return ToStringBuilder.reflectionToString(this, new CsvColumnTitleToStringStyle());
  }
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getYyyymmdd() {
    return yyyymmdd;
  }
  public void setYyyymmdd(String yyyymmdd) {
    this.yyyymmdd = yyyymmdd;
  }
  public float getUser() {
    return user;
  }
  public void setUser(float user) {
    this.user = user;
  }
  public float getNice() {
    return nice;
  }
  public void setNice(float nice) {
    this.nice = nice;
  }
  public float getSystem() {
    return system;
  }
  public void setSystem(float system) {
    this.system = system;
  }
  public float getIdle() {
    return idle;
  }
  public void setIdle(float idle) {
    this.idle = idle;
  }
  public long getCreatedOnLong() {
    return createdOnLong;
  }
  public void setCreatedOnLong(long createdOnLong) {
    this.createdOnLong = createdOnLong;
  }
  public Timestamp getCreatedOn() {
    return createdOn;
  }
  public void setCreatedOn(Timestamp createdOn) {
    this.createdOn = createdOn;
  }
}
