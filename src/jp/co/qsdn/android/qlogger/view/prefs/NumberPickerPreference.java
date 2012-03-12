/*
 * Copyright 2011 IoriAYANE
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

import android.preference.PreferenceManager;

import android.util.AttributeSet;
import android.util.Log;

import android.view.View;

import jp.co.qsdn.android.qlogger.view.picker.NumberPicker;
import jp.co.qsdn.android.qlogger.prefs.Prefs;

/**
 * @author IoriAYANE
 * @see http://relog.googlecode.com/svn/trunk/CustomLibrary/CustomLibraryA/src/jp/xii/relog/customlibrary/preference/NumberPickerPreference.java
 */
public class NumberPickerPreference
  extends OriginalDialogPreference {

  private final String TAG = getClass().getName();

  //追加属性の名称
  private final static String STR_ATTR_DEFAULT_NUMBER = "defaultNumber";
  
  //プリファレンス保存時のキー名の追加文字列
  private final static String STR_KEY_NUMBER = "_Number";    
  
  private int _defaultNumber = 0;      //設定値
  private Context mContext;
  
  /**
   * コンストラクタ
   * @param context
   * @param attrs
   */
  public NumberPickerPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    try {
      mContext = context.createPackageContext(Prefs.PACKAGE_NAME, Context.CONTEXT_RESTRICTED);
    }
    catch (NameNotFoundException ex) {
      Log.e(TAG, ex.getLocalizedMessage());
    }

    String temp;
    
    //値を取得
    temp = attrs.getAttributeValue(null, STR_ATTR_DEFAULT_NUMBER);
    if(temp != null){
      _defaultNumber = Integer.valueOf(temp);
    }
  }

  /**
   * 表示したときに呼ばれる
   */
  @Override
  protected void onBindView(View view) {
    //設定を読み込み
    SharedPreferences pref = mContext.getSharedPreferences(Prefs.PACKAGE_NAME,Context.MODE_PRIVATE);
    if(pref == null){
    }else{
      _defaultNumber = pref.getInt(getKey() + STR_KEY_NUMBER, _defaultNumber);
    }

    //サマリーに現在値を設定
    String summary = "";
    summary = String.format(getDefaultSummary(), _defaultNumber);
    setSummary((summary));

    super.onBindView(view);
  }

  /**
   * プリファレンスのクリックイベント
   */
  @Override
  protected void onClick(){

    //ダイアログ表示
    final NumberPicker picker = new NumberPicker(getContext());
    picker.setRange(0, 100000);
    picker.setCurrent(_defaultNumber);
    showCustumDialog(getContext(), (String)getDialogTitle(), (String)getDialogMessage()
              , picker, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // 設定保存
        SharedPreferences pref = mContext.getSharedPreferences(Prefs.PACKAGE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(getKey() + STR_KEY_NUMBER, picker.getCurrent());
        editor.commit();
        
        //表示を更新
        notifyChanged();
      }
    });

  }
}
