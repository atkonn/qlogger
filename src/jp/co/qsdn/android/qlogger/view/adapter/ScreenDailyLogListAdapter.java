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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.List;

import jp.co.qsdn.android.qlogger.Constant;
import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.core.ScreenDailyLog;
import jp.co.qsdn.android.qlogger.view.adapter.holder.ScreenDailyLogViewHolder;



public class ScreenDailyLogListAdapter
  extends ArrayAdapter {  
  
  private List items;  
  private LayoutInflater inflater;  
  private static final SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
  
  public ScreenDailyLogListAdapter(Context context, int textViewResourceId,  List items) {  
    super(context, textViewResourceId, items);  
    this.items = items;  
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
  }  

  @Override  
  public View getView(int position, View convertView, ViewGroup parent) {  
    ScreenDailyLogViewHolder holder = null;
    if (convertView == null) {  
      convertView = inflater.inflate(R.layout.screendailylog__row, null);  
      holder = new ScreenDailyLogViewHolder();
      holder.ddText = (TextView)convertView.findViewById(R.id.dd);
      holder.onTimeText = (TextView)convertView.findViewById(R.id.on_time);
      convertView.setTag(holder);
    }  
    else {
      holder = (ScreenDailyLogViewHolder)convertView.getTag();
    }
    ScreenDailyLog item = (ScreenDailyLog)items.get(position);  
    if (item != null && holder != null) {  
      if (holder.ddText != null) {  
        holder.ddText.setTypeface(Typeface.DEFAULT_BOLD);  
        if (item.getDd() != null) { 
          holder.ddText.setText(String.format(getContext().getResources().getString(R.string.screendailylog__time), item.getDd()));
        }
        else {
          holder.ddText.setText("");
        }
      }  
      if (holder.onTimeText != null) {  
        if (item.getId() != -1) {
          holder.onTimeText.setText(String.format(getContext().getResources().getString(R.string.screendailylog__minutes), (item.getOnTime() / 1000d / 60d / 60d)));  
        }
        else {
          holder.onTimeText.setText("");
        }
      }
    }  
    return convertView;  
  }  

  @Override
  public boolean isEnabled(int position) {
    return false;
  }
}
