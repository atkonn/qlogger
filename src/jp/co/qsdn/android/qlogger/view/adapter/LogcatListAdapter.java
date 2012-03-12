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
package jp.co.qsdn.android.qlogger.view.adapter;

import android.content.Context;

import android.graphics.Typeface;

import android.text.Html;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.List;

import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.core.LogLine;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.view.adapter.holder.LogcatViewHolder;



public class LogcatListAdapter
  extends ArrayAdapter {  
  
  private List<LogLine> items;  
  private LayoutInflater inflater;  
  private static final SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
  
  public LogcatListAdapter(Context context, int textViewResourceId,  List<LogLine> items) {  
    super(context, textViewResourceId, items);  
    this.items = items;  
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
  }  

  @Override  
  public View getView(int position, View convertView, ViewGroup parent) {  
    LogcatViewHolder holder = null;
    if (convertView == null) {  
      convertView = inflater.inflate(R.layout.logcat__row, null);  
      holder = new LogcatViewHolder();
      holder.logcatRowLayout = (LinearLayout)convertView.findViewById(R.id.logcat__row_layout);
      holder.loglevelIcon = (ImageView)convertView.findViewById(R.id.loglevel_icon);
      holder.timestampText = (TextView)convertView.findViewById(R.id.timestamp);
      holder.tagText = (TextView)convertView.findViewById(R.id.tag);
      holder.messageText = (TextView)convertView.findViewById(R.id.message);
      convertView.setTag(holder);
    }  
    else {
      holder = (LogcatViewHolder)convertView.getTag();
    }
    LogLine logLine = (LogLine)items.get(position);
    int color = 0;
    if (holder.loglevelIcon != null) {
      switch (logLine.getIntLogLevel()) {
      case LogLine.LOG_LEVEL_VERBOSE:
        holder.loglevelIcon.setImageResource(R.drawable.ic_verbose);
        color = Prefs.getInstance(getContext()).getLogcatSetting__VerboseColor();
        break;

      case LogLine.LOG_LEVEL_DEBUG:
        holder.loglevelIcon.setImageResource(R.drawable.ic_debug);
        color = Prefs.getInstance(getContext()).getLogcatSetting__DebugColor();
        break;

      case LogLine.LOG_LEVEL_INFORMATION:
        holder.loglevelIcon.setImageResource(R.drawable.ic_info);
        color = Prefs.getInstance(getContext()).getLogcatSetting__InfoColor();
        break;

      case LogLine.LOG_LEVEL_WARNING:
        holder.loglevelIcon.setImageResource(R.drawable.ic_warning);
        color = Prefs.getInstance(getContext()).getLogcatSetting__WarningColor();
        break;

      case LogLine.LOG_LEVEL_ERROR:
        holder.loglevelIcon.setImageResource(R.drawable.ic_error);
        holder.logcatRowLayout.setBackgroundColor(Prefs.getInstance(getContext()).getLogcatSetting__ErrorColor());
        color = Prefs.getInstance(getContext()).getLogcatSetting__ErrorColor();
        break;

      case LogLine.LOG_LEVEL_FATAL:
        holder.loglevelIcon.setImageResource(R.drawable.ic_fatal);
        color = Prefs.getInstance(getContext()).getLogcatSetting__FatalColor();
        break;

      default:
        break;
      }
    }

    holder.logcatRowLayout.setBackgroundColor(0x66000000 | color);
    if (holder.timestampText != null) {
      holder.timestampText.setText(logLine.getTimestamp().toString());
    }
    if (holder.tagText != null) {
      holder.tagText.setText(logLine.getTag());
    }
    if (holder.messageText != null) {
      holder.messageText.setText(logLine.getMessage());
    }
    return convertView;  
  }  

  @Override
  public boolean isEnabled(int position) {
    return false;
  }
}
