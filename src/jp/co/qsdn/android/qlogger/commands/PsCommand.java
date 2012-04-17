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
    List<String> command = new ArrayList<String>();
    command.add("/system/bin/toolbox");
    command.add("ps");
    if (getFilterString() != null) {
      command.add(getFilterString());
    }
    return command;
  }

  @Override
  protected PsCommand.PsResult filter(String line) {
    Matcher matcher = regex.matcher(line);
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
  }
  
  public String getFilterString() {
    return filterString;
  }
  
  public void setFilterString(String filterString) {
    this.filterString = filterString;
  }
}
