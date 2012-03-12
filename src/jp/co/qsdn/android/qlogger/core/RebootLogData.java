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

public class RebootLogData {
  long id;
  long rebootLogId;
  String log;
  
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
   * Get rebootLogId.
   *
   * @return rebootLogId as long.
   */
  public long getRebootLogId() {
    return rebootLogId;
  }
  
  /**
   * Set rebootLogId.
   *
   * @param rebootLogId the value to set.
   */
  public void setRebootLogId(long rebootLogId) {
    this.rebootLogId = rebootLogId;
  }
  
  /**
   * Get log.
   *
   * @return log as String.
   */
  public String getLog() {
    return log;
  }
  
  /**
   * Set log.
   *
   * @param log the value to set.
   */
  public void setLog(String log) {
    this.log = log;
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
    if (!(object instanceof RebootLogData)) {
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
}
