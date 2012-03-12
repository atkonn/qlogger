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

import android.util.Log;

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
import jp.co.qsdn.android.qlogger.core.BatteryLog;
import jp.co.qsdn.android.qlogger.view.adapter.holder.BatteryLogViewHolder;



public class BatteryLogListAdapter
  extends ArrayAdapter {  
  private final String TAG = getClass().getName();
  
  private List items;  
  private LayoutInflater inflater;  
  private static final SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
  
  public BatteryLogListAdapter(Context context, int textViewResourceId,  List items) {  
    super(context, textViewResourceId, items);  
    this.items = items;  
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
  }  

  @Override  
  public View getView(int position, View convertView, ViewGroup parent) {  
    BatteryLogViewHolder holder = null;
    if (convertView == null) {  
      convertView = inflater.inflate(R.layout.batterylog__row, null);  
      holder = new BatteryLogViewHolder();
      holder.timestampText = (TextView)convertView.findViewById(R.id.timestamp);
      holder.levelText = (TextView)convertView.findViewById(R.id.level);
      convertView.setTag(holder);
    }  
    else {
      holder = (BatteryLogViewHolder)convertView.getTag();
    }
    if (isEnabled(position)) {
      BatteryLog item = (BatteryLog)items.get(position);
      if (item != null && holder != null) {  
        if (holder.timestampText != null) {  
          holder.timestampText.setTypeface(Typeface.DEFAULT_BOLD);  
          if (item.getTimestamp() != null) { 
            String text = fullFormat.format(item.getTimestamp().getTime());
            holder.timestampText.setText(fullFormat.format(item.getTimestamp().getTime()));
          }
          else {
            holder.timestampText.setText("");
          }
        }  
        if (holder.levelText != null) {
          holder.levelText.setText("" + item.getLevel());  
        }
      }
    }
    else {
      if (holder.timestampText != null) {
        holder.timestampText.setText("");
      }
      if (holder.levelText != null) {
        holder.levelText.setText("");
      }
    }
    return convertView;  
  }  

  @Override
  public boolean isEnabled(int position) {
    if (items != null && items.size() > position) {
      BatteryLog batteryLog = (BatteryLog)items.get(position);
      if (batteryLog.getId() != -1) {
        return true;
      }
    }
    return false;
  }
}
