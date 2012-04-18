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
package jp.co.qsdn.android.qlogger.commands;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.qsdn.android.qlogger.Constant;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;

public class PsCommand 
  extends AbstractCommand<PsCommand.PsResult>
{
  private final String TAG = getClass().getName();
                                      /* USER         PID         PPID        VSIZE       RSS         WCHAN        PC                        NAME      */
  private static final String pattern = "^([^\\s]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s])\\s+([^\\s]+)$";
  private Pattern regex;
  private String filterString = "logcat";

  public PsCommand() {
    regex = Pattern.compile(pattern);
  }

  protected List<String> getCommandList() {
    return new ArrayList<String>();
  }

  @Override
  protected PsCommand.PsResult filter(String line) {
    Matcher matcher = regex.matcher(StringUtils.chop(line));
    if (matcher.matches()) {
      PsResult result = new PsResult();
      result.setUser(matcher.group(1));
      try {
        result.setPid(Integer.parseInt(matcher.group(2)));
      }
      catch (Exception ex) {}
      try {
        result.setPpid(Integer.parseInt(matcher.group(3)));
      }
      catch (Exception ex) {}
      try {
        result.setName(matcher.group(9));
      }
      catch (Exception ex) {}
      return result;
    }
    else {
     if (Constant.DEBUG) Log.d(TAG, "Unmatch!!!:[" + line + "]");
    }
    return null;
  }

  public class PsResult {
    private String user;
    private int pid;
    private int ppid;
    private String name;
    public String getUser() {
      return user;
    }
    public void setUser(String user) {
      this.user = user;
    }
    public int getPid() {
      return pid;
    }
    public void setPid(int pid) {
      this.pid = pid;
    }
    public int getPpid() {
      return ppid;
    }
    public void setPpid(int ppid) {
      this.ppid = ppid;
    }
    public String getName() {
      return this.name;
    }
    public void setName(String name) {
      this.name = name;
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
      if (!(object instanceof PsResult)) {
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
  
  public String getFilterString() {
    return filterString;
  }
  
  public void setFilterString(String filterString) {
    this.filterString = filterString;
  }


  @Override
  public void run() {
    if (Constant.DEBUG)Log.v(TAG, ">>> run");

    Ps ps = new Ps();
    ArrayList<String> result = new ArrayList<String>();
    try {
      ps.runJni(new String[] {"logcat"}, result);
    }
    catch (Exception ex) {
      Log.e(TAG, "error", ex);
      throw new RuntimeException(ex);
    }

    output.clear();
    for (String line: result) {
      synchronized(output) {
        output.add(filter(line));
      }
    }
    if (Constant.DEBUG) {
      for (String line: result) {
        if (Constant.DEBUG)Log.d(TAG, line);
      }
    }

    if (Constant.DEBUG)Log.v(TAG, "<<< run");
  }
}
