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

public class Ps
{
  private final String TAG = getClass().getName();

  static {
    System.loadLibrary("ps");
  }

  protected boolean threads;
  protected int display_flags;
  protected int pidfilter;
  protected String namefilter;
  public Ps() {
  }

  public Ps(boolean threads, int display_flags, int pidfilter, String namefilter) {
    this.threads = threads;
    this.display_flags = display_flags;
    this.namefilter = namefilter;
    this.pidfilter = pidfilter;
  }

  public native String runJni(String[] argv) throws Exception;
}
