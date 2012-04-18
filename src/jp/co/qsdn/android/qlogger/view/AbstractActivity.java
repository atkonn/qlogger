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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import android.text.Html;

import android.util.Log;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
import jp.co.qsdn.android.qlogger.QLoggerReceiverService;
import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.RebootLoggerMenu;;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.util.Util;




public abstract class AbstractActivity
  extends Activity
{
  private final String TAG = AbstractActivity.class.getName();
  private Handler handler = new Handler();
  private ViewFlipper flipper;
  private int startPos  = 0;
  private int pageCount = 0;

  BroadcastReceiver externalStorageReceiver;
  boolean externalStorageAvailable = false;
  boolean externalStorageWriteable = false;
  
  void updateExternalStorageState() {
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      externalStorageAvailable = externalStorageWriteable = true;
    }
    else
    if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      externalStorageAvailable = true;
      externalStorageWriteable = false;
    }
    else {
      externalStorageAvailable = externalStorageWriteable = false;
    }
    //handleExternalStorageState(externalStorageAvailable, externalStorageWriteable);
  }
  
  public void startWatchingExternalStorage() {
    externalStorageReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.i("test", "Storage: " + intent.getData());
        updateExternalStorageState();
      }
    };
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
    getApplicationContext().registerReceiver(externalStorageReceiver, filter);
    updateExternalStorageState();
  }
  
  public void stopWatchingExternalStorage() {
    if (externalStorageReceiver != null) {
      try {
        getApplicationContext().unregisterReceiver(externalStorageReceiver);
      }
      catch (IllegalArgumentException ex) {
      }
      externalStorageReceiver = null;
    }
  }

  private ExecutorService executor = null;
  protected ExecutorService getExecutor() {
    if (executor == null || executor.isShutdown()) {
      executor = Executors.newSingleThreadExecutor();
    }
    return executor;
  }
  protected void doExecute(Runnable command) {
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
  public void waitSecond() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
    }
  }
  public void waitMillis(long millis) {
    try {
      TimeUnit.MILLISECONDS.sleep(millis);
    } catch (InterruptedException e) {
    }
  }

  protected int getContentViewResourceId() {
    return R.layout.logcat;
  }

  protected boolean isCustomTitle() {
    return true;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    if (Constant.DEBUG)Log.v(TAG, ">>> onCreate" + this);
    super.onCreate(savedInstanceState);
    if (isCustomTitle()) {
      requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }
    setContentView(getContentViewResourceId());
    if (isCustomTitle()) {
      setupActionBar();
    }

    if (! Util.isRunning(getApplicationContext(), QLoggerReceiverService.SERVICE_NAME)) {
      Intent _intent = new Intent(getApplicationContext(), QLoggerReceiverService.class);
      _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startService(_intent);
    }
    if (! Util.isRunning(getApplicationContext(), LogcatService.SERVICE_NAME)) {
      Intent _intent = new Intent(getApplicationContext(), LogcatService.class);
      _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startService(_intent);
    }

    flipper = (ViewFlipper)findViewById(R.id.flipper);

    onCreateBottomHalf();
    if (Constant.DEBUG)Log.v(TAG, "<<< onCreate" + this);
  }
  protected void onCreateBottomHalf() {
  }
  protected void setupPagerAndClearButton() {
    View view;

    view = findViewById(R.id.action_bar_prev);
    if (view != null) {
      if (getStartPos() == 0) {
        view.setEnabled(false);
      }
      else {
        view.setEnabled(true);
      }
    }
    view = findViewById(R.id.action_bar_next);
    if (view  != null) {
      if (getStartPos() + 1 >= getPageCount()) {
        view.setEnabled(false);
      }
      else {
        view.setEnabled(true);
      }
    }
    view = findViewById(R.id.action_bar_clear);
    if (view != null) {
      if (getPageCount() == 0) {
        view.setEnabled(false);
      }
      else {
        view.setEnabled(true);
      }
    }
    view = findViewById(R.id.action_bar_send);
    if (view != null) {
      if (getPageCount() == 0) {
        view.setEnabled(false);
      }
      else {
        view.setEnabled(true);
      }
    }
    view = findViewById(R.id.action_bar_prev_textview);
    if (view != null) {
      if (getStartPos() == 0) {
        view.setEnabled(false);
      }
      else {
        view.setEnabled(true);
      }
    }
    view = findViewById(R.id.action_bar_next_textview);
    if (view  != null) {
      if (getStartPos() + 1 >= getPageCount()) {
        view.setEnabled(false);
      }
      else {
        view.setEnabled(true);
      }
    }
    view = findViewById(R.id.action_bar_clear_textview);
    if (view != null) {
      if (getPageCount() == 0) {
        view.setEnabled(false);
      }
      else {
        view.setEnabled(true);
      }
    }
    view = findViewById(R.id.action_bar_send_textview);
    if (view != null) {
      if (getPageCount() == 0) {
        view.setEnabled(false);
      }
      else {
        view.setEnabled(true);
      }
    }
  }

  protected int getWaitViewId() {
    return R.id.wait_view;
  }
  protected int getMainViewId() {
    return R.id.main_view;
  }
  protected int getNotFoundViewId() {
    return R.id.notfound_view;
  }

  protected void switchViewToWait() {
    if (getFlipper() != null) {
      getFlipper().setVisibility(View.INVISIBLE);
      while (getFlipper().getCurrentView().getId() != getWaitViewId()) {
        getFlipper().showNext();
      }
      getFlipper().setVisibility(View.VISIBLE);
    }
  }
  public void switchViewToMain() {
    if (getFlipper() != null) {
      getFlipper().setVisibility(View.INVISIBLE);
      while (getFlipper().getCurrentView().getId() != getMainViewId()) {
        getFlipper().showNext();
      }
      getFlipper().setVisibility(View.VISIBLE);
    }
  }
  public void switchViewToNotFound() {
    if (getFlipper() != null) {
      getFlipper().setVisibility(View.INVISIBLE);
      while (getFlipper().getCurrentView().getId() != getNotFoundViewId()) {
        getFlipper().showNext();
      }
      getFlipper().setVisibility(View.VISIBLE);
    }
  }
  protected int getTitleViewResourceId() {
    return R.layout.logcat__action_bar;
  }
  protected String getTitleText() {
    return "Logcat";
  }
  protected void setupActionBar() {
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, getTitleViewResourceId());
    TextView textView = (TextView)findViewById(R.id.title_text);
    if (textView != null) {
      textView.setText(getTitleText());
    }
    ImageView imageView = (ImageView)findViewById(R.id.title_icon);
    if (imageView != null) {
      imageView.setImageResource(R.drawable.icon);
    }
    setupActionBarBottomHalf();
  }
  protected void setupActionBarBottomHalf() {
  }

  @Override
  public void onResume() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onResume(" + getStartPos() + ")" + this);
    super.onResume();
    if (! Util.isRunning(getApplicationContext(), LogcatService.SERVICE_NAME)) {
      Intent _intent = new Intent(getApplicationContext(), LogcatService.class);
      _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startService(_intent);
    }
    if (Constant.DEBUG)Log.v(TAG, "<<< onResume(" + getStartPos() + ")" + this);
  }

  protected void shutdown() {
    getExecutor().shutdown();
    try {
      if (!getExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
        getExecutor().shutdownNow();
        if (!getExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
          if (Constant.DEBUG)Log.d(TAG,"ExecutorService did not terminate....");
          getExecutor().shutdownNow();
          Thread.currentThread().interrupt();
        }
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
    executor = null;
  }

  @Override
  protected void onPause() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onPause " + this);
    if (getExecutor() != null) {
      doExecute(new Runnable() {
        @Override
        public void run() {
          shutdown();
        }
      });
    }
    super.onPause();
    if (Constant.DEBUG)Log.v(TAG, "<<< onPause " + this);
  }

  @Override
  protected void onDestroy() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onDestroy" + this);
    if (getExecutor() != null) {
      doExecute(new Runnable() {
        @Override
        public void run() {
          if (Constant.DEBUG)Log.v(TAG, ">>> onDestroyCommand");
          shutdown();
          if (Constant.DEBUG)Log.v(TAG, "<<< onDestroyCommand");
        }
      });
    }
    stopWatchingExternalStorage();
    super.onDestroy();
    if (Constant.DEBUG)Log.v(TAG, "<<< onDestroy" + this);
  }

  public Handler getHandler() {
    return handler;
  }
  public void setHandler(Handler handler) {
    this.handler = handler;
  }
  
  public ViewFlipper getFlipper() {
    return flipper;
  }
  
  public void setFlipper(ViewFlipper flipper) {
    this.flipper = flipper;
  }
  
  public boolean getExternalStorageAvailable() {
    return externalStorageAvailable;
  }
  public void setExternalStorageAvailable(boolean externalStorageAvailable) {
    this.externalStorageAvailable = externalStorageAvailable;
  }
  public boolean getExternalStorageWriteable() {
    return externalStorageWriteable;
  }
  public void setExternalStorageWriteable(boolean externalStorageWriteable) {
    this.externalStorageWriteable = externalStorageWriteable;
  }
  
  public BroadcastReceiver getExternalStorageReceiver() {
    return externalStorageReceiver;
  }
  public void setExternalStorageReceiver(BroadcastReceiver externalStorageReceiver) {
    this.externalStorageReceiver = externalStorageReceiver;
  }
  
  public int getStartPos() {
    return this.startPos;
  }
  
  public void setStartPos(int startPos) {
    if (Constant.DEBUG)Log.v(TAG, ">>> setStartPos(" + getStartPos() + ")" + this);
    this.startPos = startPos;
    if (Constant.DEBUG)Log.v(TAG, "<<< setStartPos(" + getStartPos() + ")" + this);
  }
  public abstract void changePage(int newStartPos);

  @Override
  public boolean dispatchKeyEvent(KeyEvent e) {
    if(e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
      if(e.getAction() == KeyEvent.ACTION_DOWN) {
        if (Constant.DEBUG)Log.v(TAG, ">>> dispatchKeyEvent()");
        setResult(RESULT_OK, getIntent());
        finish();
        if (Constant.DEBUG)Log.v(TAG, "<<< dispatchKeyEvent()");
        return false;
      }
      return true;
    }
    return super.dispatchKeyEvent(e);
  }
  
  public int getPageCount() {
    return pageCount;
  }
  
  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }
}
