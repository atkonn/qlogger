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
package jp.co.qsdn.android.qlogger.prefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

import android.util.Log;

import jp.co.qsdn.android.qlogger.R;

public class Prefs {
  private final String TAG = getClass().getName();
  private static Prefs mPrefs = null;
  public static final String PACKAGE_NAME = "jp.co.qsdn.android.qlogger";
  private Context mContext = null;

  public static final String LOGCAT_PROCESS__KEY_LOGCAT_PID = "logcat_process__key_logcat_pid";
  public static final int LOGCAT_PROCESS__DEFAULT_LOGCAT_PID = 0;
  public void setLogcatProcess__LogcatPid(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOGCAT_PROCESS__KEY_LOGCAT_PID, v)
      .commit();  
  }
  public int getLogcatProcess__LogcatPid() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOGCAT_PROCESS__KEY_LOGCAT_PID, LOGCAT_PROCESS__DEFAULT_LOGCAT_PID);
  }

  public static final String LOGCAT_SETTING__KEY_PREF_FATAL_COLOR = "logcat_setting__key_pref_fatal_color_Color";
  public static final int LOGCAT_SETTING__DEFAULT_PREF_FATAL_COLOR = 0xFF0000;
  public void setLogcatSetting__FatalColor(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOGCAT_SETTING__KEY_PREF_FATAL_COLOR, v)
      .commit();  
  }
  public int getLogcatSetting__FatalColor() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOGCAT_SETTING__KEY_PREF_FATAL_COLOR, LOGCAT_SETTING__DEFAULT_PREF_FATAL_COLOR);
  }

  public static final String LOGCAT_SETTING__KEY_PREF_ERROR_COLOR = "logcat_setting__key_pref_error_color_Color";
  public static final int LOGCAT_SETTING__DEFAULT_PREF_ERROR_COLOR = 0xFF0000;
  public void setLogcatSetting__ErrorColor(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOGCAT_SETTING__KEY_PREF_ERROR_COLOR, v)
      .commit();  
  }
  public int getLogcatSetting__ErrorColor() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOGCAT_SETTING__KEY_PREF_ERROR_COLOR, LOGCAT_SETTING__DEFAULT_PREF_ERROR_COLOR);
  }

  public static final String LOGCAT_SETTING__KEY_PREF_WARNING_COLOR = "logcat_setting__key_pref_warning_color_Color";
  public static final int LOGCAT_SETTING__DEFAULT_PREF_WARNING_COLOR = 0xbcb607;
  public void setLogcatSetting__WarningColor(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOGCAT_SETTING__KEY_PREF_WARNING_COLOR, v)
      .commit();  
  }
  public int getLogcatSetting__WarningColor() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOGCAT_SETTING__KEY_PREF_WARNING_COLOR, LOGCAT_SETTING__DEFAULT_PREF_WARNING_COLOR);
  }

  public static final String LOGCAT_SETTING__KEY_PREF_INFO_COLOR = "logcat_setting__key_pref_info_color_Color";
  public static final int LOGCAT_SETTING__DEFAULT_PREF_INFO_COLOR = 0xFFFFFF;
  public void setLogcatSetting__InfoColor(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOGCAT_SETTING__KEY_PREF_INFO_COLOR, v)
      .commit();  
  }
  public int getLogcatSetting__InfoColor() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOGCAT_SETTING__KEY_PREF_INFO_COLOR, LOGCAT_SETTING__DEFAULT_PREF_INFO_COLOR);
  }

  public static final String LOGCAT_SETTING__KEY_PREF_VERBOSE_COLOR = "logcat_setting__key_pref_verbose_color_Color";
  public static final int LOGCAT_SETTING__DEFAULT_PREF_VERBOSE_COLOR = 0xc94943;
  public void setLogcatSetting__VerboseColor(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOGCAT_SETTING__KEY_PREF_VERBOSE_COLOR, v)
      .commit();  
  }
  public int getLogcatSetting__VerboseColor() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOGCAT_SETTING__KEY_PREF_VERBOSE_COLOR, LOGCAT_SETTING__DEFAULT_PREF_VERBOSE_COLOR);
  }

  public static final String LOGCAT_SETTING__KEY_PREF_DEBUG_COLOR = "logcat_setting__key_pref_debug_color_Color";
  public static final int LOGCAT_SETTING__DEFAULT_PREF_DEBUG_COLOR = 0x43c949;
  public void setLogcatSetting__DebugColor(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOGCAT_SETTING__KEY_PREF_DEBUG_COLOR, v)
      .commit();  
  }
  public int getLogcatSetting__DebugColor() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOGCAT_SETTING__KEY_PREF_DEBUG_COLOR, LOGCAT_SETTING__DEFAULT_PREF_DEBUG_COLOR);
  }

  public static final String LOGCAT_SETTING__KEY_PREF_BUFFER_SIZE = "logcat_setting__key_pref_buffer_size";
  public static final int LOGCAT_SETTING__DEFAULT_PREF_BUFFER_SIZE = 300;
  public void setLogcatSetting__BufferSize(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOGCAT_SETTING__KEY_PREF_BUFFER_SIZE + "_Number", v)
      .commit();  
  }
  public int getLogcatSetting__BufferSize() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOGCAT_SETTING__KEY_PREF_BUFFER_SIZE + "_Number", LOGCAT_SETTING__DEFAULT_PREF_BUFFER_SIZE);
  }

  public static final String LOGCAT_SETTING__KEY_PREF_FILTER_KEYWORD = "logcat_setting__key_pref_filter_keyword";
  public static final String LOGCAT_SETTING__DEFAULT_PREF_FILTER_KEYWORD = "";
  public void setLogcatSetting__FilterKeyword(String v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putString(LOGCAT_SETTING__KEY_PREF_FILTER_KEYWORD, v)
      .commit();  
  }
  public String getLogcatSetting__FilterKeyword() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getString(LOGCAT_SETTING__KEY_PREF_FILTER_KEYWORD,  LOGCAT_SETTING__DEFAULT_PREF_FILTER_KEYWORD);
  }
  public static final String LOGCAT_SETTING__KEY_PREF_LOGLEVEL = "logcat_setting__key_pref_loglevel";
  public static final String LOGCAT_SETTING__DEFAULT_PREF_LOGLEVEL = "V";
  public void setLogcatSetting__Loglevel(String v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putString(LOGCAT_SETTING__KEY_PREF_LOGLEVEL, v)
      .commit();  
  }
  public String getLogcatSetting__Loglevel() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getString(LOGCAT_SETTING__KEY_PREF_LOGLEVEL,  LOGCAT_SETTING__DEFAULT_PREF_LOGLEVEL);
  }

  public static final String ERRORLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "errorlog_setting__key_pref_retention_period";
  public static final int ERRORLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 3;
  public void setErrorLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(ERRORLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getErrorLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(ERRORLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  ERRORLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String REBOOTLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "rebootlog_setting__key_pref_retention_period";
  public static final int REBOOTLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 3;
  public void setRebootLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(REBOOTLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getRebootLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(REBOOTLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  REBOOTLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String REBOOTLOG_SETTING__KEY_PREF_NOTIFICATION = "rebootlog_setting__key_pref_notification";
  public static final boolean REBOOTLOG_SETTING__DEFAULT_PREF_NOTIFICATION = true;
  public void setRebootLogSetting__Notification(boolean notification) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);
    sharedPreferences
      .edit()
      .putBoolean(REBOOTLOG_SETTING__KEY_PREF_NOTIFICATION, notification)
      .commit();
  }
  public boolean getRebootLogSetting__Notification() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
    return sharedPreferences.getBoolean(REBOOTLOG_SETTING__KEY_PREF_NOTIFICATION, REBOOTLOG_SETTING__DEFAULT_PREF_NOTIFICATION);
  }

  /* Battery */
  public static final String BATTERYLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "batterylog_setting__key_pref_retention_period";
  public static final int BATTERYLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 7;
  public void setBatteryLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(BATTERYLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getBatteryLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(BATTERYLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  BATTERYLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String BATTERYLOG__KEY_PREF_NOW_LEVEL = "batterylog_setting__key_pref_now_level";
  public static final int BATTERYLOG__DEFAULT_PREF_NOW_LEVEL = 100;
  public void setBatteryLog__NowLevel(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(BATTERYLOG__KEY_PREF_NOW_LEVEL, v)
      .commit();  
  }
  public int getBatteryLog__NowLevel() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(BATTERYLOG__KEY_PREF_NOW_LEVEL,  BATTERYLOG__DEFAULT_PREF_NOW_LEVEL);
  }

  /* Screen */
  public static final String SCREENLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "screenlog_setting__key_pref_retention_period";
  public static final int SCREENLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 30;
  public void setScreenLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(SCREENLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getScreenLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(SCREENLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  SCREENLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String SCREENHOURLYLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "screenhourlylog_setting__key_pref_retention_period";
  public static final int SCREENHOURLYLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 30;
  public void setScreenHourlyLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(SCREENHOURLYLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getScreenHourlyLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(SCREENHOURLYLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  SCREENHOURLYLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String SCREENDAILYLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "screendailylog_setting__key_pref_retention_period";
  public static final int SCREENDAILYLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 92;
  public void setScreenDailyLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(SCREENDAILYLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getScreenDailyLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(SCREENDAILYLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  SCREENDAILYLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String SCREENMONTHLYLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "screenmonthlylog_setting__key_pref_retention_period";
  public static final int SCREENMONTHLYLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 3;
  public void setScreenMonthlyLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(SCREENMONTHLYLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getScreenMonthlyLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(SCREENMONTHLYLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  SCREENMONTHLYLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String LOADAVGLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "loadavglog_setting__key_pref_retention_period";
  public static final int LOADAVGLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 7;
  public void setLoadAvgLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(LOADAVGLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getLoadAvgLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(LOADAVGLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  LOADAVGLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String CPUUTILIZATIONLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "cpuutilizationlog_setting__key_pref_retention_period";
  public static final int CPUUTILIZATIONLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 7;
  public void setCpuUtilizationLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(CPUUTILIZATIONLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getCpuUtilizationLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(CPUUTILIZATIONLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  CPUUTILIZATIONLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String MEMUTILIZATIONLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "memutilizationlog_setting__key_pref_retention_period";
  public static final int MEMUTILIZATIONLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 7;
  public void setMemUtilizationLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(MEMUTILIZATIONLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getMemUtilizationLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(MEMUTILIZATIONLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  MEMUTILIZATIONLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }

  public static final String NETSTATLOG_SETTING__KEY_PREF_RETENTION_PERIOD = "netstatlog_setting__key_pref_retention_period";
  public static final int NETSTATLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD = 7;
  public void setNetStatLogSetting__RetentionPeriod(int v) {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);  
    sharedPreferences
      .edit()
      .putInt(NETSTATLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number", v)
      .commit();  
  }
  public int getNetStatLogSetting__RetentionPeriod() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);  
    return sharedPreferences.getInt(NETSTATLOG_SETTING__KEY_PREF_RETENTION_PERIOD + "_Number",  NETSTATLOG_SETTING__DEFAULT_PREF_RETENTION_PERIOD);
  }



  public static Prefs getInstance(Context context) {
    if (mPrefs == null) {
      mPrefs = new Prefs(context);
    }
    return mPrefs;
  }

  private Prefs(Context context) {
    try {
      mContext = context.createPackageContext(PACKAGE_NAME, Context.CONTEXT_RESTRICTED);  
    }
    catch (NameNotFoundException ex) {
      Log.e(TAG, ex.getLocalizedMessage());
    }
  }



}
