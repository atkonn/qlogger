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
package jp.co.qsdn.android.qlogger;


public class Constant {
  public static boolean DEBUG = true;
  public static class REQUEST {
    public final static int ERRORLOG_DETAIL = 1;

    public final static int SCREENLOG_HISTORY_GRAPH = 2;
  }

  public static class RESULT {
    public final static int OK     = 0;
    public final static int RELOAD = 1;
  }

  public final static int ERRORLOG_DELETER_NUMBER       = 9;
  public final static int BATTERY_LOGGER_NUMBER         = 8;
  public final static int SCREENHOURLY_LOGGER_NUMBER    = 7;
  public final static int SCREENDAILY_LOGGER_NUMBER     = 6;
  public final static int SCREENMONTHLY_LOGGER_NUMBER   = 5;
  public final static int LOAD_AVG_LOGGER_NUMBER        = 4;
  public final static int CPU_UTILIZATION_LOGGER_NUMBER = 3;
  public final static int MEM_UTILIZATION_LOGGER_NUMBER = 2;
  public final static int NET_STAT_LOGGER_NUMBER        = 1;

  public static class ACTION {
    public static final String BOOT_COMPLETED  = "android.intent.action.BOOT_COMPLETED";
    public static final String REBOOT          = "android.intent.action.REBOOT";
    public static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    public static final String SCREEN_ON       = "android.intent.action.SCREEN_ON";
    public static final String SCREEN_OFF      = "android.intent.action.SCREEN_OFF";
  }

  public static class SECONDS {
    public static final int EXECUTOR_TERMINATE = 5;
  }

  public static class INTERVAL {
    public static final int BATTERY_LOG         = 10 * 60 * 1000;
    public static final int SCREEN_HOURLY_LOG   = 60 * 60 * 1000;
    public static final int SCREEN_DAILY_LOG    = 60 * 60 * 1000;
    public static final int SCREEN_MONTHLY_LOG  = 60 * 60 * 1000;
    public static final int LOAD_AVG_LOG        =  1 * 60 * 1000;
    public static final int CPU_UTILIZATION_LOG =  5 * 60 * 1000;
    public static final int MEM_UTILIZATION_LOG =  5 * 60 * 1000;
    public static final int NET_STAT_LOG        =  5 * 60 * 1000;
  }


  public static class SCREEN_LOG_HISTORY {
    public static final String HOURLY_TAB   = "hourlyTab";
    public static final String DAILY_TAB    = "dailyTab";
    public static final String MONTHLY_TAB  = "monthlyTab";
  }


  public static class PROC {
    public static final String LOAD_AVG = "/proc/loadavg";
    public static final String STAT     = "/proc/stat";
    public static final String MEMINFO  = "/proc/meminfo";
    public static final String NETDEV   = "/proc/net/dev";
    public static final String NETROUTE = "/proc/net/route";
  }

  public static final String DATABASE_NAME = "qlogger.db";
  public static final int DATABASE_VERSION = 1;
}
