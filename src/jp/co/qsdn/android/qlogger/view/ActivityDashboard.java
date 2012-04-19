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

import android.content.Intent;

import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Handler;

import android.text.TextUtils;

import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ads.*;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import jp.co.qsdn.android.qlogger.Constant;
import jp.co.qsdn.android.qlogger.LogcatService;
import jp.co.qsdn.android.qlogger.QLoggerReceiverService;
import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.RebootLoggerMenu;;
import jp.co.qsdn.android.qlogger.core.RebootLog;
import jp.co.qsdn.android.qlogger.util.Util;


public class ActivityDashboard
  extends Activity
{
  final String TAG = getClass().getName();
  private AdView adView;
  private Handler handler = new Handler();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.dashboard);

    setupTitle();
    setupAd();
  }

  private void setupTitle() {
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.dashboard__action_bar);

    TextView textTitle = (TextView)findViewById(R.id.title_text);
    textTitle.setText(R.string.app_name);
    ImageView imageView = (ImageView) findViewById(R.id.title_icon);
    imageView.setImageResource(R.drawable.icon);
  }
  private void setupAd() {
    final AdView adView = (AdView)this.findViewById(R.id.adView);
    final AdRequest req = new AdRequest();
    String devs = getResources().getString(R.string.admob__devices);
    String[] devices = TextUtils.split(devs, ",");
    for (int ii=0; ii<devices.length; ii++) {
      req.addTestDevice(devices[ii]);
      if (Constant.DEBUG)Log.d(TAG, "test device:[" + devices[ii] + "]");
    }
    handler.post(new Runnable() {
      public void run() {
        adView.loadAd(req);
      }
    });
  }
  @Override
  protected void onDestroy() {
    if (adView != null) {
      adView.destroy();
      adView = null;
    }
    super.onDestroy();
  }

  public void gotoRebootLog(View v) {
    Intent intent = new Intent(this, ActivityRebootLog.class);
    startActivity(intent);
  }
  public void gotoLogcat(View v) {
    Intent intent = new Intent(this, ActivityLogcat.class);
    startActivity(intent);
  }
  public void gotoErrorLog(View v) {
    Intent intent = new Intent(this, ActivityErrorLog.class);
    startActivity(intent);
  }
  public void gotoBatteryLog(View v) {
    Intent intent = new Intent(this, ActivityBatteryLog.class);
    startActivity(intent);
  }
  public void gotoScreenLog(View v) {
    Intent intent = new Intent(this, ActivityScreenLog.class);
    startActivity(intent);
  }
  public void gotoLoadAvgLog(View v) {
    Intent intent = new Intent(this, ActivityLoadAvgLog.class);
    startActivity(intent);
  }
  public void gotoCpuUtilizationLog(View v) {
    Intent intent = new Intent(this, ActivityCpuUtilizationLog.class);
    startActivity(intent);
  }
  public void gotoMemUtilizationLog(View v) {
    Intent intent = new Intent(this, ActivityMemUtilizationLog.class);
    startActivity(intent);
  }
  public void gotoNetStatLog(View v) {
    Intent intent = new Intent(this, ActivityNetStatLog.class);
    startActivity(intent);
  }
  public void gotoAbout(View v) {
    Intent intent = new Intent(this, ActivityAbout.class);
    startActivity(intent);
  }
}
