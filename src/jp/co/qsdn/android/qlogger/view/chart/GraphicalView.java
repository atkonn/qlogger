/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
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
package jp.co.qsdn.android.qlogger.view.chart;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.os.Build;

import android.view.View;

import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.RoundChart;
import org.achartengine.chart.XYChart;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

/**
 * The view that encapsulates the graphical chart.
 */
public class GraphicalView
  extends View
{
  /** The chart to be drawn. */
  private AbstractChart mChart;
  /** The paint to be used when drawing the chart. */
  private Paint mPaint = new Paint();
  /** */
  private Canvas mBitmapCanvas;
  private DefaultRenderer mRenderer;

  /**
   * Creates a new graphical view.
   * 
   * @param context the context
   * @param chart the chart to be drawn
   * @param bitmap 
   */
  public GraphicalView(Context context, AbstractChart chart, Bitmap bitmap) {
    super(context);
    mChart = chart;
    if (mChart instanceof XYChart) {
      mRenderer = ((XYChart) mChart).getRenderer();
    } else {
      mRenderer = ((RoundChart) mChart).getRenderer();
    }
    if (bitmap != null) {
      mBitmapCanvas = new Canvas(bitmap);
      if (mRenderer.isApplyBackgroundColor()) {
        mBitmapCanvas.drawColor(mRenderer.getBackgroundColor());
      }
    }
    setEnabled(true);
  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int top = 0;
    int left = 0;
    int width = getWidth();
    int height = getHeight();
    mChart.draw(canvas, left, top, width, height, mPaint);
    if (mBitmapCanvas != null) {
      mChart.draw(mBitmapCanvas, left, top, width, height, mPaint);
    }
  }
}
