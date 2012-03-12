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
import jp.co.qsdn.android.qlogger.core.ScreenLog;
import jp.co.qsdn.android.qlogger.view.adapter.holder.ScreenLogViewHolder;



public class ScreenLogListAdapter
  extends ArrayAdapter {  
  
  private List items;  
  private LayoutInflater inflater;  
  private static final SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
  
  public ScreenLogListAdapter(Context context, int textViewResourceId,  List items) {  
    super(context, textViewResourceId, items);  
    this.items = items;  
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
  }  

  @Override  
  public View getView(int position, View convertView, ViewGroup parent) {  
    ScreenLogViewHolder holder = null;
    if (convertView == null) {  
      convertView = inflater.inflate(R.layout.screenlog__row, null);  
      holder = new ScreenLogViewHolder();
      holder.timeText = (TextView)convertView.findViewById(R.id.time);
      holder.actionNameText = (TextView)convertView.findViewById(R.id.action_name);
      holder.onTimeText = (TextView)convertView.findViewById(R.id.on_time);
      convertView.setTag(holder);
    }  
    else {
      holder = (ScreenLogViewHolder)convertView.getTag();
    }
    ScreenLog item = (ScreenLog)items.get(position);  
    if (item != null && holder != null) {  
      if (holder.timeText != null) {  
        holder.timeText.setTypeface(Typeface.DEFAULT_BOLD);  
        if (item.getCreatedOn() != null) { 
          holder.timeText.setText(fullFormat.format(item.getCreatedOn().getTime()));
        }
        else {
          holder.timeText.setText("");
        }
      }  
      if (holder.actionNameText != null) {
        if (Constant.ACTION.SCREEN_ON.equals(item.getActionName())) {
          holder.actionNameText.setText(getContext().getResources().getString(R.string.screenlog__message_of_screen_on));
        }
        else
        if (Constant.ACTION.SCREEN_OFF.equals(item.getActionName())) {
          holder.actionNameText.setText(getContext().getResources().getString(R.string.screenlog__message_of_screen_off));
        }
        else {
          holder.actionNameText.setText(item.getActionName());  
        }
      }
      if (holder.onTimeText != null) {  
        if (item.getOnTime() != 0) {
          holder.onTimeText.setText(String.format(getContext().getResources().getString(R.string.screenlog__on_time_message), (double)item.getOnTime() / 1000d));  
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
