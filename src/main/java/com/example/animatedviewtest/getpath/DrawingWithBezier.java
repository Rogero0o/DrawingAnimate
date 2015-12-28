package com.example.animatedviewtest.getpath;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.animatedviewtest.ShowAnimateActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DrawingWithBezier extends View {
    private float mX;
    private float mY;

    private final Paint mGesturePaint = new Paint();
    private final Path mPath = new Path();
    private StringBuffer pathString;
    private DecimalFormat fnum;

    public DrawingWithBezier(Context context) {
        super(context);
        init();
    }

    public DrawingWithBezier(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStyle(Style.STROKE);
        mGesturePaint.setStrokeWidth(1);
        mGesturePaint.setColor(Color.WHITE);
        fnum = new DecimalFormat("##0.000");
        pathString = new StringBuffer();
        ShowAnimateActivity.mPathStringList = new ArrayList<String>();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_UP:
                pathString.append("z");
                ShowAnimateActivity.mPathStringList.add(new String(pathString.toString()));
                pathString.delete(0, pathString.length() - 1);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);

        }
        // 更新绘制
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        // 通过画布绘制多点形成的图形
        canvas.drawPath(mPath, mGesturePaint);
    }

    // 手指点下屏幕时调用
    private void touchDown(MotionEvent event) {

        // mPath.rewind();
        // 重置绘制路线，即隐藏之前绘制的轨迹
        // mPath.reset();
        float x = event.getX();
        float y = event.getY();


        mX = x;
        mY = y;
        // mPath绘制的绘制起点
        mPath.moveTo(x, y);
        pathString.append("M").append(fnum.format(x)).append(",").append(fnum.format(y));
    }

    // 手指在屏幕上滑动时调用
    private void touchMove(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        // 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
        if (dx >= 3 || dy >= 3) {
            // 设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;

            // 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);
            pathString.append("Q").append(fnum.format(previousX)).append(",").append(fnum.format(previousY)).append(",").append(fnum.format(cX)).append(",").append(fnum.format(cY));
            // 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;
        }
    }

    public void reset() {
        mPath.reset();
    }
}
