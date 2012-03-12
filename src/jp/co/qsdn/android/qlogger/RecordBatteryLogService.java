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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;


import android.os.IBinder;

import android.util.Log;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.BatteryLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.ActivityRebootLog;


public class RecordBatteryLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordBatteryLogService.class.getCanonicalName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");


  Runnable command = null;
  
  @Override
  public void onCreate() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onCreate()");
    super.onCreate();

    command = new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG) Log.v(TAG, ">>> run()");
        long nowTime = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BatteryLogProvider.COLUMN_NAME.TIMESTAMP,      dateFormat.format(nowTime));
        contentValues.put(BatteryLogProvider.COLUMN_NAME.TIMESTAMP_LONG, nowTime);
        contentValues.put(BatteryLogProvider.COLUMN_NAME.LEVEL,          Prefs.getInstance(getApplicationContext()).getBatteryLog__NowLevel());
        getContentResolver().insert(BatteryLogProvider.CONTENT_URI, contentValues);
        stopSelf();
        if (Constant.DEBUG) Log.v(TAG, "<<< run()");
      }
    };


    doExecute(command);
    if (Constant.DEBUG) Log.v(TAG, "<<< onCreate()");
  }

  
  public void onStart(Intent intent, int startId) {
    if (Constant.DEBUG) Log.v(TAG, ">>> onStart");
    super.onStart(intent, startId);
    if (Constant.DEBUG) Log.v(TAG, "<<< onStart");
  }

  
  @Override
  public void onDestroy() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onDestroy");
    super.onDestroy();
    if (Constant.DEBUG) Log.v(TAG, "<<< onDestroy");
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
