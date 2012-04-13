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
import jp.co.qsdn.android.qlogger.provider.RebootLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.ActivityRebootLog;


public class RecordBootCompletedService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordBootCompletedService.class.getCanonicalName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");


  Runnable command = null;
  
  @Override
  public void onCreate() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onCreate");
    super.onCreate();

    command = new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG) Log.d(TAG, ">>> run()");
        Cursor cursor = null;
        long nowTime = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        try {
          cursor = getApplicationContext().getContentResolver().query(
            RebootLogProvider.CONTENT_URI,
            new String[] {
              RebootLogProvider.COLUMN_NAME.ACTION_NAME,
              RebootLogProvider.COLUMN_NAME.CREATED_ON_LONG,
              RebootLogProvider.COLUMN_NAME.CREATED_ON,
            },
            null,
            null,
            RebootLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc LIMIT 1");
    
          contentValues.put(RebootLogProvider.COLUMN_NAME.ACTION_NAME, Constant.ACTION.BOOT_COMPLETED);
          if (cursor.moveToNext()) {
            String actionName = cursor.getString(0);
            if (! Constant.ACTION.ACTION_SHUTDOWN.equals(actionName)) {
              contentValues.put(RebootLogProvider.COLUMN_NAME.ACTION_NAME, Constant.ACTION.REBOOT);
              notifySend(nowTime);
            }
            else {
              long shutdownTime = cursor.getLong(1);
              contentValues.put(RebootLogProvider.COLUMN_NAME.DOWN_TIME, nowTime - shutdownTime);
            }
          }
          else {
            contentValues.put(RebootLogProvider.COLUMN_NAME.ACTION_NAME, Constant.ACTION.REBOOT);
            notifySend(nowTime);
          }
        }
        finally {
          if (cursor != null) {
            try { cursor.close(); } catch (Exception ex) {}
            cursor = null;
          }
        }
        contentValues.put(RebootLogProvider.COLUMN_NAME.CREATED_ON_LONG, nowTime);
        contentValues.put(RebootLogProvider.COLUMN_NAME.CREATED_ON, dateFormat.format(nowTime));
        getContentResolver().insert(RebootLogProvider.CONTENT_URI, contentValues);
        if (Constant.DEBUG) Log.d(TAG, "<<< run()");
        waitSeconds(2);
        stopSelf();
      }
    };


    doExecute(command);
    if (Constant.DEBUG) Log.v(TAG, "<<< onCreate");
  }

  private static int number = 0;
  protected void notifySend(long dateTime) {
    if (Constant.DEBUG) Log.v(TAG, ">>> notifySend");
    if (Prefs.getInstance(getApplicationContext()).getRebootLogSetting__Notification()) {
      NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
      Notification notification = new Notification();
      Intent intent = new Intent(getApplicationContext(), ActivityRebootLog.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), number, intent, 0);
      notification.icon = R.drawable.icon;
      notification.tickerText = String.format(getResources().getString(R.string.rebootlog__notification_message), dateFormat.format(dateTime));
      notification.setLatestEventInfo(getApplicationContext(), notification.tickerText, "", pendingIntent);
      notificationManager.notify(R.string.app_name+number, notification);
      number++;
    }
    if (Constant.DEBUG) Log.v(TAG, "<<< notifySend");
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
