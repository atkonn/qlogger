/*
 * Copyright 2011 IoriAYANE
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
package jp.co.qsdn.android.qlogger.view.prefs;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;

import android.preference.DialogPreference;

import android.util.AttributeSet;

import android.view.View;

/**
 * @author IoriAYANE
 * @see http://relog.googlecode.com/svn/trunk/CustomLibrary/CustomLibraryA/src/jp/xii/relog/customlibrary/preference/OriginalDialogPreference.java
 */
public class OriginalDialogPreference
  extends DialogPreference 
{
  private String _summary = "";      //サマリーの文字列

  /**
   * サマリの初期状態
   * @param _summary the _summary to set
   */
  public void setDefaultSummary(String _summary) {
    this._summary = _summary;
  }

  /**
   * サマリの初期状態
   * @return the _summary
   */
  public String getDefaultSummary() {
    return _summary;
  }

  /**
   * コンストラクタ
   * @param context
   * @param attrs
   */
  public OriginalDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    String temp;
    
    //サマリーの初期状態を保存しておく
    temp = (String) getSummary();
    if(temp != null){
      setDefaultSummary(temp);
    }

  }
  
  /**
   * ダイアログの表示
   * @param context 親のコンテキスト
   * @param title ダイアログのタイトル
   * @param text ダイアログの本文
   */
  protected void showCustumDialog(Context context, String title, String text ,View custumItem, OnClickListener listener){
    AlertDialog.Builder ad = new AlertDialog.Builder(context);
    ad.setTitle(title);
    if(text != null){
      ad.setMessage(text);
    }
    ad.setView(custumItem);
    ad.setPositiveButton("OK", listener);
    ad.setNegativeButton("Cancel", null);
    ad.create();
    ad.show();
  }
  
  /**
   * 数値に変換する
   * @param number
   * @return
   */
  protected int toInt(String number){
    int num = 0;
    
    try{
      num = Integer.parseInt(number);
    }catch(Exception e){
      num = 0;
    }
    
    return num;
  }

  /**
   * 数値か確かめる
   * @param number
   * @return
   */
  protected boolean isInt(String number){
    boolean ret = false;
    
    try{
      Integer.parseInt(number);
      ret = true;
    }catch(Exception e){
      ret = false;
    }
    
    return ret;
  }
}
