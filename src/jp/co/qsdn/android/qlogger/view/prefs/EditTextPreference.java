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
package jp.co.qsdn.android.qlogger.view.prefs;

import android.content.Context;
import android.util.AttributeSet;


import jp.co.qsdn.android.qlogger.prefs.Prefs;

public class EditTextPreference 
  extends android.preference.EditTextPreference 
{
  public EditTextPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    Prefs prefs = Prefs.getInstance(context);   
    super.setText(prefs.getLogcatSetting__FilterKeyword());
  }
  public EditTextPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    Prefs prefs = Prefs.getInstance(context);   
    super.setText(prefs.getLogcatSetting__FilterKeyword());
  }
  public EditTextPreference(Context context) {
    super(context);
    Prefs prefs = Prefs.getInstance(context);   
    super.setText(prefs.getLogcatSetting__FilterKeyword());
  }
  public String getText() {
    return super.getText();
  }
  public void setText(String text) {
    Prefs prefs = Prefs.getInstance(getContext());   
    prefs.setLogcatSetting__FilterKeyword(text);
    super.setText(text);
  }
}
