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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

public class CsvSimpleToStringStyle
  extends ToStringStyle 
{
  private static final long serialVersionUID = 1L;
  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  public CsvSimpleToStringStyle() {
    super();
    this.setUseClassName(false);
    this.setUseIdentityHashCode(false);
    this.setUseFieldNames(false);
    this.setContentStart("");
    this.setContentEnd("");
  }
  protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
    if (value instanceof String) {
      value = StringEscapeUtils.escapeCsv((String)value);
    }
    else
    if (value instanceof Timestamp) {
      value = dateFormat.format(((Timestamp)value).getTime());
      value = StringEscapeUtils.escapeCsv((String)value);
    }
    buffer.append(value);
  }
}
