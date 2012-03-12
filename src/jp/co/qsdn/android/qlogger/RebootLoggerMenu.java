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

import android.content.Context;
import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;

import jp.co.qsdn.android.qlogger.view.ActivityRebootLog;
import jp.co.qsdn.android.qlogger.view.ActivityLogcat;

public class RebootLoggerMenu {
  protected static RebootLoggerMenu rebootLoggerMenu = null;
  public final static int TOP_MENU_ID       = 1;
  public final static int LOGCAT_MENU_ID    = 2;

  public static RebootLoggerMenu getInstance() {
    if (rebootLoggerMenu == null) {
      rebootLoggerMenu = new RebootLoggerMenu();
    }
    return rebootLoggerMenu;
  }
  
  public boolean onCreateOptionsMenu(Menu menu) {
//    MenuInflater inflater = getMenuInflater();
//    inflater.inflate(R.menu.main_activity, menu);
    menu.add(Menu.NONE, TOP_MENU_ID,       Menu.NONE, "top");
    menu.add(Menu.NONE, LOGCAT_MENU_ID,    Menu.NONE, "logcat");
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item, Context context) {
    switch (item.getItemId()) {
    default:
      return false;
    case TOP_MENU_ID:
      {
        Intent intent = new Intent(context, ActivityRebootLog.class);
        context.startActivity(intent);
      }
      return true;
    case LOGCAT_MENU_ID:
      {
        Intent intent = new Intent(context, ActivityLogcat.class);
        context.startActivity(intent);
      }
      return true;
    }
  } 
}
