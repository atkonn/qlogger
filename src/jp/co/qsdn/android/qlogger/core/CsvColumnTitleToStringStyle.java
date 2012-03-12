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

public class CsvColumnTitleToStringStyle
  extends ToStringStyle 
{
  private static final long serialVersionUID = 1L;

  public CsvColumnTitleToStringStyle() {
    super();
    this.setUseClassName(false);
    this.setUseIdentityHashCode(false);
    this.setUseFieldNames(true);
    this.setContentStart("");
    this.setContentEnd("");
    this.setFieldNameValueSeparator("");
  }
  @Override
  protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
  }
  @Override
  protected void appendFieldStart(StringBuffer buffer, String fieldName) {
    if (fieldName != null) {
      buffer.append(StringEscapeUtils.escapeCsv(fieldName));
    }
  }
}
