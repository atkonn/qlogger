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
import jp.co.qsdn.android.qlogger.core.NetStatLog;
import jp.co.qsdn.android.qlogger.view.adapter.holder.NetStatLogViewHolder;



public class NetStatLogListAdapter
  extends ArrayAdapter {  
  private final String TAG = getClass().getName();
  
  private List items;  
  private LayoutInflater inflater;  
  private static final SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
  
  public NetStatLogListAdapter(Context context, int textViewResourceId,  List items) {  
    super(context, textViewResourceId, items);  
    this.items = items;  
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
  }  

  @Override  
  public View getView(int position, View convertView, ViewGroup parent) {  
    NetStatLogViewHolder holder = null;
    if (convertView == null) {  
      convertView = inflater.inflate(R.layout.netstatlog__row, null);  
      holder = new NetStatLogViewHolder();
      holder.timestampText = (TextView)convertView.findViewById(R.id.timestamp);
      holder.recvByteText = (TextView)convertView.findViewById(R.id.recv_byte);
      holder.recvPacketText = (TextView)convertView.findViewById(R.id.recv_packet);
      holder.recvErrText = (TextView)convertView.findViewById(R.id.recv_err);
      holder.sendByteText = (TextView)convertView.findViewById(R.id.send_byte);
      holder.sendPacketText = (TextView)convertView.findViewById(R.id.send_packet);
      holder.sendErrText = (TextView)convertView.findViewById(R.id.send_err);
      convertView.setTag(holder);
    }  
    else {
      holder = (NetStatLogViewHolder)convertView.getTag();
    }
    if (isEnabled(position)) {
      NetStatLog item = (NetStatLog)items.get(position);
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
        if (holder.recvByteText != null) {
          String fmt = getContext().getResources().getString(R.string.netstatlog__recv_byte_fmt);
          holder.recvByteText.setText(String.format(fmt, (float)item.getRecvByte() / 1024f));  
        }
        if (holder.recvPacketText != null) {
          String fmt = getContext().getResources().getString(R.string.netstatlog__recv_packet_fmt);
          holder.recvPacketText.setText(String.format(fmt, item.getRecvPacket()));  
        }
        if (holder.recvErrText != null) {
          String fmt = getContext().getResources().getString(R.string.netstatlog__recv_err_fmt);
          holder.recvErrText.setText(String.format(fmt, item.getRecvErr()));  
        }
        if (holder.sendByteText != null) {
          String fmt = getContext().getResources().getString(R.string.netstatlog__send_byte_fmt);
          holder.sendByteText.setText(String.format(fmt, (float)item.getSendByte() / 1024f));  
        }
        if (holder.sendPacketText != null) {
          String fmt = getContext().getResources().getString(R.string.netstatlog__send_packet_fmt);
          holder.sendPacketText.setText(String.format(fmt, item.getSendPacket()));
        }
        if (holder.sendErrText != null) {
          String fmt = getContext().getResources().getString(R.string.netstatlog__send_err_fmt);
          holder.sendErrText.setText(String.format(fmt, item.getSendErr()));
        }
      }
    }
    else {
      if (holder.timestampText != null) {
        holder.timestampText.setText("");
      }
      if (holder.recvByteText != null) {
        holder.recvByteText.setText("");
      }
      if (holder.recvPacketText != null) {
        holder.recvPacketText.setText("");
      }
      if (holder.recvErrText != null) {
        holder.recvErrText.setText("");
      }
      if (holder.sendByteText != null) {
        holder.sendByteText.setText("");
      }
      if (holder.sendPacketText != null) {
        holder.sendPacketText.setText("");
      }
      if (holder.sendErrText != null) {
        holder.sendErrText.setText("");
      }
    }
    return convertView;  
  }  

  @Override
  public boolean isEnabled(int position) {
    if (items != null && items.size() > position) {
      NetStatLog netStatLog = (NetStatLog)items.get(position);
      if (netStatLog.getId() != -1) {
        return true;
      }
    }
    return false;
  }
}
