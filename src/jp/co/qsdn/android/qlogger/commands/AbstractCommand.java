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

public abstract class AbstractCommand<T> {
  private Process process;
  private List<T> output = new ArrayList<T>();
  public final String TAG = getClass().getName();
  public static final int BUFSZ = 4*1024;

  public Process getProcess() {
    return process;
  }
  public void setProcess(Process process) {
    this.process = process;
  }
  public List<T> getOutput() {
    return output;
  }
  protected abstract List<String> getCommandList();
  protected abstract T filter(String line);
  protected void waitSeconds(int sec) {
    try {
      TimeUnit.SECONDS.sleep(sec);
    } catch (InterruptedException e) {
    }
  }

  public void run() {
    if (Constant.DEBUG)Log.v(TAG, ">>> run");
    try {
      setProcess(new ProcessBuilder(getCommandList()).start());
    }
    catch (IOException ex) {
      Log.e(TAG, "process build failure", ex);
      return;
    }
    waitSeconds(3);

    final Thread readerThread = new Thread() {
      @Override
      public void run() {
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new InputStreamReader(getProcess().getInputStream()), BUFSZ);
          String line = null;
          while((line = reader.readLine()) != null) {
            synchronized(output) {
              output.add(filter(line));
            }
          }
        }
        catch (IOException ex) {
          Log.e(TAG, "failure", ex);
        }
        finally {
          if (reader != null) {
            try { reader.close(); } catch (Exception ex) {}
          }
        }
      }
    };
    Thread interruptor = new Thread() {
      @Override
      public void run() {
        readerThread.interrupt();
      }
    };
    readerThread.start();
    try {
      do {
        readerThread.join(Constant.BUFFERED_READER.WAIT_MILLISECONDS);
        if (!readerThread.isAlive()) break;
        interruptor.start();
        interruptor.join(Constant.INTERRUPTOR.WAIT_MILLISECONDS);
        readerThread.join(Constant.INTERRUPTOR.WAIT_MILLISECONDS);
        if (! readerThread.isAlive())break;
  
        throw new RuntimeException("reader interrupt failure");
      }
      while(false);
    }
    catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
    finally {
      Destroyer destroyer = new Destroyer(getProcess());
      destroyer.waitAndDestroy();
    }
    if (Constant.DEBUG)Log.v(TAG, "<<< run");
  }
}
