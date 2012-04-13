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

import jp.co.qsdn.android.qlogger.core.ScreenDailyLog;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.ScreenMonthlyLogProvider;
import jp.co.qsdn.android.qlogger.provider.ScreenDailyLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;


public class RecordScreenMonthlyLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordScreenMonthlyLogService.class.getCanonicalName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  private SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyyMMdd");
  private SimpleDateFormat yyyymmFormat = new SimpleDateFormat("yyyyMM");
  private SimpleDateFormat yyyyFormat = new SimpleDateFormat("yyyy");
  private SimpleDateFormat mmFormat = new SimpleDateFormat("MM");


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
        cal.add(Calendar.MONTH, -1);

        long targetTime = cal.getTimeInMillis();

        String yyyymmdd = yyyymmddFormat.format(targetTime);
        String yyyymm   = yyyymmFormat.format(targetTime);
        String yyyy     = yyyyFormat.format(targetTime);
        String mm       = mmFormat.format(targetTime);
        Cursor cur = null;
        try {
          cur = getContentResolver()
                         .query(ScreenMonthlyLogProvider.CONTENT_URI, 
                           ScreenMonthlyLogProvider.PROJECTION,
                           ScreenMonthlyLogProvider.COLUMN_NAME.YYYY + " = ? AND " + ScreenMonthlyLogProvider.COLUMN_NAME.MM + " = ? ",
                           new String[] {
                             yyyy,
                             mm
                           },
                           ScreenMonthlyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
          if (cur.getCount() == 0) {
            try { cur.close(); } catch (Exception ex) {}
            cur = getContentResolver()
                    .query(ScreenDailyLogProvider.CONTENT_URI, 
                           ScreenDailyLogProvider.PROJECTION,
                           ScreenDailyLogProvider.COLUMN_NAME.YYYYMM + " = ? ",
                           new String[] {
                             yyyymm
                           },
                           ScreenDailyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
            
            long sumOfOnTime = 0;
            while (cur.moveToNext()) {
              sumOfOnTime += cur.getLong(ScreenDailyLogProvider.COLUMN_INDEX.ON_TIME);
            }
    
            ContentValues contentValues = new ContentValues();
            contentValues.put(ScreenMonthlyLogProvider.COLUMN_NAME.ON_TIME,        sumOfOnTime / 1000);
            contentValues.put(ScreenMonthlyLogProvider.COLUMN_NAME.YYYY,           yyyy);
            contentValues.put(ScreenMonthlyLogProvider.COLUMN_NAME.MM,             mm);
            contentValues.put(ScreenMonthlyLogProvider.COLUMN_NAME.CREATED_ON_LONG,nowTime);
            contentValues.put(ScreenMonthlyLogProvider.COLUMN_NAME.CREATED_ON,     dateFormat.format(nowTime));
            getContentResolver().insert(ScreenMonthlyLogProvider.CONTENT_URI, contentValues);
          }
        }
        finally {
          if (cur != null) {
            try { cur.close(); } catch (Exception ex) {}
            cur = null;
          }
        }
        if (Constant.DEBUG) Log.d(TAG, "<<< run()");
        waitSeconds(2);
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
