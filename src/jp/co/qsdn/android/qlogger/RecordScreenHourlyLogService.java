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

import jp.co.qsdn.android.qlogger.core.ScreenLog;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.ScreenHourlyLogProvider;
import jp.co.qsdn.android.qlogger.provider.ScreenLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;


public class RecordScreenHourlyLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordScreenHourlyLogService.class.getCanonicalName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  private SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyyMMdd");
  private SimpleDateFormat hh24Format = new SimpleDateFormat("HH");


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
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();
        Cursor cur = null;
        List<ScreenLog> list = new ArrayList<ScreenLog>();
        try {
          cur = getContentResolver()
                       .query(ScreenLogProvider.CONTENT_URI,
                         ScreenLogProvider.PROJECTION,
                         ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG + " >= ? AND " + ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG + " < ? ",
                         new String[] {
                           "" + startTime,
                           "" + endTime
                         },
                         ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG + " asc");
          while (cur.moveToNext()) {
            ScreenLog screenLog = new ScreenLog();
            screenLog.setId(cur.getLong(ScreenLogProvider.COLUMN_INDEX._ID));
            screenLog.setActionName(cur.getString(ScreenLogProvider.COLUMN_INDEX.ACTION_NAME));
            screenLog.setOnTime(cur.getLong(ScreenLogProvider.COLUMN_INDEX.ON_TIME));
            screenLog.setYyyymmdd(cur.getString(ScreenLogProvider.COLUMN_INDEX.YYYYMMDD));
            screenLog.setCreatedOnLong(cur.getLong(ScreenLogProvider.COLUMN_INDEX.CREATED_ON_LONG));
            screenLog.setCreatedOn(Timestamp.valueOf(cur.getString(ScreenLogProvider.COLUMN_INDEX.CREATED_ON)));
            list.add(screenLog);
          }
        }
        finally {
          if (cur != null) {
            try {cur.close();}catch (Exception ex){}
            cur = null;
          }
        }

        int idx = 0;
        int listSize = list.size();
        long sumOfOnTime = 0;
        ScreenLog prevScreenOnLog = null;
        for (ScreenLog screenLog: list) {
          if (idx == 0 && Constant.ACTION.SCREEN_OFF.equals(screenLog.getActionName())) {
            sumOfOnTime += screenLog.getCreatedOnLong() - startTime; 
          }
          else if (idx == listSize - 1 && Constant.ACTION.SCREEN_ON.equals(screenLog.getActionName())) {
            sumOfOnTime += endTime - screenLog.getCreatedOnLong(); 
          }
          else {
            if (Constant.ACTION.SCREEN_ON.equals(screenLog.getActionName())) {
              prevScreenOnLog = screenLog;
            }
            else {
              if (prevScreenOnLog != null) {
                sumOfOnTime += screenLog.getCreatedOnLong() - prevScreenOnLog.getCreatedOnLong();
              }
              else {
                // ignore
              }
            }
          }

          idx++;
        }
        if (idx == 0) {
          long afterStartTime = endTime; 
          Cursor curAfter = null;
          try {
            curAfter = getContentResolver()
                           .query(ScreenLogProvider.CONTENT_URI,
                             ScreenLogProvider.PROJECTION,
                             ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG + " >= ? ",
                             new String[] {
                               "" + afterStartTime
                             },
                             ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG + " asc LIMIT 1");
            if (curAfter.moveToNext()) {
              ScreenLog screenLog = new ScreenLog();
              screenLog.setId(curAfter.getLong(ScreenLogProvider.COLUMN_INDEX._ID));
              screenLog.setActionName(curAfter.getString(ScreenLogProvider.COLUMN_INDEX.ACTION_NAME));
              screenLog.setOnTime(curAfter.getLong(ScreenLogProvider.COLUMN_INDEX.ON_TIME));
              screenLog.setYyyymmdd(curAfter.getString(ScreenLogProvider.COLUMN_INDEX.YYYYMMDD));
              screenLog.setCreatedOnLong(curAfter.getLong(ScreenLogProvider.COLUMN_INDEX.CREATED_ON_LONG));
              screenLog.setCreatedOn(Timestamp.valueOf(curAfter.getString(ScreenLogProvider.COLUMN_INDEX.CREATED_ON)));
              if (Constant.ACTION.SCREEN_OFF.equals(screenLog.getActionName())) {
                sumOfOnTime = 60 * 60 * 1000;
              }
            }
            else {
              PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
              if (powerManager.isScreenOn()) {
                sumOfOnTime = 60 * 60 * 1000;
              }
            }
          }
          finally {
            if (curAfter != null) {
              try {curAfter.close();}catch(Exception ex){}
              curAfter = null;
            }
          }
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ScreenHourlyLogProvider.COLUMN_NAME.ON_TIME,        sumOfOnTime);
        contentValues.put(ScreenHourlyLogProvider.COLUMN_NAME.YYYYMMDD,       yyyymmddFormat.format(startTime));
        contentValues.put(ScreenHourlyLogProvider.COLUMN_NAME.HH24,           hh24Format.format(startTime));
        contentValues.put(ScreenHourlyLogProvider.COLUMN_NAME.CREATED_ON_LONG,nowTime);
        contentValues.put(ScreenHourlyLogProvider.COLUMN_NAME.CREATED_ON,     dateFormat.format(nowTime));
        getContentResolver().insert(ScreenHourlyLogProvider.CONTENT_URI, contentValues);
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
