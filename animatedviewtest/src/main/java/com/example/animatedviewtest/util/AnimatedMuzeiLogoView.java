/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.animatedviewtest.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.example.animatedviewtest.ShowAnimateActivity;

import java.text.ParseException;
import java.util.ArrayList;


/**
 * 开始进行的动画View
 */
public class AnimatedMuzeiLogoView extends View {

    private static final int TRACE_TIME = 2000;//微量调整时间
    private static final int TRACE_TIME_PER_GLYPH = 1000;
    private static final int FILL_START = 1200;
    private static final int FILL_TIME = 2000;
    private static final int MARKER_LENGTH_DIP = 16;
    private static final int TRACE_RESIDUE_COLOR = Color.argb(50, 255, 255, 255);//实线的颜色
    private static final int TRACE_COLOR = Color.WHITE;//白块的颜色
    private static final int TRACE_FIRST_COLOR = Color.RED;//白块的颜色
    private static final PointF VIEWPORT = new PointF(1000, 300);

    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    private Paint mFillPaint;
    private GlyphData[] mGlyphData;
    private float mMarkerLength;
    private int mWidth;
    private int mHeight;
    private long mStartTime;

    public static final int STATE_NOT_STARTED = 0;
    public static final int STATE_TRACE_STARTED = 1;
    public static final int STATE_FILL_STARTED = 2;
    public static final int STATE_FINISHED = 3;

    private int mState = STATE_NOT_STARTED;
    private OnStateChangeListener mOnStateChangeListener;

    private ArrayList<String> mPathStringList;

    public AnimatedMuzeiLogoView(Context context) {
        super(context);
        init();
    }

    public AnimatedMuzeiLogoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedMuzeiLogoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        this.mPathStringList = ShowAnimateActivity.mPathStringList;
        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);//给Paint加上抗锯齿标志
        mFillPaint.setStyle(Paint.Style.FILL);//实心

        mMarkerLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MARKER_LENGTH_DIP, getResources().getDisplayMetrics());

        // See https://github.com/romainguy/road-trip/blob/master/application/src/main/java/org/curiouscreature/android/roadtrip/IntroView.java
        // Note: using a software layer here is an optimization. This view works with
        // hardware accelerated rendering but every time a path is modified (when the
        // dash path effect is modified), the graphics pipeline will rasterize the path
        // again in a new texture. Since we are dealing with dozens of paths, it is much
        // more efficient to rasterize the entire view into a single re-usable texture
        // instead. Ideally this should be toggled using a heuristic based on the number
        // and or dimensions of paths to render.
        // Note that PathDashPathEffects can lead to clipping issues with hardware rendering.
        setLayerType(LAYER_TYPE_SOFTWARE, null);//此View 通过软件渲染为一个bitmap
    }

    public void start() {
        mStartTime = System.currentTimeMillis();
        changeState(STATE_TRACE_STARTED);//设置状态为开始
        postInvalidateOnAnimation();//请求重绘，开始调用onDraw()
    }

    public void reset() {
        mStartTime = 0;
        changeState(STATE_NOT_STARTED);
        postInvalidateOnAnimation();
    }

    public void setToFinishedFrame() {
        mStartTime = 1;
        changeState(STATE_FINISHED);
        postInvalidateOnAnimation();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        rebuildGlyphData();
    }

    private void rebuildGlyphData() {
        SvgPathParser parser = new SvgPathParser() {
            @Override
            protected float transformX(float x) {
                return x;
            }

            @Override
            protected float transformY(float y) {
                return y;
            }
        };

        mGlyphData = new GlyphData[mPathStringList.size()];
        for (int i = 0; i < mPathStringList.size(); i++) {
            mGlyphData[i] = new GlyphData();
            try {
                mGlyphData[i].path = parser.parsePath(mPathStringList.get(i));
            } catch (ParseException e) {
                mGlyphData[i].path = new Path();
            }
            PathMeasure pm = new PathMeasure(mGlyphData[i].path, true);
            while (true) {
                mGlyphData[i].length = Math.max(mGlyphData[i].length, pm.getLength());
                if (!pm.nextContour()) {
                    break;
                }
            }
            mGlyphData[i].paint = new Paint();
            mGlyphData[i].paint.setStyle(Paint.Style.STROKE);//设置画笔为空心
            mGlyphData[i].paint.setAntiAlias(true);
            mGlyphData[i].paint.setColor(Color.WHITE);
            mGlyphData[i].paint.setStrokeWidth(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                            getResources().getDisplayMetrics()));//这是线条宽度
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mState == STATE_NOT_STARTED || mGlyphData == null) {
            return;
        }

        long t = System.currentTimeMillis() - mStartTime;

        // Draw outlines (starts as traced)
        for (int i = 0; i < mGlyphData.length; i++) {
            float phase = MathUtil.constrain(0, 1,
                    (t - (TRACE_TIME - TRACE_TIME_PER_GLYPH) * i * 1f / mGlyphData.length)
                            * 1f / TRACE_TIME_PER_GLYPH);
            float distance = INTERPOLATOR.getInterpolation(phase) * mGlyphData[i].length;
            mGlyphData[i].paint.setColor(TRACE_RESIDUE_COLOR);
            mGlyphData[i].paint.setPathEffect(new DashPathEffect(
                    new float[]{distance, mGlyphData[i].length}, 0));//背后实线的画笔
            canvas.drawPath(mGlyphData[i].path, mGlyphData[i].paint);

            mGlyphData[i].paint.setColor(TRACE_COLOR);
            mGlyphData[i].paint.setPathEffect(new DashPathEffect(
                    new float[]{0, distance, phase > 0 ? mMarkerLength : 0,
                            mGlyphData[i].length}, 0));//线条前端白块的画笔
            canvas.drawPath(mGlyphData[i].path, mGlyphData[i].paint);

        }

        if (t > FILL_START) {
            if (mState < STATE_FILL_STARTED) {//切换状态
                changeState(STATE_FILL_STARTED);
            }

            // If after fill start, draw fill
            float phase = MathUtil.constrain(0, 1, (t - FILL_START) * 1f / FILL_TIME);//根据时间计算透明度的值
            mFillPaint.setARGB((int) (phase * 255), 255, 255, 255);
            for (GlyphData glyphData : mGlyphData) {
                canvas.drawPath(glyphData.path, mFillPaint);
            }
        }

        if (t < FILL_START + FILL_TIME) {
            // draw next frame if animation isn't finished
            postInvalidateOnAnimation();//继续画，直到结束
        } else {
            changeState(STATE_FINISHED);
        }
    }

    private void changeState(int state) {
        if (mState == state) {
            return;
        }

        mState = state;
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener.onStateChange(state);
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        mOnStateChangeListener = onStateChangeListener;
    }

    public static interface OnStateChangeListener {
        void onStateChange(int state);
    }

    private static class GlyphData {
        Path path;
        Paint paint;
        float length;
    }
}
