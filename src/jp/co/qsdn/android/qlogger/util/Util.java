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
package jp.co.qsdn.android.qlogger.util;

import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Util {
  public static boolean isRunning(Context context, String serviceName) {
    ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

    for (RunningServiceInfo info : services) {
      if (serviceName.equals(info.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  public static void zip(String zipFileName, String inputFileName) throws FileNotFoundException, IOException {
    zips(zipFileName, new String[] {inputFileName});
  }

  private final static int BUFSZ = 1024;
  public static void zips(String zipFileName, String[] inputFileNames) throws FileNotFoundException, IOException {
    byte[] buf = new byte[BUFSZ];
    ZipOutputStream zipOutputStream = null;
    try {
      zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
      for (int ii=0; ii<inputFileNames.length; ii++) {
        FileInputStream fileInputStream = new FileInputStream(inputFileNames[ii]);
        try {
          ZipEntry zipEntry = new ZipEntry(inputFileNames[ii]);
          zipOutputStream.putNextEntry(zipEntry);
          int len=0;
          while ((len = fileInputStream.read(buf)) != -1) {
            zipOutputStream.write(buf, 0, len);
          }
        }
        finally {
          try {
            fileInputStream.close();
          }
          catch (Exception ex) {}
          try {
            zipOutputStream.closeEntry();
          }
          catch (Exception ex) {}
        }
      }
    }
    finally {
      if (zipOutputStream != null) {
        zipOutputStream.close();
      }
    }
  }
}
