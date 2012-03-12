package jp.co.qsdn.android.qlogger;

import jp.co.qsdn.android.qlogger.core.LogLine;

interface ILogcatService {
  java.util.List<LogLine> getLog();
}
