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

import android.preference.CheckBoxPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.text.Html;

import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.util.Util;



public class ActivityErrorLogSetting
  extends PreferenceActivity
{
  private final String TAG = getClass().getName();
  private Handler handler = new Handler();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    /* PreferenceActivityでは、requestWindowFeatureはonCreateの前 */
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.errorlog_setting);
    setupActionBar();

    setupFooterButton();
    addPreferencesFromResource(R.xml.errorlog_setting);
    Preference pref = findPreference("errorlog_setting__key_pref_retention_period");  
    Prefs prefs = Prefs.getInstance(getApplicationContext());
    if (pref != null) {
      pref.setSummary(String.format(getResources().getString(R.string.errorlog_setting__summary_pref_retention_period), prefs.getErrorLogSetting__RetentionPeriod()));
    }


    /* タイトルバーが変わらないため、別画面として作成 */
/*
    Preference pref = findPreference("key_color_setting_activity");  
    if (pref != null) {
      pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {  
        @Override  
        public boolean onPreferenceClick(final Preference preference) {  
          startActivity(new Intent(ActivityErrorLogSetting.this,  ActivityErrorLogSetting_Color.class));  
          return true;  
        }  
      });
    }
    pref = findPreference("key_filter_setting_activity");  
    if (pref != null) {
      pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {  
        @Override  
        public boolean onPreferenceClick(final Preference preference) {  
          startActivity(new Intent(ActivityErrorLogSetting.this,  ActivityErrorLogSetting_Filter.class));  
          return true;  
        }  
      });
    }
*/
  }

  protected void setupActionBar() {
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.errorlog_setting__action_bar);

    TextView textView = (TextView)findViewById(R.id.title_text);
    textView.setText(getResources().getString(R.string.errorlog_setting__title));

    ImageView imageView = (ImageView)findViewById(R.id.title_icon);
    imageView.setImageResource(R.drawable.icon);

  }
  protected void setupFooterButton() {
    final Activity self = this;
    Button button = (Button)findViewById(R.id.save_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        self.finish();
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
