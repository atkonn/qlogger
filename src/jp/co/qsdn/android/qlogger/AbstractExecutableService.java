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
package jp.co.qsdn.android.qlogger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.IBinder;
import android.os.SystemClock;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


public abstract class AbstractExecutableService
  extends Service 
{
  private final String TAG = getClass().getName();

  private ExecutorService executor = null;
  protected ExecutorService getExecutor() {
    if (executor == null) {
      executor = Executors.newSingleThreadExecutor();
    }
    return executor;
  }
  protected void doExecute(Runnable command) {
    if (command == null) {
      return;
    }
    while(true) {
      try {
        getExecutor().execute(command);
      }
      catch (RejectedExecutionException e) {
        if (getExecutor().isShutdown()) {
          // ignore
        }
        else {
          Log.e(TAG, "command execute failure", e);
          waitSecond();
          System.gc();
          continue;
        }
      }
      break;
    }
  }
  protected void waitSecond() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
    }
  }
  protected void waitSeconds(int sec) {
    try {
      TimeUnit.SECONDS.sleep(sec);
    } catch (InterruptedException e) {
    }
  }
  Runnable command = null;
  
  
  @Override
  public void onDestroy() {
    if (getExecutor() != null) {
      doExecute(new Runnable() {
        @Override
        public void run() {
          shutdown();
        }
      });
    }
    super.onDestroy();
  }

  protected void shutdown() {
    getExecutor().shutdown();
    try {
      if (!getExecutor().awaitTermination(Constant.SECONDS.EXECUTOR_TERMINATE, TimeUnit.SECONDS)) {
        getExecutor().shutdownNow();
        if (!getExecutor().awaitTermination(Constant.SECONDS.EXECUTOR_TERMINATE,TimeUnit.SECONDS)) {
          if (Constant.DEBUG)Log.d(TAG,"ExecutorService did not terminate....");
          getExecutor().shutdownNow();
          Thread.currentThread().interrupt();
        }
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
    executor = null;
  }
}
