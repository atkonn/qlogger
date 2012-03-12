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

import jp.co.qsdn.android.qlogger.core.LoadAvgLog;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.LoadAvgLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;


public class RecordLoadAvgLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordLoadAvgLogService.class.getCanonicalName();
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

        LoadAvgLog loadAvgLog = getLoadAvg();
        long nowTime = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LoadAvgLogProvider.COLUMN_NAME.YYYYMMDD,       yyyymmddFormat.format(nowTime));
        contentValues.put(LoadAvgLogProvider.COLUMN_NAME.ONE,            loadAvgLog.getOne());
        contentValues.put(LoadAvgLogProvider.COLUMN_NAME.FIVE,           loadAvgLog.getFive());
        contentValues.put(LoadAvgLogProvider.COLUMN_NAME.TEN,            loadAvgLog.getTen());
        contentValues.put(LoadAvgLogProvider.COLUMN_NAME.CREATED_ON_LONG,nowTime);
        contentValues.put(LoadAvgLogProvider.COLUMN_NAME.CREATED_ON,     dateFormat.format(nowTime));
        getContentResolver().insert(LoadAvgLogProvider.CONTENT_URI, contentValues);
        if (Constant.DEBUG) Log.d(TAG, "<<< run()");
        stopSelf(); 
      }
    };


    doExecute(command);
    if (Constant.DEBUG) Log.v(TAG, "<<< onCreate");
  }

  protected LoadAvgLog getLoadAvg() {
    LineNumberReader lineNumberReader = null;
    LoadAvgLog loadAvgLog = null;
    try {
      lineNumberReader = new LineNumberReader(new FileReader(Constant.PROC.LOAD_AVG));
      String s = lineNumberReader.readLine();
      if (! TextUtils.isEmpty(s)) {
        loadAvgLog = new LoadAvgLog();
        
        int first = s.indexOf(' ');
        String one  = s.substring(0,first);
        s = s.substring(first + 1);
        int second = s.indexOf(' ');
        String five  = s.substring(0,second);
        s = s.substring(second + 1);
        int third = s.indexOf(' ');
        String ten   = s.substring(0,third);

        loadAvgLog.setOne(Float.parseFloat(one));
        loadAvgLog.setFive(Float.parseFloat(five));
        loadAvgLog.setTen(Float.parseFloat(ten));
      }
    }
    catch (Exception ex) {
      Log.e(TAG, "loadavg read failure", ex);
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
    
    return loadAvgLog;
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
