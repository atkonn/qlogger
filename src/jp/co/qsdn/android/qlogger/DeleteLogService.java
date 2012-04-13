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
import jp.co.qsdn.android.qlogger.provider.ErrorLogProvider;
import jp.co.qsdn.android.qlogger.provider.RebootLogProvider;
import jp.co.qsdn.android.qlogger.provider.ScreenDailyLogProvider;
import jp.co.qsdn.android.qlogger.provider.ScreenHourlyLogProvider;
import jp.co.qsdn.android.qlogger.provider.ScreenLogProvider;
import jp.co.qsdn.android.qlogger.provider.ScreenMonthlyLogProvider;
import jp.co.qsdn.android.qlogger.provider.LoadAvgLogProvider;
import jp.co.qsdn.android.qlogger.provider.CpuUtilizationLogProvider;
import jp.co.qsdn.android.qlogger.provider.MemUtilizationLogProvider;
import jp.co.qsdn.android.qlogger.provider.NetStatLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.ActivityRebootLog;


public class DeleteLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = DeleteLogService.class.getCanonicalName();
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
        long oneDay = 24 * 60 * 60 * 1000;
        long oneYear = oneDay * 365;

        Context context = getApplicationContext();
    
        long errorLogDeleteTime          = nowTime - ((long)Prefs.getInstance(context).getErrorLogSetting__RetentionPeriod()         * oneDay);
        long rebootLogDeleteTime         = nowTime - ((long)Prefs.getInstance(context).getRebootLogSetting__RetentionPeriod()        * oneDay);
        long batteryLogDeleteTime        = nowTime - ((long)Prefs.getInstance(context).getBatteryLogSetting__RetentionPeriod()       * oneDay);
        long screenLogDeleteTime         = nowTime - ((long)Prefs.getInstance(context).getScreenLogSetting__RetentionPeriod()        * oneDay);
        long screenHourlyLogDeleteTime   = nowTime - ((long)Prefs.getInstance(context).getScreenHourlyLogSetting__RetentionPeriod()  * oneDay);
        long screenDailyLogDeleteTime    = nowTime - ((long)Prefs.getInstance(context).getScreenDailyLogSetting__RetentionPeriod()   * oneDay);
        long screenMonthlyLogDeleteTime  = nowTime - ((long)Prefs.getInstance(context).getScreenMonthlyLogSetting__RetentionPeriod() * oneYear);
        long loadAvgLogDeleteTime        = nowTime - ((long)Prefs.getInstance(context).getLoadAvgLogSetting__RetentionPeriod()       * oneDay);
        long cpuUtilizationLogDeleteTime = nowTime - ((long)Prefs.getInstance(context).getCpuUtilizationLogSetting__RetentionPeriod()* oneDay);
        long memUtilizationLogDeleteTime = nowTime - ((long)Prefs.getInstance(context).getMemUtilizationLogSetting__RetentionPeriod()* oneDay);
        long netStatLogDeleteTime        = nowTime - ((long)Prefs.getInstance(context).getNetStatLogSetting__RetentionPeriod()       * oneDay);
    
        context.getContentResolver().delete(ErrorLogProvider.CONTENT_URI,
            ErrorLogProvider.COLUMN_NAME.TIMESTAMP_LONG + " <= " + errorLogDeleteTime, null);
    
        context.getContentResolver().delete(RebootLogProvider.CONTENT_URI,
            RebootLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + rebootLogDeleteTime, null);
    
        context.getContentResolver().delete(BatteryLogProvider.CONTENT_URI,
            BatteryLogProvider.COLUMN_NAME.TIMESTAMP_LONG + " <= " + batteryLogDeleteTime, null);
    
        context.getContentResolver().delete(ScreenLogProvider.CONTENT_URI,
            ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + screenLogDeleteTime, null);
    
        context.getContentResolver().delete(ScreenHourlyLogProvider.CONTENT_URI,
            ScreenHourlyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + screenHourlyLogDeleteTime , null);
    
        context.getContentResolver().delete(ScreenDailyLogProvider.CONTENT_URI,
            ScreenDailyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + screenDailyLogDeleteTime , null);
    
        context.getContentResolver().delete(ScreenMonthlyLogProvider.CONTENT_URI,
            ScreenMonthlyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + screenMonthlyLogDeleteTime , null);

        context.getContentResolver().delete(LoadAvgLogProvider.CONTENT_URI,
            LoadAvgLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + loadAvgLogDeleteTime , null);

        context.getContentResolver().delete(CpuUtilizationLogProvider.CONTENT_URI,
            CpuUtilizationLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + cpuUtilizationLogDeleteTime , null);

        context.getContentResolver().delete(MemUtilizationLogProvider.CONTENT_URI,
            MemUtilizationLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + memUtilizationLogDeleteTime , null);

        context.getContentResolver().delete(NetStatLogProvider.CONTENT_URI,
            NetStatLogProvider.COLUMN_NAME.CREATED_ON_LONG + " <= " + netStatLogDeleteTime , null);

        if (Constant.DEBUG) Log.v(TAG, "<<< run()");
        waitSeconds(2);
        stopSelf();
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
