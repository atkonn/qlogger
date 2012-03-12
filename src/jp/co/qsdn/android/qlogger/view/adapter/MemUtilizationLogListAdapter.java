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
import jp.co.qsdn.android.qlogger.core.MemUtilizationLog;
import jp.co.qsdn.android.qlogger.view.adapter.holder.MemUtilizationLogViewHolder;



public class MemUtilizationLogListAdapter
  extends ArrayAdapter {  
  private final String TAG = getClass().getName();
  
  private List items;  
  private LayoutInflater inflater;  
  private static final SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
  
  public MemUtilizationLogListAdapter(Context context, int textViewResourceId,  List items) {  
    super(context, textViewResourceId, items);  
    this.items = items;  
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
  }  

  @Override  
  public View getView(int position, View convertView, ViewGroup parent) {  
    MemUtilizationLogViewHolder holder = null;
    if (convertView == null) {  
      convertView = inflater.inflate(R.layout.memutilizationlog__row, null);  
      holder = new MemUtilizationLogViewHolder();
      holder.timestampText = (TextView)convertView.findViewById(R.id.timestamp);
      holder.totalText = (TextView)convertView.findViewById(R.id.total);
      holder.titleTotalText = (TextView)convertView.findViewById(R.id.title_total);
      holder.freeText = (TextView)convertView.findViewById(R.id.free);
      holder.titleFreeText = (TextView)convertView.findViewById(R.id.title_free);
      holder.cacheText = (TextView)convertView.findViewById(R.id.cache);
      holder.titleCacheText = (TextView)convertView.findViewById(R.id.title_cache);
      convertView.setTag(holder);
    }  
    else {
      holder = (MemUtilizationLogViewHolder)convertView.getTag();
    }
    if (isEnabled(position)) {
      MemUtilizationLog item = (MemUtilizationLog)items.get(position);
      if (item != null && holder != null) {  
        if (holder.timestampText != null) {  
          holder.timestampText.setTypeface(Typeface.DEFAULT_BOLD);  
          if (item.getCreatedOn() != null) { 
            String text = fullFormat.format(item.getCreatedOn().getTime());
            holder.timestampText.setText(fullFormat.format(item.getCreatedOn().getTime()));
          }
          else {
            holder.timestampText.setText("");
          }
        }  
        if (holder.totalText != null) {
          holder.totalText.setText(String.format("%d kB", item.getTotal()));  
        }
        if (holder.titleTotalText != null) {
          holder.titleTotalText.setText(getContext().getResources().getString(R.string.memutilizationlog__row_total));
        }
        if (holder.freeText != null) {
          holder.freeText.setText(String.format("%d kB", item.getFree()));  
        }
        if (holder.titleFreeText != null) {
          holder.titleFreeText.setText(getContext().getResources().getString(R.string.memutilizationlog__row_free));
        }
        if (holder.cacheText != null) {
          holder.cacheText.setText(String.format("%d kB", item.getCached()));
        }
        if (holder.titleCacheText != null) {
          holder.titleCacheText.setText(getContext().getResources().getString(R.string.memutilizationlog__row_cache));
        }
      }
    }
    else {
      if (holder.timestampText != null) {
        holder.timestampText.setText("");
      }
      if (holder.totalText != null) {
        holder.totalText.setText("");
      }
      if (holder.titleTotalText != null) {
        holder.titleTotalText.setText("");
      }
      if (holder.freeText != null) {
        holder.freeText.setText("");
      }
      if (holder.titleFreeText != null) {
        holder.titleFreeText.setText("");
      }
      if (holder.cacheText != null) {
        holder.cacheText.setText("");
      }
      if (holder.titleCacheText != null) {
        holder.titleCacheText.setText("");
      }
    }
    return convertView;  
  }  

  @Override
  public boolean isEnabled(int position) {
    if (items != null && items.size() > position) {
      MemUtilizationLog memUtilizationLog = (MemUtilizationLog)items.get(position);
      if (memUtilizationLog.getId() != -1) {
        return true;
      }
    }
    return false;
  }
}
