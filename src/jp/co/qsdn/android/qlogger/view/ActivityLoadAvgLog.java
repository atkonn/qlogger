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
import android.app.AlertDialog;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.util.Log;

import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.qsdn.android.qlogger.Constant;
import jp.co.qsdn.android.qlogger.LogcatService;
import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.core.LoadAvgLog;
import jp.co.qsdn.android.qlogger.provider.LoadAvgLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.adapter.LoadAvgLogListAdapter;
import jp.co.qsdn.android.qlogger.view.flipper.ViewFlipper;

public class ActivityLoadAvgLog
  extends AbstractActivity
{
  final String TAG = getClass().getName();
  private int limitCount = 10;
  private int listCount = 0;
  private int pageCount = 0;
  private List dateList = new ArrayList();

  private GestureDetector gestureDetector;

  private Animation animationInFromLeft;
  private Animation animationOutToRight;
  private Animation animationInFromRight;
  private Animation animationOutToLeft;

  private ViewFlipper pagerFlipper;
  LayoutInflater inflater = null;

  View.OnTouchListener touchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent event) {
      if (gestureDetector.onTouchEvent(event)) {
        return true;
      }
      return false;
    }
  };

  protected void nextPage() {
    getHandler().post(new Runnable() {
      @Override
      public void run() {
        if (getStartPos() < pageCount - 1) {
          pagerFlipper.setInAnimation(animationInFromRight);
          pagerFlipper.setOutAnimation(animationOutToLeft);
          pagerFlipper.showNext();
          setStartPos(getStartPos()+1);
          View view = pagerFlipper.getCurrentView();
          ListView listView = (ListView)view.findViewById(R.id.list);
          if (listView.getAdapter() == null) {
            setupListView(view, getStartPos());
          }
          setupPagerAndClearButton();
        }
      }
    });
  }
  protected void prevPage() {
    getHandler().post(new Runnable() {
      @Override
      public void run() {
        if (getStartPos() > 0) {
          pagerFlipper.setInAnimation(animationInFromLeft);
          pagerFlipper.setOutAnimation(animationOutToRight);
          pagerFlipper.showPrevious();
          setStartPos(getStartPos() - 1);
          View view = pagerFlipper.getCurrentView();
          ListView listView = (ListView)view.findViewById(R.id.list);
          if (listView.getAdapter() == null) {
            setupListView(view, getStartPos());
          }
          setupPagerAndClearButton();
        }
      }
    });
  }

  OnGestureListener gestureListener = new OnGestureListener() {
    @Override
    public boolean onDown(MotionEvent arg0) {
      return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      float dx = Math.abs(velocityX);
      float dy = Math.abs(velocityY);
      if (dx > dy && dx > 100) {
        if (e1.getX() < e2.getX()) {
          prevPage();
        } 
        else {
          nextPage();
        }
        return true;
      }
      return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
      return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
      return false;
    }
  };
  @Override
  protected String getTitleText() {
    return getResources().getString(R.string.dashboard_item_screenhourlylog_title);
  }
  protected ListView setupListView(View view, int index) {
    ListView listView = (ListView)view.findViewById(R.id.list);

    TextView textView = (TextView)view.findViewById(R.id.date);
    String date = (String)dateList.get(index);
    textView.setText(String.format(getResources().getString(R.string.loadavglog__column_title), date.substring(0,4), date.substring(4,6), date.substring(6,8)));

    final ArrayAdapter adapter = new LoadAvgLogListAdapter(this, 0, getCurrentList(index));
    listView.setAdapter(adapter);
    listView.setOnTouchListener(touchListener);
    return listView;
  }

  @Override
  protected void onCreateBottomHalf() {
    startWatchingExternalStorage();
    setupInitData();

    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    pagerFlipper = (ViewFlipper)findViewById(R.id.pagerFlipper);
    for (int ii=0; ii<pageCount; ii++) {
      View view = inflater.inflate(R.layout.loadavglog__list, null);
      pagerFlipper.addView(view);
      if (ii == 0) {
        setupListView(view, ii);
      }
      else {
        ListView listView = (ListView)view.findViewById(R.id.list);
        listView.setAdapter(null);
      }
    }

    TextView textView = (TextView)findViewById(R.id.title_text);
    textView.setText(getResources().getString(R.string.dashboard_item_loadavglog_title));

    textView = (TextView)findViewById(R.id.page_text);
    if (pageCount > 0) {
      textView.setText("1/" + pageCount);
    }
    else {
      textView.setText("0/0");
    }

    ImageView imageView = (ImageView)findViewById(R.id.title_icon);
    imageView.setImageResource(R.drawable.icon);

    setupPagerAndClearButton();
    if (pageCount > 0) {
      switchViewToMain();
    }
    else {
      switchViewToNotFound();
    }
  }
  protected void setupPagerAndClearButton() {
    ImageView imageView;

    imageView = (ImageView)findViewById(R.id.action_bar_prev);
    if (imageView != null) {
      imageView.setImageResource(R.drawable.action_bar_prev);
      if (getStartPos() == 0) {
        imageView.setEnabled(false);
      }
      else {
        imageView.setEnabled(true);
      }
    }
    imageView = (ImageView)findViewById(R.id.action_bar_next);
    if (imageView  != null) {
      imageView.setImageResource(R.drawable.action_bar_next);
      if (getStartPos() + 1 >= pageCount) {
        imageView.setEnabled(false);
      }
      else {
        imageView.setEnabled(true);
      }
    }
    imageView = (ImageView)findViewById(R.id.action_bar_clear);
    if (imageView != null) {
      imageView.setImageResource(R.drawable.action_bar_clear);
      if (pageCount == 0) {
        imageView.setEnabled(false);
      }
      else {
        imageView.setEnabled(true);
      }
    }
    imageView = (ImageView)findViewById(R.id.action_bar_send);
    if (imageView != null) {
      imageView.setImageResource(R.drawable.action_bar_send);
      if (pageCount == 0) {
        imageView.setEnabled(false);
      }
      else {
        imageView.setEnabled(true);
      }
    }

    TextView textView = (TextView)findViewById(R.id.page_text);
    if (pageCount == 0) {
      textView.setText("0/0");
    }
    else {
      textView.setText((getStartPos() + 1) + "/" + pageCount);
    }
  }

  @Override
  protected int getContentViewResourceId() {
    return R.layout.loadavglog;
  }

  @Override
  protected int getTitleViewResourceId() {
    return R.layout.loadavglog__action_bar;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void setupActionBarBottomHalf() {
  }

  protected ArrayList<LoadAvgLog> currentList = new ArrayList<LoadAvgLog>();
  protected List<LoadAvgLog> getCurrentList(int startpos) {
    List<LoadAvgLog> list = new ArrayList<LoadAvgLog>();
    synchronized (currentList) {
      String yyyymmdd = (String)dateList.get(startpos);
  
      Cursor cur = null;
      try {
        cur = getContentResolver()
                       .query(LoadAvgLogProvider.CONTENT_URI, 
                         LoadAvgLogProvider.PROJECTION,
                         LoadAvgLogProvider.COLUMN_NAME.YYYYMMDD + " = ? ",
                         new String[] {
                           yyyymmdd
                         },
                         LoadAvgLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
        
        currentList.clear();
        while (cur.moveToNext()) {
          LoadAvgLog loadAvgLog = new LoadAvgLog(); 
          loadAvgLog.setId(cur.getLong(LoadAvgLogProvider.COLUMN_INDEX._ID));
          loadAvgLog.setYyyymmdd(cur.getString(LoadAvgLogProvider.COLUMN_INDEX.YYYYMMDD));
          loadAvgLog.setOne(cur.getFloat(LoadAvgLogProvider.COLUMN_INDEX.ONE));
          loadAvgLog.setFive(cur.getFloat(LoadAvgLogProvider.COLUMN_INDEX.FIVE));
          loadAvgLog.setTen(cur.getFloat(LoadAvgLogProvider.COLUMN_INDEX.TEN));
          loadAvgLog.setCreatedOn(Timestamp.valueOf(cur.getString(LoadAvgLogProvider.COLUMN_INDEX.CREATED_ON)));
          loadAvgLog.setCreatedOnLong(cur.getLong(LoadAvgLogProvider.COLUMN_INDEX.CREATED_ON_LONG));
          currentList.add(loadAvgLog);
          list.add(loadAvgLog);
        }
      }
      finally {
        if (cur != null) {
          try { cur.close(); } catch (Exception ex) {}
          cur = null;
        }
      }
    };
    for (int ii=list.size(); ii<limitCount; ii++) {
      LoadAvgLog loadAvgLog = new LoadAvgLog(); 
      loadAvgLog.setId(-1);
      list.add(loadAvgLog);
    }
    return list;
  }
  protected void setupInitData() {
    setStartPos(0);
    Cursor cur = null;
    try {
      cur = getContentResolver()
                    .query(LoadAvgLogProvider.CONTENT_URI_WITH_GROUPBY_YYYYMMDD,
                      new String[] {
                        LoadAvgLogProvider.COLUMN_NAME.YYYYMMDD
                      },
                      null,
                      null,
                      LoadAvgLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
      dateList.clear(); 
      while (cur.moveToNext()) {
        dateList.add(cur.getString(0));
      }
    }
    finally {
      if (cur != null) {
        try { cur.close(); } catch (Exception ex) {}
        cur = null;
      }
    }
    pageCount = dateList.size();
    Log.d(TAG, "listCount:[" + listCount + "]");

    gestureDetector = new GestureDetector(this, gestureListener);
    animationInFromLeft  = AnimationUtils.loadAnimation(this, R.anim.in_from_left);
    animationOutToRight  = AnimationUtils.loadAnimation(this, R.anim.out_to_right);
    animationInFromRight = AnimationUtils.loadAnimation(this, R.anim.in_from_right);
    animationOutToLeft   = AnimationUtils.loadAnimation(this, R.anim.out_to_left);
  }

  public void gotoPrev(View v) {
    prevPage();
  }
  public void gotoNext(View v) {
    nextPage();
  }
  public void gotoClear(View v) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityLoadAvgLog.this);
    alertDialogBuilder.setTitle(getResources().getString(R.string.loadavglog__title_of_delete_dialog));
    alertDialogBuilder.setMessage(getResources().getString(R.string.loadavglog__message_of_delete_dialog));
    alertDialogBuilder.setPositiveButton(R.string.loadavglog__button_of_delete_dialog_positive, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        int result = getContentResolver().delete(LoadAvgLogProvider.CONTENT_URI, null, null);
        Toast.makeText(ActivityLoadAvgLog.this, 
                       String.format(getResources().getString(R.string.loadavglog__message_of_delete_completion), result), 
                       Toast.LENGTH_LONG).show();
        pagerFlipper.removeAllViews();
        onCreateBottomHalf();
      }
    });
    alertDialogBuilder.setNegativeButton(R.string.loadavglog__button_of_delete_dialog_negative, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    alertDialogBuilder.create().show();
  }
  public void gotoSetting(View v) {
    Intent intent = new Intent(this, ActivityLoadAvgLogSetting.class);
    startActivity(intent);
  }
  public void gotoGraph(View v) {
    Intent intent = new Intent(this, ActivityLoadAvgLogGraph.class);
    intent.putExtra("startPos", getStartPos());
    startActivity(intent);
  }
  protected final static String LOADAVGLOGS_CSV = "loadavglogs.csv";
  public void gotoSend(final View v) {
    if (! getExternalStorageAvailable() || ! getExternalStorageWriteable()) {
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityLoadAvgLog.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.loadavglog__title_of_sdcard_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.loadavglog__message_of_sdcard_error));
      alertDialogBuilder.setPositiveButton(R.string.loadavglog__button_of_sdcard_error_ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          gotoSend(v);
          dialog.dismiss();
        }
      });
      alertDialogBuilder.setNegativeButton(R.string.loadavglog__button_of_sdcard_error_negative, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
      alertDialogBuilder.create().show();
      return;
    }
    String filePath = Environment.getExternalStorageDirectory() + File.separator + LOADAVGLOGS_CSV;
    String zipFilePath = Environment.getExternalStorageDirectory() + File.separator + LOADAVGLOGS_CSV + ".zip";
    File file = new File(filePath);
    file.getParentFile().mkdir();
    OutputStreamWriter out = null;
    try {
      out = new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8");
      synchronized(currentList) {
        int ii=0;
        for (LoadAvgLog loadAvgLog: currentList) {
          if (ii==0) {
            out.write(loadAvgLog.toCsvTitle());
            out.write("\n");
            ii++;
          }
          out.write(loadAvgLog.toCsv());
          out.write("\n");
        }
        out.flush();
      };
    }
    catch (IOException ex) {
      Log.e(TAG, "fileOutput failure", ex);
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityLoadAvgLog.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.loadavglog__title_of_file_output_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.loadavglog__message_of_file_output_error));
      alertDialogBuilder.setPositiveButton(R.string.loadavglog__button_of_file_output_error, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
      alertDialogBuilder.create().show();
      return;
    }
    finally {
      if (out != null) {
        try {
          out.close();
        }
        catch (Exception ex) {}
      }
    }
    
    try {
      Util.zip(zipFilePath, filePath);
    }
    catch (Exception ex) {
      Log.e(TAG, "zip failure.", ex);
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityLoadAvgLog.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.loadavglog__title_of_zip_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.loadavglog__message_of_zip_error));
      alertDialogBuilder.setPositiveButton(R.string.loadavglog__button_of_zip_error, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
      alertDialogBuilder.create().show();
      return;
    }
    (new File(filePath)).delete();
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.loadavglog__title_of_mail));
    intent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.loadavglog__text_of_mail), dateList.get(getStartPos())));
    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + zipFilePath));
    intent.setType("application/zip");
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(Intent.createChooser(intent, getResources().getString(R.string.loadavglog__message_of_client_chooser)));
  }

  @Override
  public void onDestroy() {
    if (getExternalStorageReceiver() != null) {
      try {
        unregisterReceiver(getExternalStorageReceiver());
      }
      catch (IllegalArgumentException ex) {}
      setExternalStorageReceiver(null);
    }
    super.onDestroy();
  }

  @Override
  public void changePage(int newStartPos) {
    throw new UnsupportedOperationException();
  }
}
