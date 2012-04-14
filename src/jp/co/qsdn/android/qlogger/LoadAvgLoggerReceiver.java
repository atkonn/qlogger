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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import java.util.List;


public class LoadAvgLoggerReceiver
  extends AbstractExecutableReceiver {
  private final String TAG = getClass().getName();

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Constant.DEBUG) Log.v(TAG, ">>> onReceive");
    final Context _ctx = context.getApplicationContext();
    doExecute(new Runnable() {
      @Override
      public void run() {
        Intent _intent = new Intent(_ctx, RecordLoadAvgLogService.class);
        _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _ctx.startService(_intent);
        shutdown();
      }
    });
    if (Constant.DEBUG) Log.v(TAG, "<<< onReceive");
  }
}
