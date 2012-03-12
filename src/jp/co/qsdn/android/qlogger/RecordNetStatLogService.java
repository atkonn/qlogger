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
package jp.co.qsdn.android.qlogger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;

import android.net.Uri;

import android.os.IBinder;
import android.os.PowerManager;

import android.text.TextUtils;

import android.util.Log;

import java.io.FileReader;
import java.io.LineNumberReader;

import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.qsdn.android.qlogger.core.NetStatLog;
import jp.co.qsdn.android.qlogger.core.TempNetStatLog;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.provider.NetStatLogProvider;
import jp.co.qsdn.android.qlogger.provider.TempNetStatLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;


public class RecordNetStatLogService
  extends AbstractExecutableService 
{
  private final String TAG = getClass().getName();
  public static final String SERVICE_NAME = RecordNetStatLogService.class.getCanonicalName();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  private SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyyMMdd");

  private static final String routePattern = "^([0-9a-zA-Z]+)\\s+([0-9a-fA-F]+)\\s+([0-9a-fA-F]+)\\s+.*$";
  private static String devPattern;
  private static Pattern routeRegex;
  private static Pattern devRegex;

  static {
    routeRegex = Pattern.compile(routePattern);
    String face = "([0-9a-zA-Z]+):";
    String sp   = "\\s+";
    String recv_bytes      = "([0-9]+)";
    String recv_packets    = "([0-9]+)";
    String recv_errs       = "([0-9]+)";
    String recv_drop       = "[0-9]+";
    String recv_fifo       = "[0-9]+";
    String recv_frame      = "[0-9]+";
    String recv_compressed = "[0-9]+";
    String recv_multicast  = "[0-9]+";
    String send_bytes      = "([0-9]+)";
    String send_packets    = "([0-9]+)";
    String send_errs       = "([0-9]+)";
    String send_drop       = "[0-9]+";
    String send_fifo       = "[0-9]+";
    String send_frame      = "[0-9]+";
    String send_compressed = "[0-9]+";
    String send_multicast  = "[0-9]+";
    devPattern = "^\\s*";
    devPattern += face + sp;
    devPattern += recv_bytes + sp;
    devPattern += recv_packets + sp;
    devPattern += recv_errs    + sp;
    devPattern += recv_drop    + sp;
    devPattern += recv_fifo    + sp;
    devPattern += recv_frame   + sp;
    devPattern += recv_compressed + sp;
    devPattern += recv_multicast  + sp;
    devPattern += send_bytes + sp;
    devPattern += send_packets + sp;
    devPattern += send_errs    + sp;
    devPattern += send_drop    + sp;
    devPattern += send_fifo    + sp;
    devPattern += send_frame   + sp;
    devPattern += send_compressed + sp;
    devPattern += send_multicast;
    devPattern += "$";
    if (Constant.DEBUG)Log.d("TEST", "devPattern:[" + devPattern + "]");
    devRegex = Pattern.compile(devPattern);
  };


  Runnable command = null;
  
  @Override
  public void onCreate() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onCreate");
    super.onCreate();

    command = new Runnable() {
      @Override
      public void run() {
        if (Constant.DEBUG) Log.d(TAG, ">>> run()");

        TempNetStatLog tempNetStatLog = getTempNetStatLog();
        NetStatLog netStatLog = getNetStat();
        if (netStatLog == null) {
          if (Constant.DEBUG) Log.d(TAG, "<<< run()");
          return;
        }

        if (tempNetStatLog == null) {
          long nowTime = System.currentTimeMillis();
          ContentValues contentValues = new ContentValues();
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.RECV_BYTE,      netStatLog.getRecvByte());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.RECV_PACKET,    netStatLog.getRecvPacket());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.RECV_ERR,       netStatLog.getRecvErr());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.SEND_BYTE,      netStatLog.getSendByte());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.SEND_PACKET,    netStatLog.getSendPacket());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.SEND_ERR,       netStatLog.getSendErr());
          getContentResolver().insert(TempNetStatLogProvider.CONTENT_URI, contentValues);
        }
        else {
          ContentValues contentValues = new ContentValues();
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.RECV_BYTE,      netStatLog.getRecvByte());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.RECV_PACKET,    netStatLog.getRecvPacket());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.RECV_ERR,       netStatLog.getRecvErr());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.SEND_BYTE,      netStatLog.getSendByte());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.SEND_PACKET,    netStatLog.getSendPacket());
          contentValues.put(TempNetStatLogProvider.COLUMN_NAME.SEND_ERR,       netStatLog.getSendErr());
          Uri uri = ContentUris.withAppendedId(TempNetStatLogProvider.CONTENT_URI, tempNetStatLog.getId());
          getContentResolver().update(uri, contentValues, null, null);

          long nowTime = System.currentTimeMillis();
          long recvByte   = (netStatLog.getRecvByte() < tempNetStatLog.getRecvByte()) 
                              ? netStatLog.getRecvByte() 
                              : netStatLog.getRecvByte()   - tempNetStatLog.getRecvByte();
          long recvPacket = (netStatLog.getRecvPacket() < tempNetStatLog.getRecvPacket()) 
                              ? netStatLog.getRecvPacket() 
                              : netStatLog.getRecvPacket()   - tempNetStatLog.getRecvPacket();
          long recvErr    = (netStatLog.getRecvErr() < tempNetStatLog.getRecvErr()) 
                              ? netStatLog.getRecvErr() 
                              : netStatLog.getRecvErr()   - tempNetStatLog.getRecvErr();
          long sendByte   = (netStatLog.getSendByte() < tempNetStatLog.getSendByte()) 
                              ? netStatLog.getSendByte() 
                              : netStatLog.getSendByte()   - tempNetStatLog.getSendByte();
          long sendPacket = (netStatLog.getSendPacket() < tempNetStatLog.getSendPacket()) 
                              ? netStatLog.getSendPacket() 
                              : netStatLog.getSendPacket()   - tempNetStatLog.getSendPacket();
          long sendErr    = (netStatLog.getSendErr() < tempNetStatLog.getSendErr()) 
                              ? netStatLog.getSendErr() 
                              : netStatLog.getSendErr()   - tempNetStatLog.getSendErr();
          recvByte = recvByte / 1024;
          sendByte = sendByte / 1024;

          contentValues = new ContentValues();
          contentValues.put(NetStatLogProvider.COLUMN_NAME.YYYYMMDD,       yyyymmddFormat.format(nowTime));
          contentValues.put(NetStatLogProvider.COLUMN_NAME.RECV_BYTE,      recvByte);
          contentValues.put(NetStatLogProvider.COLUMN_NAME.RECV_PACKET,    recvPacket);
          contentValues.put(NetStatLogProvider.COLUMN_NAME.RECV_ERR,       recvErr);
          contentValues.put(NetStatLogProvider.COLUMN_NAME.SEND_BYTE,      sendByte);
          contentValues.put(NetStatLogProvider.COLUMN_NAME.SEND_PACKET,    sendPacket);
          contentValues.put(NetStatLogProvider.COLUMN_NAME.SEND_ERR,       sendErr);
          contentValues.put(NetStatLogProvider.COLUMN_NAME.CREATED_ON_LONG,nowTime);
          contentValues.put(NetStatLogProvider.COLUMN_NAME.CREATED_ON,     dateFormat.format(nowTime));
          getContentResolver().insert(NetStatLogProvider.CONTENT_URI, contentValues);
        }
        if (Constant.DEBUG) Log.d(TAG, "<<< run()");
        stopSelf(); 
      }
    };


    doExecute(command);
    if (Constant.DEBUG) Log.v(TAG, "<<< onCreate");
  }

  protected String getNetDevName() {
    if (Constant.DEBUG)Log.v(TAG,">>> getNetDevName");
    LineNumberReader lineNumberReader = null;
    try {
      lineNumberReader = new LineNumberReader(new FileReader(Constant.PROC.NETROUTE));
      String line = lineNumberReader.readLine(); // title
      while((line = lineNumberReader.readLine()) != null) {
        if (Constant.DEBUG)Log.d(TAG, "line:[" + line + "]");
        String devName;
        String defaultGateway;
        String destination;
        if (TextUtils.isEmpty(line)) {
          continue;
        }
        Matcher matcher = routeRegex.matcher(line);
        if (! matcher.matches()) {
          continue;
        }
        devName        = matcher.group(1);
        destination    = matcher.group(2);
        defaultGateway = matcher.group(3);
        if (Constant.DEBUG)Log.d(TAG,
          "devName:[" + devName + "] "+
          "destination:[" + destination + "] "+
          "defaultGateway:[" + defaultGateway + "] ");
        if (!TextUtils.isEmpty(devName) && !TextUtils.isEmpty(destination) && !TextUtils.isEmpty(defaultGateway)) {
          if ("00000000".equals(destination) && !"00000000".equals(defaultGateway)) {
            if (Constant.DEBUG)Log.d(TAG,
              "Found device." +
              "devName:[" + devName + "] "+
              "destination:[" + destination + "] "+
              "defaultGateway:[" + defaultGateway + "] ");
            if (Constant.DEBUG)Log.v(TAG,"<<< getNetDevName");
            return devName;
          }
        }
      }
    }
    catch (Exception ex) {
      Log.e(TAG, Constant.PROC.NETROUTE + " read failure", ex);
    }
    finally {
      if (lineNumberReader != null) {
        try {
          lineNumberReader.close();
          lineNumberReader = null;
        }
        catch (Exception ex) {}     
      }
    }
    if (Constant.DEBUG)Log.v(TAG,"<<< getNetDevName");
    return null;
  }
  protected NetStatLog getNetStat() {
    if (Constant.DEBUG)Log.v(TAG,">>> getNetStat");
    LineNumberReader lineNumberReader = null;
    String devName = getNetDevName();
    if (null == devName) {
      if (Constant.DEBUG)Log.d(TAG, "devName is null");
      if (Constant.DEBUG)Log.v(TAG,"<<< getNetStat");
      return null;
    }
    if (Constant.DEBUG)Log.d(TAG,"devName:[" + devName + "]");
    NetStatLog netStatLog = null;
    try {
      lineNumberReader = new LineNumberReader(new FileReader(Constant.PROC.NETDEV));
      String line = lineNumberReader.readLine();
      while((line = lineNumberReader.readLine()) != null) {
        if (TextUtils.isEmpty(line)) {
          if (Constant.DEBUG)Log.d(TAG, "line is empty => continue");
          continue;
        }
        Matcher matcher = devRegex.matcher(line);
        if (! matcher.matches()) {
          if (Constant.DEBUG)Log.d(TAG, "not matches => continue line:[" + line + "]");
          continue;
        }
        if (devName.equalsIgnoreCase(matcher.group(1))) {
          netStatLog = new NetStatLog();
          netStatLog.setRecvByte(Long.parseLong(matcher.group(2)));
          netStatLog.setRecvPacket(Long.parseLong(matcher.group(3)));
          netStatLog.setRecvErr(Long.parseLong(matcher.group(4)));
          netStatLog.setSendByte(Long.parseLong(matcher.group(5)));
          netStatLog.setSendPacket(Long.parseLong(matcher.group(6)));
          netStatLog.setSendErr(Long.parseLong(matcher.group(7)));
          if (Constant.DEBUG)Log.d(TAG,"Found. devName:["+devName+"] vs ["+matcher.group(1)+"]");
          if (Constant.DEBUG)Log.v(TAG,"<<< getNetStat");
          return netStatLog;
        }
        if (Constant.DEBUG)Log.d(TAG,"NotFound. devName:["+devName+"] vs ["+matcher.group(1)+"]");
      }
    }
    catch (Exception ex) {
      Log.e(TAG, "cpuutilization read failure", ex);
    }
    finally {
      if (lineNumberReader != null) {
        try {
          lineNumberReader.close();
          lineNumberReader = null;
        }
        catch (Exception ex) {}     
      }
    }
    Log.e(TAG, "This device does not have " + Constant.PROC.NETDEV + "...?");
    if (Constant.DEBUG)Log.v(TAG,"<<< getNetStat(NotFound)");
    return netStatLog;
  }
  protected TempNetStatLog getTempNetStatLog() {
    Cursor cur = null;
    try {
      cur = getContentResolver()
                     .query(TempNetStatLogProvider.CONTENT_URI,
                       TempNetStatLogProvider.PROJECTION,
                       null,
                       null,
                       null);
  
      
      if (cur.moveToNext()) {
        TempNetStatLog temp = new TempNetStatLog();
        temp.setId(cur.getLong(TempNetStatLogProvider.COLUMN_INDEX._ID));
        temp.setRecvByte(Long.parseLong(cur.getString(TempNetStatLogProvider.COLUMN_INDEX.RECV_BYTE)));
        temp.setRecvPacket(Long.parseLong(cur.getString(TempNetStatLogProvider.COLUMN_INDEX.RECV_PACKET)));
        temp.setRecvErr(Long.parseLong(cur.getString(TempNetStatLogProvider.COLUMN_INDEX.RECV_ERR)));
        temp.setSendByte(Long.parseLong(cur.getString(TempNetStatLogProvider.COLUMN_INDEX.SEND_BYTE)));
        temp.setSendPacket(Long.parseLong(cur.getString(TempNetStatLogProvider.COLUMN_INDEX.SEND_PACKET)));
        temp.setSendErr(Long.parseLong(cur.getString(TempNetStatLogProvider.COLUMN_INDEX.SEND_ERR)));
        return temp;
      }
    }
    catch (Exception ex) {
      Log.e(TAG, "temp_net_stat retreival failure", ex);
    }
    finally {
      if (cur != null) {
        try { cur.close(); } catch (Exception ex) {}
        cur = null;
      }
    }
    return null;
  }

  
  public void onStart(Intent intent, int startId) {
    if (Constant.DEBUG) Log.v(TAG, ">>> onStart");
    super.onStart(intent, startId);
    if (Constant.DEBUG) Log.v(TAG, "<<< onStart");
  }

  
  @Override
  public void onDestroy() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onDestroy");
    super.onDestroy();
    if (Constant.DEBUG) Log.v(TAG, "<<< onDestroy");
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
