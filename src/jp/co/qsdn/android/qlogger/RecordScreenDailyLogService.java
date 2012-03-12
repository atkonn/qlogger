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

import android.database.Cursor;

import android.os.IBinder;
import android.os.PowerManager;

import android.util.Log;

import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import jp.co.qsdn.android.qlogger.core.ScreenHourlyLog;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.ScreenDailyLogProvider;
import jp.co.qsdn.android.qlogger.provider.ScreenHourlyLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;


public class RecordScreenDailyLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordScreenDailyLogService.class.getCanonicalName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  private SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyyMMdd");
  private SimpleDateFormat yyyymmFormat = new SimpleDateFormat("yyyyMM");
  private SimpleDateFormat ddFormat = new SimpleDateFormat("dd");


  Runnable command = null;
  
  @Override
  public void onCreate() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onCreate");
    super.onCreate();

    command = new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG) Log.d(TAG, ">>> run()");
        long nowTime = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(nowTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, -1);

        long targetTime = cal.getTimeInMillis();

        String yyyymmdd = yyyymmddFormat.format(targetTime);
        String yyyymm   = yyyymmFormat.format(targetTime);
        String dd       = ddFormat.format(targetTime);
        Cursor cur = null;
        try {
          cur = getContentResolver()
                         .query(ScreenDailyLogProvider.CONTENT_URI, 
                           ScreenDailyLogProvider.PROJECTION,
                           ScreenDailyLogProvider.COLUMN_NAME.YYYYMM + " = ? AND " + ScreenDailyLogProvider.COLUMN_NAME.DD + " = ? ",
                           new String[] {
                             yyyymm,
                             dd
                           },
                           ScreenDailyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
          if (cur.getCount() == 0) {
            try {cur.close();} catch (Exception ex) {}
            cur = getContentResolver()
                    .query(ScreenHourlyLogProvider.CONTENT_URI, 
                           ScreenHourlyLogProvider.PROJECTION,
                           ScreenHourlyLogProvider.COLUMN_NAME.YYYYMMDD + " = ? ",
                           new String[] {
                             yyyymmdd
                           },
                           ScreenHourlyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
            
            long sumOfOnTime = 0;
            while (cur.moveToNext()) {
              sumOfOnTime += cur.getLong(ScreenHourlyLogProvider.COLUMN_INDEX.ON_TIME);
            }
    
            ContentValues contentValues = new ContentValues();
            contentValues.put(ScreenDailyLogProvider.COLUMN_NAME.ON_TIME,        sumOfOnTime);
            contentValues.put(ScreenDailyLogProvider.COLUMN_NAME.YYYYMM,         yyyymm);
            contentValues.put(ScreenDailyLogProvider.COLUMN_NAME.DD,             dd);
            contentValues.put(ScreenDailyLogProvider.COLUMN_NAME.CREATED_ON_LONG,nowTime);
            contentValues.put(ScreenDailyLogProvider.COLUMN_NAME.CREATED_ON,     dateFormat.format(nowTime));
            getContentResolver().insert(ScreenDailyLogProvider.CONTENT_URI, contentValues);
          }
        }
        finally {
          if (cur != null) {
            try { cur.close(); } catch (Exception ex) {}
            cur = null;
          }
        }
        if (Constant.DEBUG) Log.d(TAG, "<<< run()");
        stopSelf(); 
      }
    };


    doExecute(command);
    if (Constant.DEBUG) Log.v(TAG, "<<< onCreate");
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
