package com.example.timingmemo.ui.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.example.timingmemo.R;

/*
 * 記録済みの目盛りグラフ
 */
public class TimeGraphMemoryView_tmp extends View {

    private Paint mGraghMemoryPaint;
    private Path mGraghMemoryPath;
    private float mMemoryLength1Min;


    public TimeGraphMemoryView_tmp(Context context) {
        this(context, null);
    }

    public TimeGraphMemoryView_tmp(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeGraphMemoryView_tmp(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMemoryLength1Min = getResources().getDimension( R.dimen.memory_1min_length );

        Log.i( "通過", "memory_1min_length=" + getResources().getDimension(R.dimen.memory_1min_length ));
        Log.i( "通過", "memory_1min_length=" + getResources().getDimensionPixelSize(R.dimen.memory_1min_length ));
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Log.i( "通過", "memory_1min_length=" + (getResources().getDimension(R.dimen.memory_1min_length ) * metrics.density) );
    }

    /*
     *
     */
    private void setDrawData(){

        if( mGraghMemoryPath == null ){
            setGraghMemoryPaint();
            setGraghMemoryPath();
        }
    }

    /*
     *
     */
    private void setGraghMemoryPaint(){

        // 目盛り色
        int memoryColor = getResources().getColor( R.color.mainColor );

        // Paint設定
        mGraghMemoryPaint = new Paint();
        mGraghMemoryPaint.setStyle( Paint.Style.STROKE );
        mGraghMemoryPaint.setColor( memoryColor );
        mGraghMemoryPaint.setStrokeWidth( 4 );
    }

    /*
     *
     */
    private void setGraghMemoryPath(){

        float width = getWidth();
        float height = getHeight();

//        float y01 = height * 0.1f;
        float y01 = 0.0f;
        float y03 = height * 0.3f;
        float y05 = height * 0.5f;
        float y07 = height * 0.7f;
        float y09 = height;
//        float y09 = height * 0.9f;

        mGraghMemoryPath = new Path();
        mGraghMemoryPath.moveTo(0, y05);
        mGraghMemoryPath.lineTo(width, y05);

        mGraghMemoryPath.moveTo(0, y01);
        mGraghMemoryPath.lineTo(0, y09);

        mGraghMemoryPath.moveTo(40, y01);
        mGraghMemoryPath.lineTo(40, y09);

        mGraghMemoryPath.moveTo(width, y01);
        mGraghMemoryPath.lineTo(width, y09);
        mGraghMemoryPath.close();

        Log.i( "通過", "set getWidth()" + getWidth()  );
        Log.i( "通過", "set getHeight()" + getHeight()  );
    }

    /*
     * onDraw
     */
    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        Log.i( "通過", "drawPath()" );
        setDrawData();
        canvas.drawPath(mGraghMemoryPath, mGraghMemoryPaint);

        Log.i( "通過", "getWidth()" + getWidth()  );
        Log.i( "通過", "getHeight()" + getHeight()  );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //int width = getMeasuredWidth();

        int time = 200;
        int min = time / 60;
        int second = time % 60;

        int len = (int) ((min * mMemoryLength1Min) + ( mMemoryLength1Min * (mMemoryLength1Min / 60f) ));

        Log.i( "値チェック", "len=" + len  );

        setMeasuredDimension(len, heightMeasureSpec);
    }


}
