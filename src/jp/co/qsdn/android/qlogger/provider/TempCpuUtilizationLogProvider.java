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



public class TempCpuUtilizationLogProvider
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
        "  user REAL not null," +
        "  nice REAL not null," +
        "  system REAL not null," +
        "  idle REAL not null " +
        ");"
      );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
  }
  public static final String AUTHORITY = "jp.co.qsdn.android.qlogger.provider.TempCpuUtilizationLog";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/logs");
  public static class COLUMN_NAME {
    public static final String _ID             = "_id";
    public static final String USER            = "user";
    public static final String NICE            = "nice";
    public static final String SYSTEM          = "system";
    public static final String IDLE            = "idle";
  }
  public static String[] PROJECTION = {
    COLUMN_NAME._ID,
    COLUMN_NAME.USER,
    COLUMN_NAME.NICE,
    COLUMN_NAME.SYSTEM,
    COLUMN_NAME.IDLE
  };
  public static class COLUMN_INDEX {
    public static final int _ID                = 0;
    public static final int USER               = 1;
    public static final int NICE               = 2;
    public static final int SYSTEM             = 3;
    public static final int IDLE               = 4;
  }

  public static final int CPU_UTILIZATION_LOGS                   = 0;
  public static final int CPU_UTILIZATION_LOG_ID                 = 1;

  protected static final String TABLE_NAME = "temp_cpu_utilization_log";

  private SimpleDateFormat dateFormat     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
  private SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyyMMdd");

  protected DBHelper dbHelper;

  private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    uriMatcher.addURI(AUTHORITY, "logs", CPU_UTILIZATION_LOGS);
    uriMatcher.addURI(AUTHORITY, "logs/#", CPU_UTILIZATION_LOG_ID);
  };

  public TempCpuUtilizationLogProvider() {
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
    case CPU_UTILIZATION_LOGS:
      ret = dbHelper.getWritableDatabase().delete(TABLE_NAME, selection, selectionArgs);
      break;

    case CPU_UTILIZATION_LOG_ID:
      {
        long parseId = ContentUris.parseId(uri);
        String _selection = COLUMN_NAME._ID + "=" + parseId;
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
    case CPU_UTILIZATION_LOGS:
      return "vnd.android.cursor.dir/vnd.qsdn.tempcpuutilizationlog";

    case CPU_UTILIZATION_LOG_ID:
      return "vnd.android.cursor.item/vnd.qsdn.tempcpuutilizationlog";

    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    if (contentValues == null) {
      contentValues = new ContentValues();
    }
    if (uriMatcher.match(uri) == CPU_UTILIZATION_LOG_ID) {
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    if (!contentValues.containsKey(COLUMN_NAME.USER)) {
      contentValues.put(COLUMN_NAME.USER, 0);
    }
    if (!contentValues.containsKey(COLUMN_NAME.NICE)) {
      contentValues.put(COLUMN_NAME.NICE, 0);
    }
    if (!contentValues.containsKey(COLUMN_NAME.SYSTEM)) {
      contentValues.put(COLUMN_NAME.SYSTEM, 0);
    }
    if (!contentValues.containsKey(COLUMN_NAME.IDLE)) {
      contentValues.put(COLUMN_NAME.IDLE, 0);
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
    case CPU_UTILIZATION_LOGS:
      _selection = selection;
      break;

    case CPU_UTILIZATION_LOG_ID:
      _selection = COLUMN_NAME._ID + "=" + ContentUris.parseId(uri);
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
    case CPU_UTILIZATION_LOGS:
      ret = dbHelper.getWritableDatabase().update(TABLE_NAME, contentValues, selection, selectionArgs);
      break;
    case CPU_UTILIZATION_LOG_ID:
      {
        String _selection = COLUMN_NAME._ID + "=" + ContentUris.parseId(uri);
        if (!TextUtils.isEmpty(selection)) {
          _selection += " AND (" + selection + ")";
        }
        ret = dbHelper.getWritableDatabase().update(TABLE_NAME, contentValues, _selection, selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return ret;
  }
}
