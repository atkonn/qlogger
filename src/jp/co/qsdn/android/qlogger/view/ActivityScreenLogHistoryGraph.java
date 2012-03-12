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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;

import android.os.Bundle;
import android.os.Handler;

import android.text.TextUtils;

import android.util.Log;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost.TabSpec;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import jp.co.qsdn.android.qlogger.Constant;
import jp.co.qsdn.android.qlogger.R;


public class ActivityScreenLogHistoryGraph
  extends TabActivity 
{
  private final String TAG = getClass().getName();

  public static class EXTRA_PARAM {
    public static final String TAB = "tab";
    public static final String START_POS = "startPos";
  }
  Handler handler = new Handler();
  BroadcastReceiver receiver;

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
  public void waitSecond() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
    }
  }

  protected int getContentViewResourceId() {
    return R.layout.screenlog_history_graph;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    if (Constant.DEBUG)Log.v(TAG,">>> onCreate()" + this);
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(getContentViewResourceId());
    setupActionBar();

    onCreateBottomHalf();
    if (Constant.DEBUG)Log.v(TAG,"<<< onCreate()" + this);
  }
  protected void setTitleFromTab() {
    String nowTag = getTabHost().getCurrentTabTag();
    if (Constant.SCREEN_LOG_HISTORY.HOURLY_TAB.equalsIgnoreCase(nowTag)) {
      TextView textView = (TextView)findViewById(R.id.title_text);
      if (textView != null) {
        textView.setText(getResources().getString(R.string.dashboard_item_screenhourlylog_title));
      }
    }
    else
    if (Constant.SCREEN_LOG_HISTORY.DAILY_TAB.equalsIgnoreCase(nowTag)) {
      TextView textView = (TextView)findViewById(R.id.title_text);
      if (textView != null) {
        textView.setText(getResources().getString(R.string.dashboard_item_screendailylog_title));
      }
    }
    else
    if (Constant.SCREEN_LOG_HISTORY.MONTHLY_TAB.equalsIgnoreCase(nowTag)) {
      TextView textView = (TextView)findViewById(R.id.title_text);
      if (textView != null) {
        textView.setText(getResources().getString(R.string.dashboard_item_screenmonthlylog_title));
      }
    }
  }
  protected void onCreateBottomHalf() {
    setupTab();
    setTitleFromTab();
  }
  protected void setupActionBarBottomHalf() {
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

    imageView = (ImageView)findViewById(R.id.action_bar_reload);
    if (imageView != null) {
      imageView.setImageResource(R.drawable.action_bar_reload);
    }

    imageView = (ImageView)findViewById(R.id.action_bar_setting);
    if (imageView != null) {
      imageView.setImageResource(R.drawable.action_bar_setting);
    }

    setupActionBarBottomHalf();
  }
  protected int getTitleViewResourceId() {
    return R.layout.screenlog_history_graph__action_bar;
  }
  protected String getTitleText() {
    return "ScreenLog";
  }
  protected void setupTab() {
    if (Constant.DEBUG)Log.v(TAG, ">>> setupTab");
    Resources res = getResources();
    TabHost tabHost = getTabHost();

    TabSpec hourlyTab = getTabHost().newTabSpec(Constant.SCREEN_LOG_HISTORY.HOURLY_TAB);
    hourlyTab.setIndicator(res.getString(R.string.screenlog_history__hourly_tab_label), res.getDrawable(android.R.drawable.ic_menu_recent_history));
    Intent hourlyTabIntent = new Intent();
    hourlyTabIntent.setClass(this, ActivityScreenHourlyLogGraph.class);
    hourlyTabIntent.putExtra(EXTRA_PARAM.TAB,       Constant.SCREEN_LOG_HISTORY.HOURLY_TAB);
    hourlyTabIntent.putExtra(EXTRA_PARAM.START_POS, 0);
    hourlyTab.setContent(hourlyTabIntent);
    getTabHost().addTab(hourlyTab);

    TabSpec dailyTab = tabHost.newTabSpec(Constant.SCREEN_LOG_HISTORY.DAILY_TAB);
    dailyTab.setIndicator(res.getString(R.string.screenlog_history__daily_tab_label), res.getDrawable(android.R.drawable.ic_menu_day));
    Intent dailyTabIntent = new Intent();
    dailyTabIntent.setClass(this, ActivityScreenDailyLogGraph.class);
    dailyTabIntent.putExtra(EXTRA_PARAM.TAB,       Constant.SCREEN_LOG_HISTORY.DAILY_TAB);
    dailyTabIntent.putExtra(EXTRA_PARAM.START_POS, 0);
    dailyTab.setContent(dailyTabIntent);
    getTabHost().addTab(dailyTab);

    TabSpec monthlyTab = tabHost.newTabSpec(Constant.SCREEN_LOG_HISTORY.MONTHLY_TAB);
    monthlyTab.setIndicator(res.getString(R.string.screenlog_history__monthly_tab_label), res.getDrawable(android.R.drawable.ic_menu_month));
    Intent monthlyTabIntent = new Intent();
    monthlyTabIntent.setClass(this, ActivityScreenMonthlyLogGraph.class);
    monthlyTabIntent.putExtra(EXTRA_PARAM.TAB,       Constant.SCREEN_LOG_HISTORY.MONTHLY_TAB);
    monthlyTabIntent.putExtra(EXTRA_PARAM.START_POS, 0);
    monthlyTab.setContent(monthlyTabIntent);
    getTabHost().addTab(monthlyTab);

    final TabActivity self = this;
    getTabHost().setOnTabChangedListener( new TabHost.OnTabChangeListener() {
      @Override
      public void onTabChanged(String tabId) {
        setTitleFromTab();
      }
    });

    tabHost.setCurrentTabByTag(Constant.SCREEN_LOG_HISTORY.HOURLY_TAB);
    if (Constant.DEBUG)Log.v(TAG, "<<< setupTab");
  }
  @Override
  protected void onResume() {
    if (Constant.DEBUG)Log.v(TAG, ">>> onResume");
    super.onResume();

    String tab = getIntent().getStringExtra(EXTRA_PARAM.TAB);
    if (Constant.DEBUG)Log.d(TAG, "EXTRA_PARAM.TAB:[" + tab + "]");
    if (! TextUtils.isEmpty(tab)) {
      getTabHost().setCurrentTabByTag(tab);
    }
    int startPos = getIntent().getIntExtra(EXTRA_PARAM.START_POS, 0);
    AbstractActivity childActivity = (AbstractActivity)getCurrentActivity();
    childActivity.changePage(startPos);
    if (Constant.DEBUG) Log.v(TAG, "<<< onResume");
  }

  @Override
  protected void onPause() {
    Log.d(TAG,"start onPause()");
    super.onPause();
    Log.d(TAG,"end onPause()");
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent e) {
    if(e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
      if(e.getAction() == KeyEvent.ACTION_DOWN) {
        Log.d(TAG, "start dispatchKeyEvent()");
        String nowTag = getTabHost().getCurrentTabTag();
        AbstractActivity childActivity = (AbstractActivity)getCurrentActivity();

        if (Constant.DEBUG)Log.d(TAG, "startPos:[" + childActivity.getStartPos() + "]");
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PARAM.TAB,       nowTag);
        intent.putExtra(EXTRA_PARAM.START_POS, childActivity.getStartPos());
        setResult(RESULT_OK, intent);
        finish();
        Log.d(TAG, "end dispatchKeyEvent()");
        return false;
      }
      return true;
    }
    return super.dispatchKeyEvent(e);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  public void gotoList(View v) {
    String nowTag = getTabHost().getCurrentTabTag();
    AbstractActivity childActivity = (AbstractActivity)getCurrentActivity();

    if (Constant.DEBUG)Log.d(TAG, "startPos:[" + childActivity.getStartPos() + "]");
    Intent intent = new Intent();
    intent.putExtra(EXTRA_PARAM.TAB,       nowTag);
    intent.putExtra(EXTRA_PARAM.START_POS, childActivity.getStartPos());
    setResult(RESULT_OK, intent);
    finish();
  }
}
