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

import java.sql.Timestamp;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ErrorLog {
  long id;
  Timestamp timestamp;
  long timestampLong;
  String tag;
  String message;
  String stacktrace;
  long unread;
  long hold;
  Timestamp createdOn;

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
    if (!(object instanceof ErrorLog)) {
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
  public String getTag() {
    return tag;
  }
  public void setTag(String tag) {
    this.tag = tag;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public String getStacktrace() {
    return stacktrace;
  }
  public void setStacktrace(String stacktrace) {
    this.stacktrace = stacktrace;
  }
  public Timestamp getCreatedOn() {
    return createdOn;
  }
  public void setCreatedOn(Timestamp createdOn) {
    this.createdOn = createdOn;
  }
  public long getTimestampLong() {
    return timestampLong;
  }
  public void setTimestampLong(long timestampLong) {
    this.timestampLong = timestampLong;
  }
  
  public long getUnread() {
    return unread;
  }
  public void setUnread(long unread) {
    this.unread = unread;
  }
  public long getHold() {
    return hold;
  }
  public void setHold(long hold) {
    this.hold = hold;
  }
}
