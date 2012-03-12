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
import jp.co.qsdn.android.qlogger.core.ErrorLog;
import jp.co.qsdn.android.qlogger.view.adapter.holder.ErrorLogViewHolder;



public class ErrorLogListAdapter
  extends ArrayAdapter {  
  
  private List items;  
  private LayoutInflater inflater;  
  private static final SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
  
  public ErrorLogListAdapter(Context context, int textViewResourceId,  List items) {  
    super(context, textViewResourceId, items);  
    this.items = items;  
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
  }  

  @Override  
  public View getView(int position, View convertView, ViewGroup parent) {  
    ErrorLogViewHolder holder = null;
    if (convertView == null) {  
      convertView = inflater.inflate(R.layout.errorlog_row, null);  
      holder = new ErrorLogViewHolder();
      holder.errorLogLayout = (LinearLayout)convertView.findViewById(R.id.errorlog_row_layout);
      holder.timestampText = (TextView)convertView.findViewById(R.id.timestamp);
      holder.tagText = (TextView)convertView.findViewById(R.id.tag);
      holder.messageText = (TextView)convertView.findViewById(R.id.message);
      holder.rightArrowText = (TextView)convertView.findViewById(R.id.right_arrow);
      convertView.setTag(holder);
    }  
    else {
      holder = (ErrorLogViewHolder)convertView.getTag();
    }
    if (isEnabled(position)) {
      ErrorLog item = (ErrorLog)items.get(position);
      if (item != null && holder != null) {  
        if (holder.timestampText != null) {  
          holder.timestampText.setTypeface(Typeface.DEFAULT_BOLD);  
          if (item.getTimestamp() != null) { 
            String text = fullFormat.format(item.getTimestamp().getTime());
            if (item.getUnread() == 1) {
              text += " <font color=\"#ff4a79\">(NEW)</font>";
            }
            holder.timestampText.setText(Html.fromHtml(text));
          }
          else {
            holder.timestampText.setText("");
          }
        }  
        if (holder.tagText != null) {
          holder.tagText.setText(item.getTag());  
        }
        if (holder.messageText != null) {
          holder.messageText.setText(item.getMessage());  
        }
        if (holder.rightArrowText != null) {
          holder.rightArrowText.setVisibility(View.VISIBLE);
        }
        if (holder.errorLogLayout != null) {
          if (item.getUnread() == 1) {
            holder.errorLogLayout.setBackgroundColor(0xFFFFF6D1);
          }
          else {
            holder.errorLogLayout.setBackgroundColor(0x00000000);
          }
        }
      }
    }
    else {
      if (holder.timestampText != null) {
        holder.timestampText.setText("");
      }
      if (holder.tagText != null) {
        holder.tagText.setText("");
      }
      if (holder.messageText != null) {
        holder.messageText.setText("");
      }
      if (holder.rightArrowText != null) {
        holder.rightArrowText.setVisibility(View.INVISIBLE);
      }
      if (holder.errorLogLayout != null) {
        holder.errorLogLayout.setBackgroundColor(0x00000000);
      }
    }
    return convertView;  
  }  

  @Override
  public boolean isEnabled(int position) {
    if (items != null && items.size() > position) {
      ErrorLog errorLog = (ErrorLog)items.get(position);
      if (errorLog.getId() != -1) {
        return true;
      }
    }
    return false;
  }
}
