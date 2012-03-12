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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint.Align;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.text.TextUtils;
import android.text.TextUtils;

import android.util.Log;

import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.qsdn.android.qlogger.Constant;
import jp.co.qsdn.android.qlogger.LogcatService;
import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.core.ScreenHourlyLog;
import jp.co.qsdn.android.qlogger.provider.ScreenHourlyLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.adapter.ScreenHourlyLogListAdapter;
import jp.co.qsdn.android.qlogger.view.chart.GraphicalView;
import jp.co.qsdn.android.qlogger.view.flipper.ViewFlipper;
import jp.co.qsdn.android.qlogger.cache.ImageCache;

import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class ActivityScreenHourlyLogGraph
  extends AbstractActivity
{
  final String TAG = getClass().getName();
  public static class EXTRA_PARAM {
    public static final String START_POS = "startPos";
  }
  private int limitCount = 24;
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
          setupGraphView(view, getStartPos());
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
          setStartPos(getStartPos()-1);
          View view = pagerFlipper.getCurrentView();
          setupGraphView(view, getStartPos());
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
    return getResources().getString(R.string.screenhourlylog__text_of_title);
  }

  protected XYMultipleSeriesDataset getCurrentData(int startpos) {
    if (Constant.DEBUG) Log.v(TAG, ">>> getCurrentData");
    String yyyymmdd = (String)dateList.get(startpos);
    Log.d(TAG, "yyyymmdd:[" + yyyymmdd + "]");

    Cursor cur = null;
    List<ScreenHourlyLog> list = new ArrayList<ScreenHourlyLog>();
    try {
      cur = getContentResolver()
                     .query(ScreenHourlyLogProvider.CONTENT_URI, 
                       ScreenHourlyLogProvider.PROJECTION,
                       ScreenHourlyLogProvider.COLUMN_NAME.YYYYMMDD + " = ? ",
                       new String[] {
                         yyyymmdd
                       },
                       ScreenHourlyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
      
      while (cur.moveToNext()) {
        ScreenHourlyLog screenHourlyLog = new ScreenHourlyLog(); 
        screenHourlyLog.setId(cur.getLong(ScreenHourlyLogProvider.COLUMN_INDEX._ID));
        screenHourlyLog.setYyyymmdd(cur.getString(ScreenHourlyLogProvider.COLUMN_INDEX.YYYYMMDD));
        screenHourlyLog.setHh24(cur.getString(ScreenHourlyLogProvider.COLUMN_INDEX.HH24));
        screenHourlyLog.setOnTime(cur.getLong(ScreenHourlyLogProvider.COLUMN_INDEX.ON_TIME));
        screenHourlyLog.setCreatedOnLong(cur.getLong(ScreenHourlyLogProvider.COLUMN_INDEX.CREATED_ON_LONG));
        screenHourlyLog.setCreatedOn(Timestamp.valueOf(cur.getString(ScreenHourlyLogProvider.COLUMN_INDEX.CREATED_ON)));
        if (Constant.DEBUG) Log.d(TAG, "screenHourlyLog:[" + screenHourlyLog + "]");
  
        list.add(screenHourlyLog);
      }
    }
    finally {
      if (cur != null) {
        try { cur.close(); } catch (Exception ex) {}
        cur = null;
      }
    }
    XYMultipleSeriesDataset data = new XYMultipleSeriesDataset();
    XYSeries timeSeries = new XYSeries("data");

    ScreenHourlyLog[] screenHourlyLogs = (ScreenHourlyLog[])list.toArray(new ScreenHourlyLog[list.size()]);
    for (int ii=0; ii<screenHourlyLogs.length; ii++) {
      try {
        timeSeries.add(Double.parseDouble(screenHourlyLogs[ii].getHh24()), (double)((double)screenHourlyLogs[ii].getOnTime() / 1000d / 60d));
      }
      catch (Exception ex) {
        Log.e(TAG, "series set failure.", ex);
      }
    }
    data.addSeries(timeSeries);
    if (Constant.DEBUG) Log.v(TAG, "<<< getCurrentData");
    return data;
  }
  protected XYMultipleSeriesRenderer getRenderer() {
    XYSeriesRenderer renderer = new XYSeriesRenderer();

    /*
     * "#158aea" come from http://d.hatena.ne.jp/good-speed/20110806/1312638320
     */
    renderer.setColor(Color.parseColor("#158aea"));

    XYMultipleSeriesRenderer baseRenderer = new XYMultipleSeriesRenderer(); 
    baseRenderer.addSeriesRenderer(renderer);
        
    baseRenderer.setXAxisMin(24);
    baseRenderer.setXAxisMax(0.5);
    baseRenderer.setYAxisMin(0);
    baseRenderer.setYAxisMax(60);
    baseRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.VERTICAL);
        
    baseRenderer.setShowGrid(true);

    /*
     * "#c9c9c9" come from http://d.hatena.ne.jp/good-speed/20110806/1312638320
     */
    baseRenderer.setGridColor(Color.parseColor("#c9c9c9"));

    baseRenderer.setLabelsColor(Color.parseColor("#000000"));

    baseRenderer.setPanEnabled(false, false);
    baseRenderer.setPanLimits(new double[]{0, 0, 0, 0});
    baseRenderer.setShowLegend(false);

    baseRenderer.setLabelsTextSize(20);
    baseRenderer.setXTitle(getResources().getString(R.string.screenhourlylog_graph__xtitle));
    baseRenderer.setYLabelsAlign(Align.RIGHT);
    baseRenderer.setYTitle(getResources().getString(R.string.screenhourlylog_graph__ytitle));
        
    baseRenderer.setBarSpacing(0.5);
    baseRenderer.setZoomEnabled(false, false);
    int[] margin = {20, 50, 50, 30};
    baseRenderer.setMargins(margin);
    baseRenderer.setMarginsColor(Color.parseColor("#FFFFFF"));
    
    return baseRenderer;
  }

  protected int bitmapWidth;
  protected int bitmapHeight;
  protected Bitmap currentGraphBitmap;
  protected GraphicalView setupGraphView(View view, int index) {
    if (Constant.DEBUG) Log.v(TAG, ">>> setupGraphView");
    LinearLayout graphView = (LinearLayout)view.findViewById(R.id.graph);
    graphView.removeAllViews();


    BarChart chart = new BarChart(getCurrentData(index), getRenderer(), BarChart.Type.DEFAULT);
    View scrollView = (View)view.findViewById(R.id.scrollview);
    scrollView.setOnTouchListener(touchListener);

    bitmapWidth = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    bitmapHeight = getResources().getDimensionPixelSize(R.dimen.height_of_graph);

    if (currentGraphBitmap == null) {
      currentGraphBitmap = ImageCache.getImage(bitmapWidth + "x" + bitmapHeight);
      if (currentGraphBitmap == null) {
        currentGraphBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
        ImageCache.setImage(bitmapWidth + "x" + bitmapHeight, currentGraphBitmap);
      }
    }

    currentGraphBitmap.eraseColor(Color.WHITE);
    GraphicalView chartView =  new GraphicalView(getApplicationContext(), chart, currentGraphBitmap);
    chartView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, bitmapHeight));
    graphView.addView(chartView);

    if (Constant.DEBUG) Log.v(TAG, "<<< setupGraphView");
    return chartView;
  }

  @Override
  protected void onCreateBottomHalf() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onCreateBottomHalf");
    startWatchingExternalStorage();
    setupInitData();

    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    if (Constant.DEBUG) Log.v(TAG, "<<< onCreateBottomHalf");
  }

  protected void onResumeBottomHalf() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onResumeBottomHalf");
    setStartPos(getIntent().getIntExtra(EXTRA_PARAM.START_POS, 0));
    if (Constant.DEBUG) Log.d(TAG, "startPos:[" + getStartPos() + "]");

    pagerFlipper = (ViewFlipper)findViewById(R.id.pagerFlipper);
    pagerFlipper.removeAllViews();
    for (int ii=0; ii<pageCount; ii++) {
      View view = inflater.inflate(R.layout.screenhourlylog_graph__main, null);
      pagerFlipper.addView(view);
      if (ii == 0 || ii == getStartPos()) {
        setupGraphView(view, ii);
      }
    }
    pagerFlipper.setDisplayedChild(getStartPos());

    TextView textView = (TextView)findViewById(R.id.date);
    if (textView != null && dateList.size() > 0) {
      String date = (String)dateList.get(getStartPos());
      if (! TextUtils.isEmpty(date)) {
        textView.setText(String.format(getResources().getString(R.string.screenhourlylog__column_title), date.substring(0,4), date.substring(4,6), date.substring(6,8)));
      }
    }

    textView = (TextView)findViewById(R.id.page_text);
    if (pageCount > 0) {
      textView.setText("1/" + pageCount);
    }
    else {
      textView.setText("0/0");
    }

    setupPagerAndClearButton();
    if (pageCount > 0) {
      switchViewToMain();
    }
    else {
      switchViewToNotFound();
    }
    if (Constant.DEBUG) Log.v(TAG, "<<< onResumeBottomHalf");
  }
  @Override
  protected boolean isCustomTitle() {
    return false;
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

    textView = (TextView)findViewById(R.id.date);
    if (textView != null && dateList.size() > 0) {
      String date = (String)dateList.get(getStartPos());
      if (! TextUtils.isEmpty(date)) {
        textView.setText(String.format(getResources().getString(R.string.screenhourlylog__column_title), date.substring(0,4), date.substring(4,6), date.substring(6,8)));
      }
    }
  }

  @Override
  protected int getContentViewResourceId() {
    return R.layout.screenhourlylog_graph;
  }

  @Override
  protected int getTitleViewResourceId() {
    return R.layout.screenhourlylog__action_bar2;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void setupActionBarBottomHalf() {
  }

  protected void setupInitData() {
    Cursor cur = null;
    try {
      cur = getContentResolver()
                    .query(ScreenHourlyLogProvider.CONTENT_URI_WITH_GROUPBY_YYYYMMDD,
                      new String[] {
                        ScreenHourlyLogProvider.COLUMN_NAME.YYYYMMDD
                      },
                      null,
                      null,
                      ScreenHourlyLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
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
    Log.d(TAG, "pageCount:[" + pageCount + "]");

    gestureDetector = new GestureDetector(this, gestureListener);
    animationInFromLeft  = AnimationUtils.loadAnimation(this, R.anim.in_from_left);
    animationOutToRight  = AnimationUtils.loadAnimation(this, R.anim.out_to_right);
    animationInFromRight = AnimationUtils.loadAnimation(this, R.anim.in_from_right);
    animationOutToLeft   = AnimationUtils.loadAnimation(this, R.anim.out_to_left);
  }

  @Override
  public void onResume() {
    if (Constant.DEBUG) Log.v(TAG, ">>> onResume");
    super.onResume();
    switchViewToWait();
    if (Constant.DEBUG) Log.d(TAG, "startPos:[" + getStartPos() + "]");
    onResumeBottomHalf();
    if (Constant.DEBUG) Log.v(TAG, "<<< onResume");
  }

  public void gotoPrev(View v) {
    prevPage();
  }
  public void gotoNext(View v) {
    nextPage();
  }
  public void gotoClear(View v) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityScreenHourlyLogGraph.this);
    alertDialogBuilder.setTitle(getResources().getString(R.string.screenhourlylog__title_of_delete_dialog));
    alertDialogBuilder.setMessage(getResources().getString(R.string.screenhourlylog__message_of_delete_dialog));
    alertDialogBuilder.setPositiveButton(R.string.screenhourlylog__button_of_delete_dialog_positive, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        int result = getContentResolver().delete(ScreenHourlyLogProvider.CONTENT_URI, null, null);
        Toast.makeText(ActivityScreenHourlyLogGraph.this, 
                       String.format(getResources().getString(R.string.screenhourlylog__message_of_delete_completion), result), 
                       Toast.LENGTH_LONG).show();
        pagerFlipper.removeAllViews();
        onCreateBottomHalf();
      }
    });
    alertDialogBuilder.setNegativeButton(R.string.screenhourlylog__button_of_delete_dialog_negative, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    alertDialogBuilder.create().show();
  }
  public void gotoSetting(View v) {
    Intent intent = new Intent(this, ActivityScreenLogSetting.class);
    startActivity(intent);
  }

  protected final static String BATTERYLOGS_PNG = "screenhourlylogs.png";
  public void gotoSend(final View v) {
    if (currentGraphBitmap == null) {
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityScreenHourlyLogGraph.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.screenhourlylog_graph__title_of_be_not_drawn_graph_yet));
      alertDialogBuilder.setMessage(getResources().getString(R.string.screenhourlylog_graph__message_of_be_not_drawn_graph_yet));
      alertDialogBuilder.setPositiveButton(R.string.screenhourlylog_graph__button_of_be_not_drawn_graph_yet, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
      alertDialogBuilder.create().show();
      return;
    }
    if (! getExternalStorageAvailable() || ! getExternalStorageWriteable()) {
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityScreenHourlyLogGraph.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.screenhourlylog__title_of_sdcard_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.screenhourlylog__message_of_sdcard_error));
      alertDialogBuilder.setPositiveButton(R.string.screenhourlylog__button_of_sdcard_error_ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          gotoSend(v);
          dialog.dismiss();
        }
      });
      alertDialogBuilder.setNegativeButton(R.string.screenhourlylog__button_of_sdcard_error_negative, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
      alertDialogBuilder.create().show();
      return;
    }
    String filePath = Environment.getExternalStorageDirectory() + File.separator + BATTERYLOGS_PNG;
    String zipFilePath = Environment.getExternalStorageDirectory() + File.separator + BATTERYLOGS_PNG + ".zip";
    File file = new File(filePath);
    file.getParentFile().mkdir();
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file,false);
      currentGraphBitmap.compress(Bitmap.CompressFormat.PNG,100,out); 
    }
    catch (IOException ex) {
      Log.e(TAG, "fileOutput failure", ex);
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityScreenHourlyLogGraph.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.screenhourlylog__title_of_file_output_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.screenhourlylog__message_of_file_output_error));
      alertDialogBuilder.setPositiveButton(R.string.screenhourlylog__button_of_file_output_error, new DialogInterface.OnClickListener() {
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
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityScreenHourlyLogGraph.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.screenhourlylog__title_of_zip_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.screenhourlylog__message_of_zip_error));
      alertDialogBuilder.setPositiveButton(R.string.screenhourlylog__button_of_zip_error, new DialogInterface.OnClickListener() {
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
    String date = (String) dateList.get(getStartPos());
    intent.putExtra(Intent.EXTRA_SUBJECT, String.format(getResources().getString(R.string.screenhourlylog__title_of_mail), date));
    intent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.screenhourlylog__text_of_mail), date));
    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + zipFilePath));
    intent.setType("application/zip");
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(Intent.createChooser(intent, getResources().getString(R.string.screenhourlylog__message_of_client_chooser)));
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
  public void changePage(final int newStartPos) {
    if (Constant.DEBUG)Log.v(TAG, ">>> changePage:(" + newStartPos + ")");
    if (Constant.DEBUG)Log.d(TAG, "now startPos:[" + getStartPos() + "]");
    if (newStartPos < pageCount && newStartPos >= 0) {
      pagerFlipper.setDisplayedChild(newStartPos);
      setStartPos(newStartPos);
      View view = pagerFlipper.getCurrentView();
      setupGraphView(view, getStartPos());
      setupPagerAndClearButton();
    }
    if (Constant.DEBUG)Log.v(TAG, "<<< changePage:(" + newStartPos + ")");
  }
}
