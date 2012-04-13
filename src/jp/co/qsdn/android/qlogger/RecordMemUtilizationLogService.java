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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.qsdn.android.qlogger.core.MemUtilizationLog;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.MemUtilizationLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;


public class RecordMemUtilizationLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordMemUtilizationLogService.class.getCanonicalName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  private SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyyMMdd");

  private static final String totalPattern = "^MemTotal:\\s+([0-9]+)\\skB$";
  private static final String freePattern = "^MemFree:\\s+([0-9]+)\\skB$";
  private static final String cachePattern = "^Cached:\\s+([0-9]+)\\skB$";

  private static Pattern totalRegex;
  private static Pattern freeRegex;
  private static Pattern cacheRegex;

  static {
    totalRegex = Pattern.compile(totalPattern);
    freeRegex = Pattern.compile(freePattern);
    cacheRegex = Pattern.compile(cachePattern);
  };

  Runnable command = null;
  
  @Override
  public void onCreate() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onCreate");
    super.onCreate();

    command = new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG) Log.d(TAG, ">>> run()");

        MemUtilizationLog memUtilizationLog = getMemUtilization();

        long nowTime = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MemUtilizationLogProvider.COLUMN_NAME.YYYYMMDD,       yyyymmddFormat.format(nowTime));
        contentValues.put(MemUtilizationLogProvider.COLUMN_NAME.TOTAL,          memUtilizationLog.getTotal());
        contentValues.put(MemUtilizationLogProvider.COLUMN_NAME.FREE,           memUtilizationLog.getFree());
        contentValues.put(MemUtilizationLogProvider.COLUMN_NAME.CACHE,          memUtilizationLog.getCached());
        contentValues.put(MemUtilizationLogProvider.COLUMN_NAME.CREATED_ON_LONG,nowTime);
        contentValues.put(MemUtilizationLogProvider.COLUMN_NAME.CREATED_ON,     dateFormat.format(nowTime));
        getContentResolver().insert(MemUtilizationLogProvider.CONTENT_URI, contentValues);
        if (Constant.DEBUG) Log.d(TAG, "<<< run()");
        waitSeconds(2);
        stopSelf(); 
      }
    };


    doExecute(command);
    if (Constant.DEBUG) Log.v(TAG, "<<< onCreate");
  }



  protected MemUtilizationLog getMemUtilization() {
    LineNumberReader lineNumberReader = null;
    MemUtilizationLog memUtilizationLog = null;
    try {
      lineNumberReader = new LineNumberReader(new FileReader(Constant.PROC.MEMINFO));
      memUtilizationLog = new MemUtilizationLog();

      String line  = null;
      int idx = 0;
      while((line = lineNumberReader.readLine()) != null) {
        if (! TextUtils.isEmpty(line)) {
          Matcher matcher = totalRegex.matcher(line);
          if (matcher.matches()) {
            memUtilizationLog.setTotal(Long.parseLong(matcher.group(1))); 
            idx++;
          }
          matcher = freeRegex.matcher(line);
          if (matcher.matches()) {
            memUtilizationLog.setFree(Long.parseLong(matcher.group(1))); 
            idx++;
          }
          matcher = cacheRegex.matcher(line);
          if (matcher.matches()) {
            memUtilizationLog.setCached(Long.parseLong(matcher.group(1))); 
            idx++;
          }
          if (idx >= 3) {
            break;
          }
        }
      }
    }
    catch (Exception ex) {
      Log.e(TAG, "memutilization read failure", ex);
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
    
    return memUtilizationLog;
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
