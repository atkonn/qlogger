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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;

import android.net.Uri;

import android.os.IBinder;
import android.os.PowerManager;

import android.text.TextUtils;

import android.util.Log;

import java.io.FileReader;
import java.io.LineNumberReader;

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

import jp.co.qsdn.android.qlogger.core.CpuUtilizationLog;
import jp.co.qsdn.android.qlogger.core.TempCpuUtilizationLog;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.CpuUtilizationLogProvider;
import jp.co.qsdn.android.qlogger.provider.TempCpuUtilizationLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;


public class RecordCpuUtilizationLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordCpuUtilizationLogService.class.getCanonicalName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  private SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyyMMdd");


  Runnable command = null;
  
  @Override
  public void onCreate() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onCreate");
    super.onCreate();

    command = new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG) Log.d(TAG, ">>> run()");

        TempCpuUtilizationLog tempCpuUtilizationLog = getTempCpuUtilizationLog();
        CpuUtilizationLog cpuUtilizationLog = getCpuUtilization();

        if (tempCpuUtilizationLog == null) {
          long nowTime = System.currentTimeMillis();
          ContentValues contentValues = new ContentValues();
          contentValues.put(TempCpuUtilizationLogProvider.COLUMN_NAME.USER,           cpuUtilizationLog.getUser());
          contentValues.put(TempCpuUtilizationLogProvider.COLUMN_NAME.NICE,           cpuUtilizationLog.getNice());
          contentValues.put(TempCpuUtilizationLogProvider.COLUMN_NAME.SYSTEM,         cpuUtilizationLog.getSystem());
          contentValues.put(TempCpuUtilizationLogProvider.COLUMN_NAME.IDLE,           cpuUtilizationLog.getIdle());
          getContentResolver().insert(TempCpuUtilizationLogProvider.CONTENT_URI, contentValues);
        }
        else {
          ContentValues contentValues = new ContentValues();
          contentValues.put(TempCpuUtilizationLogProvider.COLUMN_NAME.USER,           cpuUtilizationLog.getUser());
          contentValues.put(TempCpuUtilizationLogProvider.COLUMN_NAME.NICE,           cpuUtilizationLog.getNice());
          contentValues.put(TempCpuUtilizationLogProvider.COLUMN_NAME.SYSTEM,         cpuUtilizationLog.getSystem());
          contentValues.put(TempCpuUtilizationLogProvider.COLUMN_NAME.IDLE,           cpuUtilizationLog.getIdle());
          Uri uri = ContentUris.withAppendedId(TempCpuUtilizationLogProvider.CONTENT_URI, tempCpuUtilizationLog.getId());
          getContentResolver().update(uri, contentValues, null, null);

          long nowTime = System.currentTimeMillis();
          float user = cpuUtilizationLog.getUser() - tempCpuUtilizationLog.getUser();
          float nice = cpuUtilizationLog.getNice() - tempCpuUtilizationLog.getNice();
          float system = cpuUtilizationLog.getSystem() - tempCpuUtilizationLog.getSystem();
          float idle = cpuUtilizationLog.getIdle() - tempCpuUtilizationLog.getIdle();
          float sums = user + nice + system + idle;

          contentValues = new ContentValues();
          contentValues.put(CpuUtilizationLogProvider.COLUMN_NAME.YYYYMMDD,       yyyymmddFormat.format(nowTime));
          contentValues.put(CpuUtilizationLogProvider.COLUMN_NAME.USER,           user / sums * 100f);
          contentValues.put(CpuUtilizationLogProvider.COLUMN_NAME.NICE,           nice / sums * 100f);
          contentValues.put(CpuUtilizationLogProvider.COLUMN_NAME.SYSTEM,         system / sums * 100f);
          contentValues.put(CpuUtilizationLogProvider.COLUMN_NAME.IDLE,           idle / sums * 100f);
          contentValues.put(CpuUtilizationLogProvider.COLUMN_NAME.CREATED_ON_LONG,nowTime);
          contentValues.put(CpuUtilizationLogProvider.COLUMN_NAME.CREATED_ON,     dateFormat.format(nowTime));
          getContentResolver().insert(CpuUtilizationLogProvider.CONTENT_URI, contentValues);
        }
        if (Constant.DEBUG) Log.d(TAG, "<<< run()");
        waitSeconds(2);
        stopSelf(); 
      }
    };


    doExecute(command);
    if (Constant.DEBUG) Log.v(TAG, "<<< onCreate");
  }

  protected CpuUtilizationLog getCpuUtilization() {
    LineNumberReader lineNumberReader = null;
    CpuUtilizationLog cpuUtilizationLog = null;
    try {
      lineNumberReader = new LineNumberReader(new FileReader(Constant.PROC.STAT));
      String s = lineNumberReader.readLine();
      if (! TextUtils.isEmpty(s)) {
        cpuUtilizationLog = new CpuUtilizationLog();
        
        int first = s.indexOf(' ');
        s = s.substring(first + 2);
        int second = s.indexOf(' ');
        String user  = s.substring(0,second);
        s = s.substring(second + 1);
        int third = s.indexOf(' ');
        String nice   = s.substring(0,third);
        s = s.substring(third + 1);
        int forth = s.indexOf(' ');
        String system  = s.substring(0,forth);
        s = s.substring(forth + 1);
        int fifth = s.indexOf(' ');
        String idle    = s.substring(0,fifth);

        cpuUtilizationLog.setUser(Float.parseFloat(user));
        cpuUtilizationLog.setNice(Float.parseFloat(nice));
        cpuUtilizationLog.setSystem(Float.parseFloat(system));
        cpuUtilizationLog.setIdle(Float.parseFloat(idle));
      }
    }
    catch (Exception ex) {
      Log.e(TAG, "cpuutilization read failure", ex);
    }
    finally {
      if (lineNumberReader != null) {
        try {
          lineNumberReader.close();
          lineNumberReader = null;
        }
        catch (Exception ex) {}     
      }
    }
    
    return cpuUtilizationLog;
  }
  protected TempCpuUtilizationLog getTempCpuUtilizationLog() {
    Cursor cur = null;
    try {
      cur = getContentResolver()
                     .query(TempCpuUtilizationLogProvider.CONTENT_URI,
                       TempCpuUtilizationLogProvider.PROJECTION,
                       null,
                       null,
                       null);
  
      
      if (cur.moveToNext()) {
        TempCpuUtilizationLog temp = new TempCpuUtilizationLog();
        temp.setId(cur.getLong(TempCpuUtilizationLogProvider.COLUMN_INDEX._ID));
        temp.setUser(cur.getFloat(TempCpuUtilizationLogProvider.COLUMN_INDEX.USER));
        temp.setNice(cur.getFloat(TempCpuUtilizationLogProvider.COLUMN_INDEX.NICE));
        temp.setSystem(cur.getFloat(TempCpuUtilizationLogProvider.COLUMN_INDEX.SYSTEM));
        temp.setIdle(cur.getFloat(TempCpuUtilizationLogProvider.COLUMN_INDEX.IDLE));
        return temp;
      }
    }
    finally {
      if (cur != null) {
        try { cur.close(); } catch (Exception ex) {}
        cur = null;
      }
    }
    return null;
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
