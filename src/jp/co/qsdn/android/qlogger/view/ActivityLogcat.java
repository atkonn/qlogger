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
package jp.co.qsdn.android.qlogger.view;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import android.text.Html;

import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.qsdn.android.qlogger.Constant;
import jp.co.qsdn.android.qlogger.ILogcatService;
import jp.co.qsdn.android.qlogger.LogcatService;
import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.RebootLoggerMenu;;
import jp.co.qsdn.android.qlogger.core.LogLine;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.adapter.LogcatListAdapter;




public class ActivityLogcat
  extends AbstractActivity
{
  private final String TAG = getClass().getName();
  private ILogcatService logcatService = null;
  private ServiceConnection serviceConnection = null;
  private Runnable updateViewCommand = null;
  Pattern basePattern = null;

  @Override
  protected int getContentViewResourceId() {
    return R.layout.logcat;
  }
  @Override
  protected String getTitleText() {
    return getResources().getString(R.string.logcat__text_of_title);
  }
  @Override
  public void onCreate(Bundle savedInstanceState) {
    if (Constant.DEBUG)Log.v(TAG, ">>> onCreate");
    super.onCreate(savedInstanceState);

    basePattern = Pattern.compile("^([^\\s]+)\\s([^\\s]+)\\s([VDIWEFS])/.*$");

    serviceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        setLogcatService(ILogcatService.Stub.asInterface(service));
      }
      public void onServiceDisconnected (ComponentName name) {
        setLogcatService(null);
      }
    };

    updateViewCommand = new Runnable() {
      public void run() {
        if (Constant.DEBUG)Log.v(TAG,">>> updateViewCommand");
        int retry = 0;
        int retryConn = 0;
        waitMillis(200);
        while(getLogcatService() == null) {
          connectLogcatService();
          waitMillis(200);
          while (getLogcatService() == null) {
            waitSecond();
            Log.d(TAG, "logcatService接続待ち");
            if (++retry >= 10) {
              Log.e(TAG, "logcatServiceに接続できませんでした");
              break;
            }
          }
          if (++retryConn >= 10) {
            Log.e(TAG, "logcatServiceに接続できませんでした2");
            return; 
          }
        }
        List _list = null;
        try {
          _list = getLogcatService().getLog();
        }
        catch (RemoteException ex) {
          Log.e(TAG, "logcat service(getLog()) failure", ex);
        }
        
        if (_list == null) {
          getHandler().post(new Runnable() {
            public void run() {
              switchViewToNotFound();
            }
          });
          if (Constant.DEBUG)Log.v(TAG,"<<< updateViewCommand (_list is null)");
          return;
        }

        final ListView listView = (ListView)findViewById(getMainViewId());
        if (listView == null) {
          getHandler().post(new Runnable() {
            public void run() {
              switchViewToNotFound();
            }
          });
          if (Constant.DEBUG)Log.v(TAG,"<<< updateViewCommand (listView is null)");
          return;
        }

        final List<LogLine> list = filter(_list);
        getHandler().post(new Runnable() {
          public void run() {
            ArrayAdapter<String> adapter = (ArrayAdapter)listView.getAdapter();
            if (adapter != null) {
              adapter.clear();
            }
            listView.setAdapter(new LogcatListAdapter(ActivityLogcat.this, R.layout.logcat__row, list));
            listView.setSelection(listView.getCount()-1);
          }
        });
        if (listView.getCount() == 0) {
          getHandler().post(new Runnable() {
            public void run() {
              switchViewToNotFound();
              if (Constant.DEBUG)Log.v(TAG,"listView.getCount() == 0");
            }
          });
        }
        else {
          getHandler().post(new Runnable() {
            public void run() {
              switchViewToMain();
            }
          });
        }
        if (Constant.DEBUG)Log.v(TAG,"<<< updateViewCommand");
      }
    };
    if (Constant.DEBUG)Log.v(TAG, "<<< onCreate");
  }

  protected List<LogLine> filter(List<LogLine> list) {
    List<LogLine> result = new ArrayList<LogLine>();
    String filterKeyword = Prefs.getInstance(getApplicationContext()).getLogcatSetting__FilterKeyword();
    if ("".equalsIgnoreCase(filterKeyword)) {
      filterKeyword = "^.*$";
    }
    Pattern filterKeywordPattern = Pattern.compile(filterKeyword);
    for (LogLine line: list) {
      if (line.getMessage() == null) {
        Log.d(TAG, line.getRawLine());
        continue;
      }
      Matcher keywordMatcher = filterKeywordPattern.matcher(line.getMessage());
      if (! keywordMatcher.matches()) {
        continue;
      }

      result.add(line);
    }
    return result;
  }

  @Override
  public void onResume() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onResume");
    super.onResume();
    switchViewToWait();
    doExecute(updateViewCommand);
    if (Constant.DEBUG)Log.v(TAG, "<<< onResume");
  }

  @Override
  public void onPause() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onPause");
    doExecute(new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG)Log.v(TAG, ">>> onPauseCommand");
        if (getLogcatService() != null) {
          disconnectLogcatService();
        }
        if (Constant.DEBUG)Log.v(TAG, "<<< onPauseCommand");
      }
    });
    super.onPause();
    if (Constant.DEBUG)Log.v(TAG, "<<< onPause");
  }
  @Override
  public void onDestroy() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onDestroy");
    doExecute(new Runnable() {
      @Override 
      public void run() {
        if (Constant.DEBUG)Log.v(TAG, ">>> onDestroyCommand");
        if (getLogcatService() != null) {
          disconnectLogcatService();
        }
        if (Constant.DEBUG)Log.v(TAG, "<<< onDestroyCommand");
      }
    });
    super.onDestroy();
    if (Constant.DEBUG)Log.v(TAG, "<<< onDestroy");
  }

  protected Object connectLock = new Object(){};
  protected boolean isConnect = false;
  protected void connectLogcatService() {
    if (Constant.DEBUG)Log.v(TAG, ">>> connectLogcatService");
    synchronized(connectLock) {
      if (! isConnect) {
        Intent intent = new Intent(getApplicationContext(), LogcatService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        isConnect = true;
      }
    }
    if (Constant.DEBUG)Log.v(TAG, "<<< connectLogcatService");
  }

  protected void disconnectLogcatService() {
    if (Constant.DEBUG)Log.v(TAG, ">>> disconnectLogcatService");
    synchronized(connectLock) {
      if (isConnect) {
        unbindService(serviceConnection);
        setLogcatService(null);
      }
    }
    if (Constant.DEBUG)Log.v(TAG, "<<< disconnectLogcatService");
  }

  public void gotoReload(View v) {
    switchViewToWait();
    Log.d(TAG,"gotoReload");
    doExecute(updateViewCommand);
  }

  public void gotoSetting(View v) {
    Log.d(TAG,"gotoSetting");
    Intent intent = new Intent(this, ActivityLogcatSetting.class);
    startActivity(intent);
  }
  
  public ILogcatService getLogcatService() {
    return logcatService;
  }
  public void setLogcatService(ILogcatService logcatService) {
    this.logcatService = logcatService;
  }
  public void changePage(int newStartPos) {
    throw new UnsupportedOperationException();
  }
}
