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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.qsdn.android.qlogger.Constant;
import jp.co.qsdn.android.qlogger.LogcatService;
import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.core.ErrorLog;
import jp.co.qsdn.android.qlogger.provider.ErrorLogProvider;
import jp.co.qsdn.android.qlogger.util.Util;
import jp.co.qsdn.android.qlogger.view.adapter.ErrorLogListAdapter;
import jp.co.qsdn.android.qlogger.view.flipper.ViewFlipper;

public class ActivityErrorLogDetail
  extends AbstractActivity
{
  final String TAG = getClass().getName();


  @Override
  protected String getTitleText() {
    return getResources().getString(R.string.errorlog_detail__text_of_title);
  }

  @Override
  protected void onCreateBottomHalf() {
    ImageView imageView;
    imageView = (ImageView)findViewById(R.id.action_bar_send);
    if (imageView  != null) {
      imageView.setImageResource(R.drawable.action_bar_send);
    }
    imageView = (ImageView)findViewById(R.id.action_bar_clear);
    if (imageView != null) {
      imageView.setImageResource(R.drawable.action_bar_clear);
    }
  }

  @Override
  protected int getContentViewResourceId() {
    return R.layout.errorlog_detail;
  }

  @Override
  protected int getTitleViewResourceId() {
    return R.layout.errorlog_detail__action_bar;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void setupActionBarBottomHalf() {
    ImageView imageView = (ImageView)findViewById(R.id.action_bar_back);
    if (imageView != null) {
      imageView.setImageResource(R.drawable.action_bar_back);
    }
  }
  @Override
  public void onResume() {
    super.onResume();


    TextView timeText = (TextView)findViewById(R.id.timestamp);
    timeText.setText(getIntent().getStringExtra(ErrorLogProvider.COLUMN_NAME.TIMESTAMP));

    TextView tagText = (TextView)findViewById(R.id.tag);
    tagText.setText(getIntent().getStringExtra(ErrorLogProvider.COLUMN_NAME.TAG));

    TextView messageText = (TextView)findViewById(R.id.message);
    messageText.setText(getIntent().getStringExtra(ErrorLogProvider.COLUMN_NAME.MESSAGE));

    TextView stacktraceText = (TextView)findViewById(R.id.stacktrace);
    stacktraceText.setText(getIntent().getStringExtra(ErrorLogProvider.COLUMN_NAME.STACKTRACE));

    long id = getIntent().getLongExtra(ErrorLogProvider.COLUMN_NAME._ID, -1);
    long unread = getIntent().getLongExtra(ErrorLogProvider.COLUMN_NAME.UNREAD, 0);
    if (id != -1 && unread == 1) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(ErrorLogProvider.COLUMN_NAME.UNREAD,     0);
      Uri uri = ContentUris.withAppendedId(ErrorLogProvider.CONTENT_URI, id);
      getApplicationContext().getContentResolver().update(uri, contentValues, null, null);
    }
  }

  public void gotoClear(View v) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityErrorLogDetail.this);
    alertDialogBuilder.setTitle(getResources().getString(R.string.errorlog_detail__title_of_delete_dialog));
    alertDialogBuilder.setMessage(getResources().getString(R.string.errorlog_detail__message_of_delete_dialog));
    alertDialogBuilder.setPositiveButton(R.string.errorlog_detail__button_of_delete_dialog_positive, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        long id = getIntent().getLongExtra(ErrorLogProvider.COLUMN_NAME._ID, -1);
        if (id != -1) {
          int result = getContentResolver().delete(ContentUris.withAppendedId(ErrorLogProvider.CONTENT_URI, id), null, null);
          Toast.makeText(ActivityErrorLogDetail.this, 
                         String.format(getResources().getString(R.string.errorlog_detail__message_of_delete_completion), result), 
                         Toast.LENGTH_LONG).show();
        }
        setResultAndFinish();
      }
    });
    alertDialogBuilder.setNegativeButton(R.string.errorlog_detail__button_of_delete_dialog_negative, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    alertDialogBuilder.create().show();
  }
  public void gotoSend(View v) {
    getHandler().post(new Runnable() {
      public void run() {
        Uri uri=Uri.parse("mailto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.errorlog_detail__title_of_mail));
        intent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.errorlog_detail__text_of_mail),
          getIntent().getStringExtra(ErrorLogProvider.COLUMN_NAME.TIMESTAMP),
          getIntent().getStringExtra(ErrorLogProvider.COLUMN_NAME.TAG),
          getIntent().getStringExtra(ErrorLogProvider.COLUMN_NAME.MESSAGE),
          getIntent().getStringExtra(ErrorLogProvider.COLUMN_NAME.STACKTRACE)
        ));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.errorlog_detail__message_of_mail_client_chooser)));
      }
    });
  }

  protected void setResultAndFinish() {
    getHandler().post(new Runnable() {
      public void run() {
        Intent intent = getIntent();
        intent.putExtra("RELOAD", "RELOAD");
        setResult(RESULT_OK, intent);
        finish();
      }
    });
  }

  public void gotoBack(View v) {
    finish();
  }

  public void changePage(int newStartPos) {
    throw new UnsupportedOperationException();
  }
}
