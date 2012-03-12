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

public class RebootLog {
  long id;
  String actionName;
  long downTime;
  long createdOnLong;
  Timestamp createdOn;
  
  /**
   * Get id.
   *
   * @return id as long.
   */
  public long getId() {
    return id;
  }
  
  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(long id) {
    this.id = id;
  }
  
  /**
   * Get actionName.
   *
   * @return actionName as String.
   */
  public String getActionName() {
    return actionName;
  }
  
  /**
   * Set actionName.
   *
   * @param actionName the value to set.
   */
  public void setActionName(String actionName) {
    this.actionName = actionName;
  }
  
  /**
   * Get createdOn.
   *
   * @return createdOn as Timestamp.
   */
  public Timestamp getCreatedOn() {
    return createdOn;
  }
  
  /**
   * Set createdOn.
   *
   * @param createdOn the value to set.
   */
  public void setCreatedOn(Timestamp createdOn) {
    this.createdOn = createdOn;
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
    if (!(object instanceof RebootLog)) {
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
  
  public long getDownTime() {
    return downTime;
  }
  public void setDownTime(long downTime) {
    this.downTime = downTime;
  }
  public long getCreatedOnLong() {
    return createdOnLong;
  }
  public void setCreatedOnLong(long createdOnLong) {
    this.createdOnLong = createdOnLong;
  }

  public String toCsv() {
    return ToStringBuilder.reflectionToString(this, new CsvSimpleToStringStyle());
  }
  public String toCsvTitle() {
    return ToStringBuilder.reflectionToString(this, new CsvColumnTitleToStringStyle());
  }
}
