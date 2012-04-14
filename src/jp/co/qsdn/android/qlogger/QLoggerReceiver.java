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

import jp.co.qsdn.android.qlogger.provider.RebootLogProvider;
import jp.co.qsdn.android.qlogger.provider.ScreenLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;



public class QLoggerReceiver
  extends AbstractExecutableReceiver {
  private final String TAG = getClass().getName();
  private ContentResolver resolver;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");

  @Override
  public void onReceive(Context context, final Intent intent) {
    if (Constant.DEBUG)Log.v(TAG, ">>> onReceive");

    final Context _ctx = context.getApplicationContext();
    doExecute(new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG)Log.v(TAG, ">>> onReceiveCommand action:[" + intent.getAction() + "]");
        String action = intent.getAction();
        if ((action.equals("android.intent.action.ACTION_SHUTDOWN"))||
            (action.equals("android.intent.action.REBOOT"))) {
          recordRebootLogOnDb(_ctx, action);
        }
        else
        if (Constant.ACTION.SCREEN_ON.equals(action)) {
          recordScreenOnLogOnDb(_ctx, action);
        }
        else
        if (Constant.ACTION.SCREEN_OFF.equals(action)) {
          recordScreenOffLogOnDb(_ctx, action);
        }
        if (Constant.DEBUG)Log.v(TAG, "<<< onReceiveCommand action:[" + intent.getAction() + "]");
        shutdown();
      }
    });
    if (Constant.DEBUG)Log.v(TAG, "<<< onReceive");
  }

  protected void recordRebootLogOnDb(Context context,String action) {
    long nowTime = System.currentTimeMillis();
    ContentValues contentValues = new ContentValues();
    contentValues.put(RebootLogProvider.COLUMN_NAME.ACTION_NAME, action);
    contentValues.put(RebootLogProvider.COLUMN_NAME.CREATED_ON, dateFormat.format(nowTime));
    contentValues.put(RebootLogProvider.COLUMN_NAME.CREATED_ON_LONG, nowTime);
    contentValues.put(RebootLogProvider.COLUMN_NAME.DOWN_TIME, 0);
    context.getContentResolver().insert(RebootLogProvider.CONTENT_URI, contentValues);
  }
  protected void recordScreenOnLogOnDb(Context context, String action) {
    long nowTime = System.currentTimeMillis();
    ContentValues contentValues = new ContentValues();
    contentValues.put(ScreenLogProvider.COLUMN_NAME.ACTION_NAME, action);
    contentValues.put(ScreenLogProvider.COLUMN_NAME.ON_TIME, 0);
    contentValues.put(ScreenLogProvider.COLUMN_NAME.CREATED_ON, dateFormat.format(nowTime));
    contentValues.put(ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG, nowTime);
    context.getContentResolver().insert(ScreenLogProvider.CONTENT_URI, contentValues);
  }
  protected void recordScreenOffLogOnDb(Context context, String action) {
    Cursor cursor = null;
    try {
      cursor = context.getContentResolver().query(
        ScreenLogProvider.CONTENT_URI,
          new String[] {
            ScreenLogProvider.COLUMN_NAME.ACTION_NAME,
            ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG,
            ScreenLogProvider.COLUMN_NAME.CREATED_ON,
          },
          null,
          null,
          ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc LIMIT 1");
      if (cursor.moveToNext()) {
        String actionName = cursor.getString(0);
        if ("android.intent.action.SCREEN_ON".equalsIgnoreCase(actionName)) {
          long onTime = cursor.getLong(1);
          long nowTime = System.currentTimeMillis();
          ContentValues contentValues = new ContentValues();
          contentValues.put(ScreenLogProvider.COLUMN_NAME.ACTION_NAME, action);
          contentValues.put(ScreenLogProvider.COLUMN_NAME.ON_TIME, nowTime - onTime);
          contentValues.put(ScreenLogProvider.COLUMN_NAME.CREATED_ON, dateFormat.format(nowTime));
          contentValues.put(ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG, nowTime);
          context.getContentResolver().insert(ScreenLogProvider.CONTENT_URI, contentValues);
          return;
        }
      }    
    }
    finally {
      if (cursor != null) {
        try { cursor.close(); } catch (Exception ex) {}
        cursor = null;
      }
    }
    long nowTime = System.currentTimeMillis();
    ContentValues contentValues = new ContentValues();
    contentValues.put(ScreenLogProvider.COLUMN_NAME.ACTION_NAME, action);
    contentValues.put(ScreenLogProvider.COLUMN_NAME.ON_TIME, 0);
    contentValues.put(ScreenLogProvider.COLUMN_NAME.CREATED_ON, dateFormat.format(nowTime));
    contentValues.put(ScreenLogProvider.COLUMN_NAME.CREATED_ON_LONG, nowTime);
    context.getContentResolver().insert(ScreenLogProvider.CONTENT_URI, contentValues);
  }
}
