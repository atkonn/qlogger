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

public class NetStatLog
  implements Parcelable
{
  long id;
  String yyyymmdd;
  long recvByte;
  long recvPacket;
  long recvErr;
  long sendByte;
  long sendPacket;
  long sendErr;
  long createdOnLong;
  Timestamp createdOn;

  public NetStatLog() {
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
    if (!(object instanceof NetStatLog)) {
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
    out.writeLong(getRecvByte());
    out.writeLong(getRecvPacket());
    out.writeLong(getRecvErr());
    out.writeLong(getSendByte());
    out.writeLong(getSendPacket());
    out.writeLong(getSendErr());
    out.writeLong(getCreatedOnLong());
    out.writeLong(getCreatedOn().getTime());
  }
  private NetStatLog(Parcel in) {
    setId(in.readLong());
    setYyyymmdd(in.readString());
    setRecvByte(in.readLong());
    setRecvPacket(in.readLong());
    setRecvErr(in.readLong());
    setSendByte(in.readLong());
    setSendPacket(in.readLong());
    setSendErr(in.readLong());
    setCreatedOnLong(in.readLong());
    setCreatedOn(new Timestamp(in.readLong()));
  }

  public static final Parcelable.Creator<NetStatLog> CREATOR = new Parcelable.Creator<NetStatLog>() {
    public NetStatLog createFromParcel(Parcel in) {
      return new NetStatLog(in);
    }
    public NetStatLog[] newArray(int size) {
      return new NetStatLog[size];
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
  public long getRecvByte() {
    return recvByte;
  }
  public void setRecvByte(long recvByte) {
    this.recvByte = recvByte;
  }
  public long getRecvPacket() {
    return recvPacket;
  }
  public void setRecvPacket(long recvPacket) {
    this.recvPacket = recvPacket;
  }
  public long getRecvErr() {
    return recvErr;
  }
  public void setRecvErr(long recvErr) {
    this.recvErr = recvErr;
  }
  public long getSendByte() {
    return sendByte;
  }
  public void setSendByte(long sendByte) {
    this.sendByte = sendByte;
  }
  public long getSendPacket() {
    return sendPacket;
  }
  public void setSendPacket(long sendPacket) {
    this.sendPacket = sendPacket;
  }
  public long getSendErr() {
    return sendErr;
  }
  public void setSendErr(long sendErr) {
    this.sendErr = sendErr;
  }
}
