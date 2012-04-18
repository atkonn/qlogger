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
import jp.co.qsdn.android.qlogger.core.CpuUtilizationLog;
import jp.co.qsdn.android.qlogger.provider.CpuUtilizationLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.adapter.CpuUtilizationLogListAdapter;
import jp.co.qsdn.android.qlogger.view.chart.GraphicalView;
import jp.co.qsdn.android.qlogger.view.flipper.ViewFlipper;
import jp.co.qsdn.android.qlogger.cache.ImageCache;

import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class ActivityCpuUtilizationLogGraph
  extends AbstractActivity
{
  final String TAG = getClass().getName();
  private int limitCount = 144;
  private int listCount = 0;
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
        if (getStartPos() < getPageCount() - 1) {
          pagerFlipper.setInAnimation(animationInFromRight);
          pagerFlipper.setOutAnimation(animationOutToLeft);
          pagerFlipper.showNext();
          setStartPos(getStartPos() + 1);
          View view = pagerFlipper.getCurrentView();
          ListView listView = (ListView)view.findViewById(R.id.list);
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
          setStartPos(getStartPos() - 1);
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
    return getResources().getString(R.string.cpuutilizationlog__text_of_title);
  }

  protected XYMultipleSeriesDataset getCurrentData(int startpos) {
    String yyyymmdd = (String)dateList.get(startpos);
    if (Constant.DEBUG)Log.d(TAG, "yyyymmdd:[" + yyyymmdd + "]");

    Cursor cur = null;
    List<CpuUtilizationLog> list = new ArrayList<CpuUtilizationLog>();
    try {
      cur = getContentResolver()
                     .query(CpuUtilizationLogProvider.CONTENT_URI, 
                       CpuUtilizationLogProvider.PROJECTION,
                       CpuUtilizationLogProvider.COLUMN_NAME.YYYYMMDD + " = ? ",
                       new String[] {
                         yyyymmdd
                       },
                       CpuUtilizationLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
      
      while (cur.moveToNext()) {
        CpuUtilizationLog cpuUtilizationLog = new CpuUtilizationLog(); 
        cpuUtilizationLog.setId(cur.getLong(CpuUtilizationLogProvider.COLUMN_INDEX._ID));
        cpuUtilizationLog.setYyyymmdd(cur.getString(CpuUtilizationLogProvider.COLUMN_INDEX.YYYYMMDD));
        cpuUtilizationLog.setUser(cur.getFloat(CpuUtilizationLogProvider.COLUMN_INDEX.USER));
        cpuUtilizationLog.setNice(cur.getFloat(CpuUtilizationLogProvider.COLUMN_INDEX.NICE));
        cpuUtilizationLog.setSystem(cur.getFloat(CpuUtilizationLogProvider.COLUMN_INDEX.SYSTEM));
        cpuUtilizationLog.setIdle(cur.getFloat(CpuUtilizationLogProvider.COLUMN_INDEX.IDLE));
        cpuUtilizationLog.setCreatedOn(Timestamp.valueOf(cur.getString(CpuUtilizationLogProvider.COLUMN_INDEX.CREATED_ON)));
        cpuUtilizationLog.setCreatedOnLong(cur.getLong(CpuUtilizationLogProvider.COLUMN_INDEX.CREATED_ON_LONG));
        list.add(cpuUtilizationLog);
      }
    }
    finally {
      if (cur != null) {
        try { cur.close(); } catch (Exception ex) {}
        cur = null;
      }
    }
    XYMultipleSeriesDataset data = new XYMultipleSeriesDataset();
    TimeSeries userSeries = new TimeSeries("user");
    TimeSeries niceSeries = new TimeSeries("nice");
    TimeSeries systemSeries = new TimeSeries("system");
    TimeSeries idleSeries = new TimeSeries("idle");

    CpuUtilizationLog[] cpuUtilizationLogs = (CpuUtilizationLog[])list.toArray(new CpuUtilizationLog[list.size()]);
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTimeInMillis(cpuUtilizationLogs[0].getCreatedOn().getTime());
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    cal.set(java.util.Calendar.MINUTE, 0);
    cal.set(java.util.Calendar.SECOND, 0);
    cal.set(java.util.Calendar.MILLISECOND, 0);
    xAxisMin = cal.getTimeInMillis();
    cal.add(java.util.Calendar.DATE, 1);
    xAxisMax = cal.getTimeInMillis();
    for (int ii=0; ii<cpuUtilizationLogs.length; ii++) {
      float val = cpuUtilizationLogs[ii].getIdle();
      idleSeries.add(new java.util.Date(cpuUtilizationLogs[ii].getCreatedOn().getTime()), val);
      val += cpuUtilizationLogs[ii].getSystem();
      systemSeries.add(new java.util.Date(cpuUtilizationLogs[ii].getCreatedOn().getTime()), val);
      val += cpuUtilizationLogs[ii].getNice();
      niceSeries.add(new java.util.Date(cpuUtilizationLogs[ii].getCreatedOn().getTime()), val);
      val += cpuUtilizationLogs[ii].getUser();
      userSeries.add(new java.util.Date(cpuUtilizationLogs[ii].getCreatedOn().getTime()), val);
    }
    data.addSeries(userSeries);
    data.addSeries(niceSeries);
    data.addSeries(systemSeries);
    data.addSeries(idleSeries);
    return data;
  }
  private long xAxisMin;
  private long xAxisMax;
  protected XYMultipleSeriesRenderer getRenderer() {

    /*
     * "#158aea" come from http://d.hatena.ne.jp/good-speed/20110806/1312638320
     */
    XYSeriesRenderer rendererUser = new XYSeriesRenderer();
    rendererUser.setColor(Color.parseColor("#158aea"));
    rendererUser.setDisplayChartValues(false);
    rendererUser.setChartValuesTextSize(18);
    rendererUser.setFillBelowLine(true);
    rendererUser.setFillBelowLineColor(Color.parseColor("#158aea"));
    rendererUser.setFillPoints(true);

    XYMultipleSeriesRenderer baseRenderer = new XYMultipleSeriesRenderer(); 
    baseRenderer.addSeriesRenderer(rendererUser);

    XYSeriesRenderer rendererNice = new XYSeriesRenderer();
    rendererNice.setColor(Color.parseColor("#150000"));
    rendererNice.setDisplayChartValues(false);
    rendererNice.setChartValuesTextSize(18);
    rendererNice.setFillBelowLine(true);
    rendererNice.setFillBelowLineColor(Color.parseColor("#150000"));
    rendererNice.setFillPoints(true);
    baseRenderer.addSeriesRenderer(rendererNice);

    XYSeriesRenderer rendererSystem = new XYSeriesRenderer();
    rendererSystem.setColor(Color.parseColor("#00ae00"));
    rendererSystem.setDisplayChartValues(false);
    rendererSystem.setChartValuesTextSize(18);
    rendererSystem.setFillBelowLine(true);
    rendererSystem.setFillBelowLineColor(Color.parseColor("#00ae00"));
    rendererSystem.setFillPoints(true);
    baseRenderer.addSeriesRenderer(rendererSystem);

    XYSeriesRenderer rendererIdle = new XYSeriesRenderer();
    rendererIdle.setColor(Color.parseColor("#0000ea"));
    rendererIdle.setDisplayChartValues(false);
    rendererIdle.setChartValuesTextSize(18);
    rendererIdle.setFillBelowLine(true);
    rendererIdle.setFillBelowLineColor(Color.parseColor("#0000ea"));
    rendererIdle.setFillPoints(true);
    baseRenderer.addSeriesRenderer(rendererIdle);
        
    baseRenderer.setXAxisMin(xAxisMax);
    baseRenderer.setXAxisMax(xAxisMin);
    baseRenderer.setYAxisMin(0);
    baseRenderer.setYAxisMax(100);
    baseRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.VERTICAL);
        
    baseRenderer.setShowGrid(true);

    /*
     * "#c9c9c9" come from http://d.hatena.ne.jp/good-speed/20110806/1312638320
     */
    baseRenderer.setGridColor(Color.parseColor("#c9c9c9"));
    baseRenderer.setPanEnabled(false, false);
    baseRenderer.setPanLimits(new double[]{0, 0, 0, 0});

    baseRenderer.setShowLegend(true);

    baseRenderer.setLabelsTextSize(20);
    baseRenderer.setYLabelsAlign(Align.RIGHT);
        
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
    LinearLayout graphView = (LinearLayout)view.findViewById(R.id.graph);
    graphView.removeAllViews();

    TextView textView = (TextView)view.findViewById(R.id.date);
    String date = (String)dateList.get(index);
    textView.setText(String.format(getResources().getString(R.string.cpuutilizationlog__column_title), date.substring(0,4), date.substring(4,6), date.substring(6,8)));

    TimeChart chart = new TimeChart(getCurrentData(index), getRenderer());
    chart.setDateFormat("HH:mm");

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


    return chartView;
  }

  
  @Override
  protected void onCreateBottomHalf() {
    super.onCreateBottomHalf();
    setupInitData();
    startWatchingExternalStorage();
    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  protected void onResumeBottomHalf() {
    pagerFlipper = (ViewFlipper)findViewById(R.id.pagerFlipper);
    pagerFlipper.removeAllViews();
    if (Constant.DEBUG)Log.d(TAG, "startPos:[" + getStartPos() + "]");
    for (int ii=0; ii<getPageCount(); ii++) {
      View view = inflater.inflate(R.layout.cpuutilizationlog_graph__main, null);
      pagerFlipper.addView(view);
      if (ii == getStartPos()) {
        setupGraphView(view, ii);
      }
    }
    pagerFlipper.setDisplayedChild(getStartPos());

    TextView textView = (TextView)findViewById(R.id.title_text);
    textView.setText(getResources().getString(R.string.dashboard_item_cpuutilization_title));

    textView = (TextView)findViewById(R.id.page_text);
    if (getPageCount() > 0) {
      textView.setText("1/" + getPageCount());
    }
    else {
      textView.setText("0/0");
    }

    ImageView imageView = (ImageView)findViewById(R.id.title_icon);
    imageView.setImageResource(R.drawable.icon);

    setupPagerAndClearButton();
    if (getPageCount() > 0) {
      switchViewToMain();
    }
    else {
      switchViewToNotFound();
    }
  }
  protected void setupPagerAndClearButton() {
    super.setupPagerAndClearButton();

    TextView textView = (TextView)findViewById(R.id.page_text);
    if (getPageCount() == 0) {
      textView.setText("0/0");
    }
    else {
      textView.setText((getStartPos() + 1) + "/" + getPageCount());
    }
  }

  @Override
  protected int getContentViewResourceId() {
    return R.layout.cpuutilizationlog_graph;
  }

  @Override
  protected int getTitleViewResourceId() {
    return R.layout.cpuutilizationlog__action_bar2;
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
                    .query(CpuUtilizationLogProvider.CONTENT_URI_WITH_GROUPBY_YYYYMMDD,
                      new String[] {
                        CpuUtilizationLogProvider.COLUMN_NAME.YYYYMMDD
                      },
                      null,
                      null,
                      CpuUtilizationLogProvider.COLUMN_NAME.CREATED_ON_LONG + " desc");
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
    setPageCount(dateList.size());
    if (Constant.DEBUG)Log.d(TAG, "listCount:[" + listCount + "]");

    gestureDetector = new GestureDetector(this, gestureListener);
    animationInFromLeft  = AnimationUtils.loadAnimation(this, R.anim.in_from_left);
    animationOutToRight  = AnimationUtils.loadAnimation(this, R.anim.out_to_right);
    animationInFromRight = AnimationUtils.loadAnimation(this, R.anim.in_from_right);
    animationOutToLeft   = AnimationUtils.loadAnimation(this, R.anim.out_to_left);
  }

  @Override
  public void onResume() {
    super.onResume();
    switchViewToWait();
    setStartPos(getIntent().getIntExtra("startPos", 0));
    onResumeBottomHalf();
  }

  public void gotoPrev(View v) {
    prevPage();
  }
  public void gotoNext(View v) {
    nextPage();
  }
  public void gotoClear(View v) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCpuUtilizationLogGraph.this);
    alertDialogBuilder.setTitle(getResources().getString(R.string.cpuutilizationlog__title_of_delete_dialog));
    alertDialogBuilder.setMessage(getResources().getString(R.string.cpuutilizationlog__message_of_delete_dialog));
    alertDialogBuilder.setPositiveButton(R.string.cpuutilizationlog__button_of_delete_dialog_positive, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        int result = getContentResolver().delete(CpuUtilizationLogProvider.CONTENT_URI, null, null);
        Toast.makeText(ActivityCpuUtilizationLogGraph.this, 
                       String.format(getResources().getString(R.string.cpuutilizationlog__message_of_delete_completion), result), 
                       Toast.LENGTH_LONG).show();
        pagerFlipper.removeAllViews();
        onCreateBottomHalf();
      }
    });
    alertDialogBuilder.setNegativeButton(R.string.cpuutilizationlog__button_of_delete_dialog_negative, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    alertDialogBuilder.create().show();
  }
  public void gotoSetting(View v) {
    Intent intent = new Intent(this, ActivityCpuUtilizationLogSetting.class);
    startActivity(intent);
  }
  public void gotoList(View v) {
    finish();
  }

  protected final static String BATTERYLOGS_PNG = "cpuutilizationlogs.png";
  public void gotoSend(final View v) {
    if (currentGraphBitmap == null) {
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCpuUtilizationLogGraph.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.cpuutilizationlog_graph__title_of_be_not_drawn_graph_yet));
      alertDialogBuilder.setMessage(getResources().getString(R.string.cpuutilizationlog_graph__message_of_be_not_drawn_graph_yet));
      alertDialogBuilder.setPositiveButton(R.string.cpuutilizationlog_graph__button_of_be_not_drawn_graph_yet, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
      alertDialogBuilder.create().show();
      return;
    }
    if (! getExternalStorageAvailable() || ! getExternalStorageWriteable()) {
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCpuUtilizationLogGraph.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.cpuutilizationlog__title_of_sdcard_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.cpuutilizationlog__message_of_sdcard_error));
      alertDialogBuilder.setPositiveButton(R.string.cpuutilizationlog__button_of_sdcard_error_ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          gotoSend(v);
          dialog.dismiss();
        }
      });
      alertDialogBuilder.setNegativeButton(R.string.cpuutilizationlog__button_of_sdcard_error_negative, new DialogInterface.OnClickListener() {
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
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCpuUtilizationLogGraph.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.cpuutilizationlog__title_of_file_output_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.cpuutilizationlog__message_of_file_output_error));
      alertDialogBuilder.setPositiveButton(R.string.cpuutilizationlog__button_of_file_output_error, new DialogInterface.OnClickListener() {
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
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCpuUtilizationLogGraph.this);
      alertDialogBuilder.setTitle(getResources().getString(R.string.cpuutilizationlog__title_of_zip_error));
      alertDialogBuilder.setMessage(getResources().getString(R.string.cpuutilizationlog__message_of_zip_error));
      alertDialogBuilder.setPositiveButton(R.string.cpuutilizationlog__button_of_zip_error, new DialogInterface.OnClickListener() {
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
    intent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.cpuutilizationlog__title_of_mail));
    intent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.cpuutilizationlog__text_of_mail), dateList.get(getStartPos())));
    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + zipFilePath));
    intent.setType("application/zip");
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(Intent.createChooser(intent, getResources().getString(R.string.cpuutilizationlog__message_of_client_chooser)));
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
