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

import android.os.Bundle;

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

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.qsdn.android.qlogger.LogcatService;
import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.Constant;
import jp.co.qsdn.android.qlogger.core.ErrorLog;
import jp.co.qsdn.android.qlogger.provider.ErrorLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.adapter.ErrorLogListAdapter;
import jp.co.qsdn.android.qlogger.view.flipper.ViewFlipper;

public class ActivityErrorLog
  extends AbstractActivity
{
  final String TAG = getClass().getName();
  private int limitCount = 10;
  private int listCount = 0;

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
          setStartPos(getStartPos()-1);
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
    return getResources().getString(R.string.errorlog__text_of_title);
  }
  protected ListView setupListView(View view, int index) {
    ListView listView = (ListView)view.findViewById(R.id.list);
    final ArrayAdapter adapter = new ErrorLogListAdapter(this, 0, getCurrentList(index));
    listView.setAdapter(adapter);
    listView.setOnTouchListener(touchListener);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = (ListView) parent;
        ErrorLog item = (ErrorLog) listView.getItemAtPosition(position);
        Intent intent = new Intent(getApplicationContext(), ActivityErrorLogDetail.class);
        intent.putExtra(ErrorLogProvider.COLUMN_NAME._ID, item.getId());
        intent.putExtra(ErrorLogProvider.COLUMN_NAME.TIMESTAMP,item.getTimestamp().toString());
        intent.putExtra(ErrorLogProvider.COLUMN_NAME.TIMESTAMP_LONG, item.getTimestampLong());
        intent.putExtra(ErrorLogProvider.COLUMN_NAME.TAG,item.getTag());
        intent.putExtra(ErrorLogProvider.COLUMN_NAME.MESSAGE,item.getMessage());
        intent.putExtra(ErrorLogProvider.COLUMN_NAME.STACKTRACE,item.getStacktrace());
        intent.putExtra(ErrorLogProvider.COLUMN_NAME.UNREAD,    item.getUnread());
        intent.putExtra(ErrorLogProvider.COLUMN_NAME.HOLD,      item.getHold());
        intent.putExtra(ErrorLogProvider.COLUMN_NAME.CREATED_ON,item.getCreatedOn());
        startActivityForResult(intent, Constant.REQUEST.ERRORLOG_DETAIL);

        item.setUnread(0);

        adapter.notifyDataSetChanged();
      }
    });
    return listView;
  }

  @Override
  protected void onCreateBottomHalf() {
    setupInitData();

    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    pagerFlipper = (ViewFlipper)findViewById(R.id.pagerFlipper);
    for (int ii=0; ii<getPageCount(); ii++) {
      View view = inflater.inflate(R.layout.errorlog_list, null);
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
    textView.setText(getResources().getString(R.string.dashboard_item_errorlog_title));

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
    return R.layout.errorlog;
  }

  @Override
  protected int getTitleViewResourceId() {
    return R.layout.errorlog__action_bar;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void setupActionBarBottomHalf() {
  }

  protected List<ErrorLog> getCurrentList(int startpos) {
    Cursor cur = null;
    List<ErrorLog> list = new ArrayList<ErrorLog>();
    try {
      cur = getContentResolver()
                     .query(ErrorLogProvider.CONTENT_URI, 
                       new String[] {
                         ErrorLogProvider.COLUMN_NAME._ID,
                         ErrorLogProvider.COLUMN_NAME.TIMESTAMP,
                         ErrorLogProvider.COLUMN_NAME.TIMESTAMP_LONG,
                         ErrorLogProvider.COLUMN_NAME.TAG,
                         ErrorLogProvider.COLUMN_NAME.MESSAGE,
                         ErrorLogProvider.COLUMN_NAME.STACKTRACE,
                         ErrorLogProvider.COLUMN_NAME.UNREAD,
                         ErrorLogProvider.COLUMN_NAME.HOLD,
                         ErrorLogProvider.COLUMN_NAME.CREATED_ON
                       }, 
                       null,
                       null,
                       ErrorLogProvider.COLUMN_NAME.TIMESTAMP_LONG + " desc LIMIT " + (startpos * limitCount) + "," + limitCount);
      
      while (cur.moveToNext()) {
        ErrorLog errorLog = new ErrorLog(); 
        errorLog.setId(cur.getLong(ErrorLogProvider.COLUMN_INDEX._ID));
        errorLog.setTimestamp(Timestamp.valueOf(cur.getString(ErrorLogProvider.COLUMN_INDEX.TIMESTAMP)));
        errorLog.setTimestampLong(cur.getLong(ErrorLogProvider.COLUMN_INDEX.TIMESTAMP_LONG));
        errorLog.setTag(cur.getString(ErrorLogProvider.COLUMN_INDEX.TAG));
        errorLog.setMessage(cur.getString(ErrorLogProvider.COLUMN_INDEX.MESSAGE));
        errorLog.setStacktrace(cur.getString(ErrorLogProvider.COLUMN_INDEX.STACKTRACE));
        errorLog.setUnread(cur.getLong(ErrorLogProvider.COLUMN_INDEX.UNREAD));
        errorLog.setHold(cur.getLong(ErrorLogProvider.COLUMN_INDEX.HOLD));
        errorLog.setCreatedOn(Timestamp.valueOf(cur.getString(ErrorLogProvider.COLUMN_INDEX.CREATED_ON)));
        list.add(errorLog);
      }
    }
    finally {
      if (cur != null) {
        try { cur.close(); } catch (Exception ex) {}
        cur = null;
      }
    } 
    for (int ii=list.size(); ii<limitCount; ii++) {
      ErrorLog errorLog = new ErrorLog(); 
      errorLog.setId(-1);
      list.add(errorLog);
    }
    return list;
  }
  protected void setupInitData() {
    setStartPos(0);
    Cursor cur = null;
    try {
      cur = getContentResolver()
                    .query(ErrorLogProvider.CONTENT_URI,
                      new String[] {
                        "COUNT(*)"
                      },
                      null,
                      null,
                      null);
      while (cur.moveToNext()) {
        listCount = (int)cur.getLong(0);
      }
    }
    finally {
      if (cur != null) {
        try { cur.close(); } catch (Exception ex) {}
        cur = null;
      }
    } 
    setPageCount(listCount / limitCount);
    if (listCount % limitCount > 0) {
      setPageCount(getPageCount() + 1);
    }
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
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityErrorLog.this);
    alertDialogBuilder.setTitle(getResources().getString(R.string.errorlog__title_of_delete_dialog));
    alertDialogBuilder.setMessage(getResources().getString(R.string.errorlog__message_of_delete_dialog));
    alertDialogBuilder.setPositiveButton(R.string.errorlog__button_of_delete_dialog_positive, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        int result = getContentResolver().delete(ErrorLogProvider.CONTENT_URI, null, null);
        Toast.makeText(ActivityErrorLog.this, 
                       String.format(getResources().getString(R.string.errorlog__message_of_delete_completion), result), 
                       Toast.LENGTH_LONG).show();
        pagerFlipper.removeAllViews();
        onCreateBottomHalf();
      }
    });
    alertDialogBuilder.setNegativeButton(R.string.errorlog__button_of_delete_dialog_negative, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    alertDialogBuilder.create().show();
  }
  public void gotoSetting(View v) {
    Intent intent = new Intent(this, ActivityErrorLogSetting.class);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,Intent data){
    super.onActivityResult(requestCode, resultCode, data);
    Log.d(TAG, "start onActivityResult(" + requestCode + "," + resultCode + ")");
    Log.d(TAG, "RESULT_OK:[" + RESULT_OK + "]");
    Log.d(TAG, "Constant.REQUEST.ERRORLOG_DETAIL:[" + Constant.REQUEST.ERRORLOG_DETAIL + "]");
    if (requestCode == Constant.REQUEST.ERRORLOG_DETAIL) {
      if (resultCode == RESULT_OK){
        switchViewToWait();
        pagerFlipper.removeAllViews();
        onCreateBottomHalf();
      }
    }
  }

  @Override
  public void changePage(int newStartPos) {
    throw new UnsupportedOperationException();
  }
}
