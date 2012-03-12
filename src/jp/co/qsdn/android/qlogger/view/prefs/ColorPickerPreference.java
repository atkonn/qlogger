/*
 * Copyright 2012 Atsushi Konno
 * Copyright 2012 QSDN,Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.qsdn.android.qlogger.view.prefs;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;

import android.preference.PreferenceManager;

import android.util.AttributeSet;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.LinearLayout;

import jp.co.qsdn.android.qlogger.R;
import jp.co.qsdn.android.qlogger.prefs.Prefs;
import jp.co.qsdn.android.qlogger.view.picker.ColorPicker;

/**
 * Based on Iori AYANE's OriginalDialogPreference
 * @see http://code.google.com/p/relog/ 
 */
public class ColorPickerPreference
  extends OriginalDialogPreference {

  private final String TAG = getClass().getName();

  private final static String STR_ATTR_DEFAULT_COLOR = "defaultColor";
  
  private final static String STR_KEY_COLOR = "_Color";
  
  private int _defaultColor = 0;
  private Context mContext;

  LayoutInflater inflater;
  
  /**
   * @param context
   * @param attrs
   */
  public ColorPickerPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    try {
      mContext = context.createPackageContext(Prefs.PACKAGE_NAME, Context.CONTEXT_RESTRICTED);
    }
    catch (NameNotFoundException ex) {
      Log.e(TAG, ex.getLocalizedMessage());
    }

    inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    String temp;
    
    temp = attrs.getAttributeValue(null, STR_ATTR_DEFAULT_COLOR);
    if(temp != null){
      if (temp.startsWith("0x")) {
        _defaultColor = Integer.valueOf(temp.substring(2), 16);
      }
      else if (temp.startsWith("#")) {
        _defaultColor = Integer.valueOf(temp.substring(1), 16);
      }
      else {
        _defaultColor = Integer.valueOf(temp);
      }
      _defaultColor += 0xFF000000;
    }
  }

  @Override
  protected void onBindView(View view) {
    SharedPreferences pref = mContext.getSharedPreferences(Prefs.PACKAGE_NAME,Context.MODE_PRIVATE);
    if (pref != null) {
      _defaultColor = pref.getInt(getKey() + STR_KEY_COLOR, _defaultColor);
    }

    String summary = "";
    summary = String.format(getDefaultSummary(), 0xFFFFFF & _defaultColor);
    setSummary((summary));

    super.onBindView(view);
  }

  @Override
  protected void onClick(){
    LinearLayout pickerView = (LinearLayout)inflater.inflate(R.layout.color_picker, null);

    final ColorPicker picker = (ColorPicker)pickerView.findViewById(R.id.color_picker);
    picker.setCurrentColor(_defaultColor);
    String message = (String)getDialogMessage();
    if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      message = null;
    }
    showCustumDialog(getContext(), (String)getDialogTitle(), message, pickerView, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // 設定保存
        SharedPreferences pref = mContext.getSharedPreferences(Prefs.PACKAGE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(getKey() + STR_KEY_COLOR, picker.getCurrentColor());
        editor.commit();
        
        //表示を更新
        notifyChanged();
      }
    });
  }
}
