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

import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;

import android.util.Log;

import java.text.SimpleDateFormat;

import java.util.List;

import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.BatteryLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


public class AbstractExecutableReceiver
  extends BroadcastReceiver {
  private final String TAG = getClass().getName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");

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
  public void waitSecond() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
    }
  }
  public void waitMillis(long millis) {
    try {
      TimeUnit.MILLISECONDS.sleep(millis);
    } catch (InterruptedException e) {
    }
  }
  protected void shutdown() {
    getExecutor().shutdown();
    try {
      if (!getExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
        getExecutor().shutdownNow();
        if (!getExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
          Log.d(TAG,"ExecutorService did not terminate....");
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

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Log.d(TAG, "onReceive:[" + action + "]");
  }
}
