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

public class ListPreference 
  extends android.preference.ListPreference
{
  public ListPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    Prefs prefs = Prefs.getInstance(context);
    super.setValue(prefs.getLogcatSetting__Loglevel());
  }
  public ListPreference(Context context) {
    super(context);
    Prefs prefs = Prefs.getInstance(context);
    super.setValue(prefs.getLogcatSetting__Loglevel());
  }

  public String getValue() {
    return super.getValue();
  }
  public void setValue(String text) {
    Prefs prefs = Prefs.getInstance(getContext());
    prefs.setLogcatSetting__Loglevel(text);
    super.setValue(text);
  }
}
