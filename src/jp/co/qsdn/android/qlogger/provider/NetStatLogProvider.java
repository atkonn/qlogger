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
package jp.co.qsdn.android.qlogger.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import android.net.Uri;

import android.text.TextUtils;

import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import jp.co.qsdn.android.qlogger.Constant;



public class NetStatLogProvider
  extends ContentProvider {

  private static class DBHelper extends SQLiteOpenHelper {
    private static DBHelper _dbHelper = null;
    public static DBHelper getInstance(Context context) {
      if (_dbHelper == null) {
        _dbHelper = new DBHelper(context);
      }
      return _dbHelper;
    }
    private DBHelper(Context context) {
      super(context, TABLE_NAME+".db", null, Constant.DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(
        "CREATE TABLE "+TABLE_NAME+" ("+
        "  _id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  yyyymmdd TEXT not null," +
        "  recv_byte TEXT not null," +
        "  recv_packet TEXT not null," +
        "  recv_err TEXT not null," +
        "  send_byte TEXT not null," +
        "  send_packet TEXT not null," +
        "  send_err TEXT not null," +
        "  created_on_long INTEGER not null, "+
        "  created_on TEXT not null "+
        ");"
      );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
  }
  public static final String AUTHORITY = "jp.co.qsdn.android.qlogger.provider.NetStatLog";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/logs");
  public static final Uri CONTENT_URI_WITH_GROUPBY_YYYYMMDD = Uri.parse("content://" + AUTHORITY + "/logs" + GROUP_BY.YYYYMMDD);
  public static class GROUP_BY {
    public static String YYYYMMDD = "/groupByYYYYMMDD";
  }
  public static class COLUMN_NAME {
    public static final String _ID             = "_id";
    public static final String YYYYMMDD        = "yyyymmdd";
    public static final String RECV_BYTE       = "recv_byte";
    public static final String RECV_PACKET     = "recv_packet";
    public static final String RECV_ERR        = "recv_err";
    public static final String SEND_BYTE       = "send_byte";
    public static final String SEND_PACKET     = "send_packet";
    public static final String SEND_ERR        = "send_err";
    public static final String CREATED_ON_LONG = "created_on_long";
    public static final String CREATED_ON      = "created_on";
  }
  public static String[] PROJECTION = {
    COLUMN_NAME._ID,
    COLUMN_NAME.YYYYMMDD,
    COLUMN_NAME.RECV_BYTE,
    COLUMN_NAME.RECV_PACKET,
    COLUMN_NAME.RECV_ERR,
    COLUMN_NAME.SEND_BYTE,
    COLUMN_NAME.SEND_PACKET,
    COLUMN_NAME.SEND_ERR,
    COLUMN_NAME.CREATED_ON_LONG,
    COLUMN_NAME.CREATED_ON
  };
  public static class COLUMN_INDEX {
    public static final int _ID                = 0;
    public static final int YYYYMMDD           = 1;
    public static final int RECV_BYTE          = 2;
    public static final int RECV_PACKET        = 3;
    public static final int RECV_ERR           = 4;
    public static final int SEND_BYTE          = 5;
    public static final int SEND_PACKET        = 6;
    public static final int SEND_ERR           = 7;
    public static final int CREATED_ON_LONG    = 8;
    public static final int CREATED_ON         = 9;
  }

  public static final int NET_STAT_LOGS                   = 0;
  public static final int NET_STAT_LOG_ID                 = 1;
  public static final int NET_STAT_LOGS_GROUP_BY_YYYYMMDD = 2;
  public static final int NET_STAT_LOGS_BY_YYYYMMDD       = 3;

  protected static final String TABLE_NAME = "net_stat_log";

  private SimpleDateFormat dateFormat     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  private SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyyMMdd");
  private SimpleDateFormat hh24Format     = new SimpleDateFormat("HH");

  protected DBHelper dbHelper;

  private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    uriMatcher.addURI(AUTHORITY, "logs", NET_STAT_LOGS);
    uriMatcher.addURI(AUTHORITY, "logs/#", NET_STAT_LOG_ID);
    uriMatcher.addURI(AUTHORITY, "logs" + GROUP_BY.YYYYMMDD, NET_STAT_LOGS_GROUP_BY_YYYYMMDD);
    uriMatcher.addURI(AUTHORITY, "logs/yyyymmdd", NET_STAT_LOGS);
    uriMatcher.addURI(AUTHORITY, "logs/yyyymmdd/#", NET_STAT_LOGS_BY_YYYYMMDD);
  };

  public NetStatLogProvider() {
    dbHelper = null;
  }

  @Override
  public boolean onCreate() {
    dbHelper = DBHelper.getInstance(getContext());
    return false;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int ret = 0;
    switch(uriMatcher.match(uri)) {
    case NET_STAT_LOGS:
    case NET_STAT_LOGS_GROUP_BY_YYYYMMDD:
      ret = dbHelper.getWritableDatabase().delete(TABLE_NAME, selection, selectionArgs);
      break;

    case NET_STAT_LOG_ID:
      {
        long parseId = ContentUris.parseId(uri);
        String _selection = COLUMN_NAME._ID + "=" + parseId;
        if (!TextUtils.isEmpty(selection)) {
          _selection += " AND (" + selection + ")";
        }
        ret = dbHelper.getWritableDatabase().delete(TABLE_NAME, _selection, selectionArgs);
      }
      break;

    case NET_STAT_LOGS_BY_YYYYMMDD:
      {
        long yyyymmdd = ContentUris.parseId(uri);
        String _selection = COLUMN_NAME.YYYYMMDD + "=" + yyyymmdd;
        if (!TextUtils.isEmpty(selection)) {
          _selection += " AND (" + selection + ")";
        }
        ret = dbHelper.getWritableDatabase().delete(TABLE_NAME, _selection, selectionArgs);
      }
      break;

    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return ret;    
  }

  @Override
  public String getType(Uri uri) {
    switch(uriMatcher.match(uri)) {
    case NET_STAT_LOGS:
    case NET_STAT_LOGS_GROUP_BY_YYYYMMDD:
    case NET_STAT_LOGS_BY_YYYYMMDD:
      return "vnd.android.cursor.dir/vnd.qsdn.netstatlog";

    case NET_STAT_LOG_ID:
      return "vnd.android.cursor.item/vnd.qsdn.netstatlog";

    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    if (contentValues == null) {
      contentValues = new ContentValues();
    }
    if (uriMatcher.match(uri) == NET_STAT_LOG_ID) {
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    if (!contentValues.containsKey(COLUMN_NAME.CREATED_ON)) {
      contentValues.put(COLUMN_NAME.CREATED_ON, dateFormat.format(System.currentTimeMillis()));
    }
    if (!contentValues.containsKey(COLUMN_NAME.CREATED_ON_LONG)) {
      if (contentValues.containsKey(COLUMN_NAME.CREATED_ON)) {
        String strTime = (String)contentValues.get(COLUMN_NAME.CREATED_ON);
        if (!TextUtils.isEmpty(strTime)) {
          Timestamp tmp = Timestamp.valueOf(strTime);
          contentValues.put(COLUMN_NAME.CREATED_ON_LONG, tmp.getTime());
        }
        else {
          contentValues.put(COLUMN_NAME.CREATED_ON_LONG, System.currentTimeMillis());
        }
      }
      else {
        contentValues.put(COLUMN_NAME.CREATED_ON_LONG, System.currentTimeMillis());
      }
    }
    if (!contentValues.containsKey(COLUMN_NAME.YYYYMMDD)) {
      if (contentValues.containsKey(COLUMN_NAME.CREATED_ON)) {
        String strTime = (String)contentValues.get(COLUMN_NAME.CREATED_ON);
        if (!TextUtils.isEmpty(strTime)) {
          Timestamp tmp = Timestamp.valueOf(strTime);
          contentValues.put(COLUMN_NAME.YYYYMMDD, yyyymmddFormat.format(tmp.getTime()));
        }
        else {
          contentValues.put(COLUMN_NAME.YYYYMMDD, yyyymmddFormat.format(System.currentTimeMillis()));
        }
      }
      else {
        contentValues.put(COLUMN_NAME.YYYYMMDD, yyyymmddFormat.format(System.currentTimeMillis()));
      }
    }
   
    if (!contentValues.containsKey(COLUMN_NAME.RECV_BYTE)) {
      contentValues.put(COLUMN_NAME.RECV_BYTE, 0);
    }
    if (!contentValues.containsKey(COLUMN_NAME.RECV_PACKET)) {
      contentValues.put(COLUMN_NAME.RECV_PACKET, 0);
    }
    if (!contentValues.containsKey(COLUMN_NAME.RECV_ERR)) {
      contentValues.put(COLUMN_NAME.RECV_ERR, 0);
    }
    if (!contentValues.containsKey(COLUMN_NAME.SEND_BYTE)) {
      contentValues.put(COLUMN_NAME.SEND_BYTE, 0);
    }
    if (!contentValues.containsKey(COLUMN_NAME.SEND_PACKET)) {
      contentValues.put(COLUMN_NAME.SEND_PACKET, 0);
    }
    if (!contentValues.containsKey(COLUMN_NAME.SEND_ERR)) {
      contentValues.put(COLUMN_NAME.SEND_ERR, 0);
    }
    
    long newId = dbHelper.getWritableDatabase().insert(TABLE_NAME, null, contentValues);
    if (newId >= 0) {
      getContext().getContentResolver().notifyChange(uri, null);
      return ContentUris.withAppendedId(CONTENT_URI, newId);
    }
    throw new SQLException("Failed to insert row into " + uri);
  }


  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    String _selection = "";
    String _groupBy = null;
    switch(uriMatcher.match(uri)) {
    case NET_STAT_LOGS:
      _selection = selection;
      break;

    case NET_STAT_LOGS_GROUP_BY_YYYYMMDD:
      _selection = selection;
      _groupBy = COLUMN_NAME.YYYYMMDD;
      break;

    case NET_STAT_LOG_ID:
      _selection = COLUMN_NAME._ID + "=" + ContentUris.parseId(uri);
      if (!TextUtils.isEmpty(selection)) {
        _selection += " AND (" + selection + ")";
      }
      break;

    case NET_STAT_LOGS_BY_YYYYMMDD:
      _selection = COLUMN_NAME.YYYYMMDD + "=" + ContentUris.parseId(uri);
      if (!TextUtils.isEmpty(selection)) {
        _selection += " AND (" + selection + ")";
      }
      break;

    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
    Cursor cursor = dbHelper.getReadableDatabase().query(TABLE_NAME, projection, _selection, selectionArgs, _groupBy, null, sortOrder);
    cursor.setNotificationUri(getContext().getContentResolver(), uri);
    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
    int ret;

    switch(uriMatcher.match(uri)) {
    case NET_STAT_LOGS:
      ret = dbHelper.getWritableDatabase().update(TABLE_NAME, contentValues, selection, selectionArgs);
      break;
    case NET_STAT_LOG_ID:
      {
        String _selection = COLUMN_NAME._ID + "=" + ContentUris.parseId(uri);
        if (!TextUtils.isEmpty(selection)) {
          _selection += " AND (" + selection + ")";
        }
        ret = dbHelper.getWritableDatabase().update(TABLE_NAME, contentValues, _selection, selectionArgs);
      }
      break;
    case NET_STAT_LOGS_BY_YYYYMMDD:
      {
        String _selection = COLUMN_NAME.YYYYMMDD + "=" + ContentUris.parseId(uri);
        if (!TextUtils.isEmpty(selection)) {
          _selection += " AND (" + selection + ")";
        }
        ret = dbHelper.getWritableDatabase().update(TABLE_NAME, contentValues, _selection, selectionArgs);
      }
      break;

    case NET_STAT_LOGS_GROUP_BY_YYYYMMDD:
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return ret;
  }
}
