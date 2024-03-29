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

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;

import android.net.Uri;

import android.os.IBinder;
import android.os.SystemClock;

import android.text.TextUtils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import jp.co.qsdn.android.qlogger.commands.Destroyer;
import jp.co.qsdn.android.qlogger.commands.PsCommand;
import jp.co.qsdn.android.qlogger.core.ErrorLog;
import jp.co.qsdn.android.qlogger.core.LogLine;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.ErrorLogProvider;



public class LogcatService
  extends Service 
{
  private static final int BUFSZ = 1024;
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = LogcatService.class.getCanonicalName();

  private IBinder binder = new LogcatServiceBinder();
  private List<LogLine> logdata = new ArrayList<LogLine>();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  
  static final Object logcatProcessMutex = new Object();
  Process logcatProcess = null;
  BufferedReader logcatReader = null;
  Runnable setupCommand = null;
  Runnable mainCommand = null;
  Runnable shutdownCommand = null;
  Runnable restartCommand = null;
  Runnable resetRestartFlagCommand = null;
  private boolean restartFlag = false;
  private ErrorLog lastDbData = null;

  private ExecutorService executor = null;
  private ExecutorService getExecutor() {
    if (executor == null) {
      executor = Executors.newSingleThreadExecutor();
    }
    return executor;
  }
  private void doExecute(Runnable command) {
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
  private void waitSeconds(long sec) {
    try {
      TimeUnit.SECONDS.sleep(sec);
    } catch (InterruptedException e) {
    }
  }
  private void waitSecond() {
    waitSeconds(1);
  }

  private Uri lastErrorLog = null;
  private LogLine lastErrorLogLine = null;
  protected LogLine filter(LogLine logLine) {
    if (logLine.getIntLogLevel() >= LogLine.LOG_LEVEL_ERROR) {
      if (lastDbData != null) {
        boolean ignoreFlag = false;
        if (logLine.getTimestamp().getTime() < lastDbData.getTimestampLong()) {
          ignoreFlag = true;
        }
        else
        if (logLine.getTimestamp().getTime() == lastDbData.getTimestampLong()) {
          if (((! TextUtils.isEmpty(lastDbData.getTag()) && lastDbData.getTag().equalsIgnoreCase(logLine.getTag())) ||
               (TextUtils.isEmpty(lastDbData.getTag()) && TextUtils.isEmpty(logLine.getTag()))) &&
              ((! TextUtils.isEmpty(lastDbData.getMessage()) && lastDbData.getMessage().equalsIgnoreCase(logLine.getMessage())) ||
               (TextUtils.isEmpty(lastDbData.getMessage()) && TextUtils.isEmpty(logLine.getMessage())))) {
            ignoreFlag = true;
          }
        }
        if (ignoreFlag) {
          lastDbData = null;
          return logLine;
        }
      }
      if ((logLine.getStackTraceFlag() && lastErrorLog != null) || 
          (lastErrorLogLine != null && lastErrorLogLine.isFatalException())) {
        Cursor cursor = null;
        try {
          cursor = getApplicationContext().getContentResolver().query(
                       lastErrorLog,
                       new String[] { 
                         ErrorLogProvider.COLUMN_NAME.STACKTRACE
                       },
                       null,
                       null,
                       null);
          if (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ErrorLogProvider.COLUMN_NAME.STACKTRACE, cursor.getString(0) + "\n" + logLine.getMessage());
            contentValues.put(ErrorLogProvider.COLUMN_NAME.UNREAD,     1);
            getApplicationContext().getContentResolver().update(lastErrorLog, contentValues, null, null);
          }
        }
        finally {
          if (cursor != null) {
            try { cursor.close(); } catch (Exception ex) {}
            cursor = null;
          }
        }
      }
      else 
      if (logLine.isAnrDataLine() && lastErrorLog != null && lastErrorLogLine != null && lastErrorLogLine.isAnrFirstLine()) {
        Cursor cursor = null;
        try {
          cursor = getApplicationContext().getContentResolver().query(
                       lastErrorLog,
                       new String[] { 
                         ErrorLogProvider.COLUMN_NAME.STACKTRACE
                       },
                       null,
                       null,
                       null);
          if (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ErrorLogProvider.COLUMN_NAME.STACKTRACE, cursor.getString(0) + "\n" + logLine.getMessage());
            contentValues.put(ErrorLogProvider.COLUMN_NAME.UNREAD,     1);
            getApplicationContext().getContentResolver().update(lastErrorLog, contentValues, null, null);
          }
        }
        finally {
          if (cursor != null) {
            try { cursor.close(); } catch (Exception ex) {}
            cursor = null;
          }
        } 
      }
      else 
      if (logLine.isUpdateContactLine() && lastErrorLog != null && lastErrorLogLine != null && lastErrorLogLine.isUpdateContactLine()) {
        Cursor cursor = null;
        try {
          cursor = getApplicationContext().getContentResolver().query(
                       lastErrorLog,
                       new String[] { 
                         ErrorLogProvider.COLUMN_NAME.STACKTRACE
                       },
                       null,
                       null,
                       null);
          if (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ErrorLogProvider.COLUMN_NAME.STACKTRACE, cursor.getString(0) + "\n" + logLine.getMessage());
            contentValues.put(ErrorLogProvider.COLUMN_NAME.UNREAD,     1);
            getApplicationContext().getContentResolver().update(lastErrorLog, contentValues, null, null);
          }
        }
        finally {
          if (cursor != null) {
            try { cursor.close(); } catch (Exception ex) {}
            cursor = null;
          }
        }
      }
      else {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ErrorLogProvider.COLUMN_NAME.TIMESTAMP,       logLine.getTimestamp().toString());
        contentValues.put(ErrorLogProvider.COLUMN_NAME.TIMESTAMP_LONG,  logLine.getTimestamp().getTime());
        contentValues.put(ErrorLogProvider.COLUMN_NAME.TAG,             logLine.getTag());
        contentValues.put(ErrorLogProvider.COLUMN_NAME.MESSAGE,         logLine.getMessage());
        contentValues.put(ErrorLogProvider.COLUMN_NAME.STACKTRACE,      logLine.getMessage());
        contentValues.put(ErrorLogProvider.COLUMN_NAME.UNREAD,          1);
        contentValues.put(ErrorLogProvider.COLUMN_NAME.CREATED_ON,      dateFormat.format(SystemClock.elapsedRealtime()));
        lastErrorLog = getApplicationContext().getContentResolver().insert(ErrorLogProvider.CONTENT_URI, contentValues);
        lastErrorLogLine = logLine;
      }
    }
    else {
      lastErrorLog = null;
      lastErrorLogLine = null;
    }
    return logLine;
  }

  private PendingIntent errorLogDeleter;
  protected void registerAlarmService() {
    Intent deleteReceiverIntent = new Intent(this, LogDeleteReceiver.class);
    errorLogDeleter = PendingIntent.getBroadcast(this, Constant.ERRORLOG_DELETER_NUMBER, deleteReceiverIntent, 0);
    long nowTime = SystemClock.elapsedRealtime();
    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, nowTime, 60 * 60 * 1000, errorLogDeleter);
  }

  @Override
  public void onCreate() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onCreate");
    super.onCreate();

    registerAlarmService();

    setupCommand = new Runnable() {
      @Override
      public void run() {
        if(Constant.DEBUG)Log.d(TAG, "セットアップ開始");
        try {
          int uid = android.os.Process.myUid();
          if (Constant.DEBUG)Log.d(TAG, "uid:[" + uid + "]");
          synchronized (logcatProcessMutex) {
            PsCommand psCommand = new PsCommand();
            psCommand.run();
            for (PsCommand.PsResult psResult: psCommand.getOutput()) {
              if (Constant.DEBUG)Log.d(TAG, "PsResult:[" + psResult + "]");
              if (psResult != null && psResult.getPpid() == 1) {
                if (Constant.DEBUG)Log.d(TAG, "KILL:[" + psResult.getPid() + "]");
                /*===========================================================*/
                /* 親のいないlogcatプロセスで同一UIDのプロセスをkillする.    */
                /* 同一UIDのプロセス以外は殺せないはずなので何も考えずにkill */
                /* adb install -r だと確実に残る:<                           */
                /*===========================================================*/
                android.os.Process.killProcess(psResult.getPid());
              }
            }

            if (logcatProcess == null) {
              if (Constant.DEBUG)Log.d(TAG, "start logcat process");
              logcatProcess = Runtime.getRuntime().exec(new String[] { "logcat", "-v", "time", "*:V"});
              if (Constant.DEBUG)Log.d(TAG, "done logcat process");
              logcatReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()), BUFSZ);
              if (Constant.DEBUG)Log.d(TAG, "got reader from logcat");
            }
          }
        }
        catch (IOException ex) {
          Log.e(TAG, "Initialize logcatProcess failure.", ex);
          doExecute(shutdownCommand);
        }
        Cursor cursor = null;
        try {
          cursor = getApplicationContext().getContentResolver().query(
            ErrorLogProvider.CONTENT_URI,
            new String[] { 
              ErrorLogProvider.COLUMN_NAME.TIMESTAMP_LONG,
              ErrorLogProvider.COLUMN_NAME.TAG,
              ErrorLogProvider.COLUMN_NAME.MESSAGE
            },
            null,
            null,
            ErrorLogProvider.COLUMN_NAME.TIMESTAMP_LONG + " desc LIMIT 1");
          if (cursor.moveToNext()) {
            lastDbData = new ErrorLog();
            lastDbData.setTimestampLong(cursor.getLong(0));
            lastDbData.setTag(cursor.getString(1));
            lastDbData.setMessage(cursor.getString(2));
          }
        }
        finally {
          if (cursor != null) {
            try { cursor.close(); } catch (Exception ex) {}
            cursor = null;
          }
        }
        if (Constant.DEBUG)Log.d(TAG, "セットアップ終了");
      }
    };

    shutdownCommand = new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG)Log.d(TAG,"シャットダウン開始");
        shutdown();
        if (Constant.DEBUG)Log.d(TAG,"シャットダウン終了");
      }
    };

    restartCommand = new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG)Log.v(TAG, ">>> restartCommand");
        restartFlag = true;
        shutdown(true);
        if (Constant.DEBUG)Log.v(TAG, "<<< restartCommand");
      }
    };
    resetRestartFlagCommand = new Runnable() {
      @Override
      public void run() {
        restartFlag = false;
      }
    };

    final Prefs prefs = Prefs.getInstance(getApplicationContext());

    mainCommand = new Runnable() {
      @Override
      public void run() {
        if (getExecutor().isShutdown()) {
          if (Constant.DEBUG)Log.d(TAG, "logcat主処理 終了中につき終了");
          return;
        }
        try {
          if (logcatReader.ready()) {
            String line = logcatReader.readLine();
            if (line == null) {
              if (Constant.DEBUG)Log.d(TAG, "readLine returned null");
              doExecute(restartCommand);
              return;
            }
            synchronized (logdata) {
              while (logdata.size() >= prefs.getLogcatSetting__BufferSize()) {
                logdata.remove(0);
              }
              logdata.add(filter(new LogLine(line)));
            }
          }
          else {
            if (Constant.DEBUG)Log.d(TAG, "main");
            if (! isAliveLogcatProcess()) {
              if (Constant.DEBUG)Log.d(TAG, "ps logcat process is dead or not my child.");
              if (Constant.DEBUG)Log.d(TAG, "ps logcat process restart now.");
              doExecute(restartCommand);
              return;
            }
            waitSeconds(5);
          }
        }
        catch (IOException ex) {
          Log.e(TAG, "read failure.", ex);
          doExecute(shutdownCommand);
          if (Constant.DEBUG)Log.d(TAG, "logcat主処理異常終了");
          return;
        }
        if (! getExecutor().isShutdown()) {
          doExecute(mainCommand);
        }
      }
    };
    if (Constant.DEBUG)Log.v(TAG, "<<< onCreate");
  }
  
  public void onStart(Intent intent, int startId) {
    if (Constant.DEBUG)Log.v(TAG, ">>> onStart");
    super.onStart(intent, startId);
    if (Constant.DEBUG)Log.v(TAG, "<<< onStart");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (Constant.DEBUG)Log.v(TAG, ">>> onStartCommand");
    Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
    doExecute(setupCommand);
    doExecute(mainCommand);
    if (Constant.DEBUG)Log.v(TAG, "<<< onStartCommand");
    return START_STICKY;
  }

  protected void shutdown() {
    shutdown(false);
  }

  protected boolean isAliveLogcatProcess() {
    if (Constant.DEBUG)Log.v(TAG, ">>> isAliveLogcatProcess");
    int pid = android.os.Process.myPid();
    PsCommand psCommand = new PsCommand();
    psCommand.run();
    boolean result = false;
    for (PsCommand.PsResult psResult: psCommand.getOutput()) {
      if (psResult == null) continue;
      if (Constant.DEBUG)Log.v(TAG, "TEST:[" + psResult.getName() + "]");
      if (psResult != null && psResult.getPpid() == pid) {
        if (Constant.DEBUG)Log.v(TAG, "ALIVE:[" + psResult.getName() + "]");
        result = true;
      }
      if (psResult != null && psResult.getPpid() == 1) {
        if (Constant.DEBUG)Log.v(TAG, "KILL:[" + psResult.getName() + "]");
        android.os.Process.killProcess(psResult.getPid());
      }
    }
    if (Constant.DEBUG)Log.v(TAG, "<<< isAliveLogcatProcess");
    return result;
  }

  protected void shutdown(boolean restart) {
    synchronized(logcatProcessMutex) {
      if (logcatProcess != null) {
        new Destroyer(logcatProcess).waitAndDestroy();
        logcatProcess = null;
      }
    }
    ExecutorService executor = getExecutor();
    if (executor != null) {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
          executor.shutdownNow();
          if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
          }
        }
      } 
      catch (InterruptedException ex) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
      this.executor = null;
    }
    if (logcatReader != null) {
      try {
        logcatReader.close();
      }
      catch (IOException ex) {
        Log.e(TAG, "logcat reader close failure.", ex);
      }
      logcatReader = null;
    }
    if (restart) {
      doExecute(setupCommand);
      doExecute(mainCommand);
      doExecute(resetRestartFlagCommand);
    }
  }
  
  protected void unregisterAlarmService() {
    if (errorLogDeleter != null) {
      AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
      alarmManager.cancel(errorLogDeleter);
      errorLogDeleter = null;
    }
  }

  @Override
  public void onDestroy() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onDestroy");
    unregisterAlarmService();
    doExecute(shutdownCommand);
    shutdown();
    super.onDestroy();
    if (Constant.DEBUG)Log.v(TAG, "<<< onDestroy");
  }
  
  private class LogcatServiceBinder extends ILogcatService.Stub {
    public List<LogLine> getLog() {
      if (Constant.DEBUG)Log.v(TAG, ">>> getLog");
      if (! getExecutor().isShutdown()) {
        if (Constant.DEBUG)Log.d(TAG, "executor alive.");
        if (Constant.DEBUG)Log.d(TAG, "getLog");
        if (! isAliveLogcatProcess()) {
          if (Constant.DEBUG)Log.d(TAG, "ps logcat process is dead or not my child.");
          if (Constant.DEBUG)Log.d(TAG, "ps logcat process restart now.");
          doExecute(restartCommand);
          while(restartFlag) {
            if (Constant.DEBUG)Log.d(TAG, "wait restarting...");
            waitSecond();
          }
        }
      }
      else {
        if (Constant.DEBUG)Log.d(TAG, "executor dead");
        if (! restartFlag) {
          doExecute(restartCommand);
          while(restartFlag) {
            if (Constant.DEBUG)Log.d(TAG, "wait restarting...");
            waitSecond();
          }
        }
      }
      int retry = 0;
      while(true) {
        synchronized (logdata) {
          if (logdata.size() != 0) {
            break;
          }
        }
        waitSecond();
        if (getExecutor().isShutdown()) {
          break;
        }
      }
      ArrayList<LogLine> result = new ArrayList<LogLine>();
      synchronized(logdata) {
        result = new ArrayList<LogLine>(logdata);
      }
      if (Constant.DEBUG)Log.v(TAG, "<<< getLog");
      return result;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }
}
