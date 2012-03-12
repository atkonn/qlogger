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


public class QLoggerReceiverService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = QLoggerReceiverService.class.getCanonicalName();
  private BroadcastReceiver receiver;
  private BroadcastReceiver batteryReceiver;

  Runnable command = null;
  
  @Override
  public void onCreate() {
    super.onCreate();

    receiver = new QLoggerReceiver();

    {
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction("android.intent.action.SCREEN_ON");
      intentFilter.addAction("android.intent.action.SCREEN_OFF");
      intentFilter.addAction("android.intent.action.REBOOT");
      intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
      registerReceiver(receiver, intentFilter);
    }
    {
      batteryReceiver = new BatteryChangedReceiver();
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED); 
      registerReceiver(batteryReceiver, intentFilter);
    }
    registerAlarmService();

    command = new Runnable() {
      @Override
      public void run() {
        waitSecond();
        if (! getExecutor().isShutdown()) {
          doExecute(command);
        }
      }
    };


    doExecute(command);
  }
  
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
  }

  
  @Override
  public void onDestroy() {
    try {
      unregisterReceiver(receiver);
    }
    catch (java.lang.IllegalArgumentException ex) {
      // already unregistered.
    }
    try {
      unregisterReceiver(batteryReceiver);
    }
    catch (java.lang.IllegalArgumentException ex) {
      // already unregistered.
    }
    unregisterAlarmService();
    super.onDestroy();
  }
  

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private PendingIntent batteryLogger;
  private PendingIntent screenHourlyLogger;
  private PendingIntent screenDailyLogger;
  private PendingIntent screenMonthlyLogger;
  private PendingIntent loadAvgLogger;
  private PendingIntent cpuUtilizationLogger;
  private PendingIntent memUtilizationLogger;
  private PendingIntent netStatLogger;

  protected void registerAlarmService() {
    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    registerBatteryLogger(alarmManager);
    registerScreenHourlyLogger(alarmManager);
    registerScreenDailyLogger(alarmManager);
    registerScreenMonthlyLogger(alarmManager);
    registerLoadAvgLogger(alarmManager);
    registerCpuUtilization(alarmManager);
    registerMemUtilization(alarmManager);
    registerNetStat(alarmManager);
  }
  protected void unregisterAlarmService() {
    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    unregisterBatteryLogger(alarmManager);
    unregisterScreenHourlyLogger(alarmManager);
    unregisterScreenDailyLogger(alarmManager);
    unregisterScreenMonthlyLogger(alarmManager);
    unregisterLoadAvgLogger(alarmManager);
    unregisterCpuUtilization(alarmManager);
    unregisterMemUtilization(alarmManager);
    unregisterNetStat(alarmManager);
  }

  protected void registerBatteryLogger(AlarmManager alarmManager) {
    Intent batteryLoggerIntent = new Intent(this, BatteryLoggerReceiver.class);
    batteryLogger = PendingIntent.getBroadcast(this, Constant.BATTERY_LOGGER_NUMBER,batteryLoggerIntent, 0);
    long nowTime = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(nowTime);
    int minute = cal.get(Calendar.MINUTE);
    minute = minute + (10 - (minute % 10));
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    long subTime = nowTime - cal.getTimeInMillis();

    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - subTime, Constant.INTERVAL.BATTERY_LOG, batteryLogger);
  }
  protected void unregisterBatteryLogger(AlarmManager alarmManager) {
    if (batteryLogger != null) {
      alarmManager.cancel(batteryLogger);
      batteryLogger = null;
    }
  }

  protected void registerScreenHourlyLogger(AlarmManager alarmManager) {
    Intent screenHourlyLoggerIntent = new Intent(this, ScreenHourlyLoggerReceiver.class);
    screenHourlyLogger = PendingIntent.getBroadcast(this, Constant.SCREENHOURLY_LOGGER_NUMBER, screenHourlyLoggerIntent, 0);

    long nowTime = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(nowTime);
    cal.set(Calendar.MINUTE, 3);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.HOUR_OF_DAY, 1);
    long subTime = nowTime - cal.getTimeInMillis();

    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - subTime, Constant.INTERVAL.SCREEN_HOURLY_LOG, screenHourlyLogger);
  }
  protected void unregisterScreenHourlyLogger(AlarmManager alarmManager) {
    if (screenHourlyLogger != null) {
      alarmManager.cancel(screenHourlyLogger);
      screenHourlyLogger = null;
    }
  }

  protected void registerScreenDailyLogger(AlarmManager alarmManager) {
    Intent screenDailyLoggerIntent = new Intent(this, ScreenDailyLoggerReceiver.class);
    screenDailyLogger = PendingIntent.getBroadcast(this, Constant.SCREENDAILY_LOGGER_NUMBER, screenDailyLoggerIntent, 0);

    long nowTime = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(nowTime);
    cal.set(Calendar.MINUTE, 5);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.HOUR_OF_DAY, 1);
    long subTime = nowTime - cal.getTimeInMillis();

    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - subTime, Constant.INTERVAL.SCREEN_DAILY_LOG, screenDailyLogger);
  }
  protected void unregisterScreenDailyLogger(AlarmManager alarmManager) {
    if (screenDailyLogger != null) {
      alarmManager.cancel(screenDailyLogger);
      screenDailyLogger = null;
    }
  }

  protected void registerScreenMonthlyLogger(AlarmManager alarmManager) {
    Intent screenMonthlyLoggerIntent = new Intent(this, ScreenMonthlyLoggerReceiver.class);
    screenMonthlyLogger = PendingIntent.getBroadcast(this, Constant.SCREENMONTHLY_LOGGER_NUMBER, screenMonthlyLoggerIntent, 0);

    long nowTime = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(nowTime);
    cal.set(Calendar.MINUTE, 7);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.HOUR_OF_DAY, 1);
    long subTime = nowTime - cal.getTimeInMillis();

    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - subTime, Constant.INTERVAL.SCREEN_MONTHLY_LOG, screenMonthlyLogger);
  }
  protected void unregisterScreenMonthlyLogger(AlarmManager alarmManager) {
    if (screenMonthlyLogger != null) {
      alarmManager.cancel(screenMonthlyLogger);
      screenMonthlyLogger = null;
    }
  }
  protected void registerLoadAvgLogger(AlarmManager alarmManager) {
    Intent loadAvgLoggerIntent = new Intent(this, LoadAvgLoggerReceiver.class);
    loadAvgLogger = PendingIntent.getBroadcast(this, Constant.LOAD_AVG_LOGGER_NUMBER, loadAvgLoggerIntent, 0);

    long nowTime = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();

    cal.setTimeInMillis(nowTime);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    long subTime = nowTime - cal.getTimeInMillis();

    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - subTime, Constant.INTERVAL.LOAD_AVG_LOG, loadAvgLogger);
  }
  protected void unregisterLoadAvgLogger(AlarmManager alarmManager) {
    if (loadAvgLogger != null) {
      alarmManager.cancel(loadAvgLogger);
      loadAvgLogger = null;
    }
  }
  protected void registerCpuUtilization(AlarmManager alarmManager) {
    Intent cpuUtilizationLoggerIntent = new Intent(this, CpuUtilizationLoggerReceiver.class);
    cpuUtilizationLogger = PendingIntent.getBroadcast(this, Constant.CPU_UTILIZATION_LOGGER_NUMBER, cpuUtilizationLoggerIntent, 0);

    long nowTime = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();

    cal.setTimeInMillis(nowTime);
    int minute = cal.get(Calendar.MINUTE);
    minute = minute + (5 - (minute % 5));
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    long subTime = nowTime - cal.getTimeInMillis();

    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - subTime, Constant.INTERVAL.CPU_UTILIZATION_LOG, cpuUtilizationLogger);
  }
  protected void unregisterCpuUtilization(AlarmManager alarmManager) {
    if (cpuUtilizationLogger != null) {
      alarmManager.cancel(cpuUtilizationLogger);
      cpuUtilizationLogger = null;
    }
  }
  protected void registerMemUtilization(AlarmManager alarmManager) {
    Intent memUtilizationLoggerIntent = new Intent(this, MemUtilizationLoggerReceiver.class);
    memUtilizationLogger = PendingIntent.getBroadcast(this, Constant.MEM_UTILIZATION_LOGGER_NUMBER, memUtilizationLoggerIntent, 0);

    long nowTime = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();

    cal.setTimeInMillis(nowTime);
    int minute = cal.get(Calendar.MINUTE);
    minute = minute + (5 - (minute % 5));
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    long subTime = nowTime - cal.getTimeInMillis();

    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - subTime, Constant.INTERVAL.MEM_UTILIZATION_LOG, memUtilizationLogger);
  }
  protected void unregisterMemUtilization(AlarmManager alarmManager) {
    if (memUtilizationLogger != null) {
      alarmManager.cancel(memUtilizationLogger);
      memUtilizationLogger = null;
    }
  }
  protected void registerNetStat(AlarmManager alarmManager) {
    Intent netStatLoggerIntent = new Intent(this, NetStatLoggerReceiver.class);
    netStatLogger = PendingIntent.getBroadcast(this, Constant.NET_STAT_LOGGER_NUMBER, netStatLoggerIntent, 0);

    long nowTime = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();

    cal.setTimeInMillis(nowTime);
    int minute = cal.get(Calendar.MINUTE);
    minute = minute + (5 - (minute % 5));
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    long subTime = nowTime - cal.getTimeInMillis();

    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - subTime, Constant.INTERVAL.NET_STAT_LOG, netStatLogger);
  }
  protected void unregisterNetStat(AlarmManager alarmManager) {
    if (netStatLogger != null) {
      alarmManager.cancel(netStatLogger);
      netStatLogger = null;
    }
  }
}

