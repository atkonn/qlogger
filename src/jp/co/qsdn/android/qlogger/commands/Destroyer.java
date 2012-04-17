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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jp.co.qsdn.android.qlogger.Constant;


public class Destroyer
  extends TimerTask 
{
  public final String TAG = getClass().getName();
  private Process process;

  public Destroyer(Process process) {
    setProcess(process);
  }

  public void waitAndDestroy() {
    if (Constant.DEBUG)Log.v(TAG, ">>> waitAndDestroy");
    Timer timer = new Timer("destroyer timer");
    timer.schedule(this, 5000L);
    for (;;) {
      try {
        getProcess().waitFor();
        break;
      }
      catch (InterruptedException ex) {
        Log.w(TAG, "", ex);
      }
    }
    timer.cancel();
    if (Constant.DEBUG)Log.v(TAG, "<<< waitAndDestroy");
  }
  
  public Process getProcess() {
    return process;
  }
  
  public void setProcess(Process process) {
    this.process = process;
  }

  @Override
  public void run() {
    getProcess().destroy();
  }
}
