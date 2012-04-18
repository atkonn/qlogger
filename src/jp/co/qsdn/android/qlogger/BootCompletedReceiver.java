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

import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;

import android.util.Log;

import java.text.SimpleDateFormat;

import java.util.List;

import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.RebootLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.ActivityRebootLog;


public class BootCompletedReceiver
  extends AbstractExecutableReceiver {
  private final String TAG = getClass().getName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");

  @Override
  public void onReceive(Context context, final Intent intent) {
    if (Constant.DEBUG)Log.v(TAG, ">>> onReceive");

    final Context _ctx = context.getApplicationContext();
    doExecute(new Runnable() { 
      @Override
      public void run() {
        String action = intent.getAction();
        if (Constant.DEBUG)Log.d(TAG, "onReceive:[" + action + "]");
        if(action.equals("android.intent.action.BOOT_COMPLETED")) {
          {
            Intent _intent = new Intent(_ctx, RecordBootCompletedService.class);
            _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _ctx.startService(_intent);
          }
    
          if (! Util.isRunning(_ctx, QLoggerReceiverService.SERVICE_NAME)) {
            Intent _intent = new Intent(_ctx, QLoggerReceiverService.class);
            _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _ctx.startService(_intent);
          }
          if (! Util.isRunning(_ctx, LogcatService.SERVICE_NAME)) {
            Intent _intent = new Intent(_ctx, LogcatService.class);
            _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _ctx.startService(_intent);
          }
        }
        shutdown();
      }
    });
    if (Constant.DEBUG)Log.v(TAG, "<<< onReceive");
  }
}
