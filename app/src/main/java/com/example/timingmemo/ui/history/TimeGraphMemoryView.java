package com.example.timingmemo.ui.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;

/*
 * 記録済みの目盛りグラフ
 */
public class TimeGraphMemoryView extends View {

    private Paint mGraghMemoryPaint;
    private Path mGraghMemoryPath;
    private float mMemoryLength1Min;
    private float mRecordTotalMinuteTime;

    public TimeGraphMemoryView(Context context) {
        this(context, null);
    }

    public TimeGraphMemoryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeGraphMemoryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMemoryLength1Min = getResources().getDimension(R.dimen.memory_1min_length);

        Log.i("通過", "memory_1min_length=" + getResources().getDimension(R.dimen.memory_1min_length));
        Log.i("通過", "memory_1min_length=" + getResources().getDimensionPixelSize(R.dimen.memory_1min_length));
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Log.i("通過", "memory_1min_length=" + (getResources().getDimension(R.dimen.memory_1min_length) * metrics.density));

        setGraghMemoryPaint();
    }

    /*
     *
     */
/*    private void setDrawData() {

        if (mGraghMemoryPath == null) {
            setGraghMemoryPaint();
            setGraghMemoryPath();
        }
    }*/

    /*
     *
     */
    private void setGraghMemoryPaint() {

        // 目盛り色
        int memoryColor = getResources().getColor(R.color.mainColor);

        // Paint設定
        mGraghMemoryPaint = new Paint();
        mGraghMemoryPaint.setStyle(Paint.Style.STROKE);
        mGraghMemoryPaint.setColor(memoryColor);
        mGraghMemoryPaint.setStrokeWidth(4);
    }

    /*
     * グラフ目盛りのPathを設定
     */
    public void setGraghMemoryPath() {

        float height = getHeight();

        final float[] every10Range = new float[]{
                height * 0.1f,
                height * 0.9f
        };
        final float[] every5Range = new float[]{
                height * 0.2f,
                height * 0.8f
        };
        final float[] every1Range = new float[]{
                height * 0.3f,
                height * 0.7f
        };

        // グラフ開始と終了の目盛りと中央線のPathを設定
        mGraghMemoryPath = new Path();
        setEdgeCenterMemory( mGraghMemoryPath );

        // 描画目盛りx位置
        float xPos = mMemoryLength1Min;
        // 描画目盛りy位置 上／下 index
        final int yPosUpper = 0;
        final int yPosLower = 1;

        // 1分毎に縦の目盛りPathを設定
        int totalMinute = (int) mRecordTotalMinuteTime;
        for (int drawMinute = 1; drawMinute < totalMinute; drawMinute++) {

            // グラフ縦線の高さの範囲（上限・下限）を取得
            float[] drawHeightRange = getDrawHeightRange(drawMinute, every10Range, every5Range, every1Range);

            // 縦線のPathを設定
            mGraghMemoryPath.moveTo(xPos, drawHeightRange[yPosUpper]);
            mGraghMemoryPath.lineTo(xPos, drawHeightRange[yPosLower]);

            // １目盛り分横へ移動
            xPos += mMemoryLength1Min;
        }

        // Pathを閉じる
        mGraghMemoryPath.close();

/*        Log.i("通過", "set getWidth()" + getWidth());
        Log.i("通過", "set getHeight()" + getHeight());*/
    }

    /*
     * グラフ目盛りの高さ範囲を取得
     */
    private float[] getDrawHeightRange(int minute, float[] every10Range, float[] every5Range, float[] every1Range) {

        if ((minute % 10) == 0) {
            return every10Range;
        } else if ((minute % 5) == 0) {
            return every5Range;
        } else {
            return every1Range;
        }
    }

    /*
     * グラフ開始(縦)と終了(縦)の目盛りと中央線(横)のPathを設定
     */
    private void setEdgeCenterMemory( Path path ) {

        float width = getWidth();
        float height = getHeight();
        float centerHeight = height / 2f;

        // 目盛り端（左）の縦線を描画
        path.moveTo( 0, 0 );
        path.lineTo( 0, height );

        // 目盛り端（右）の縦線を描画
        path.moveTo( width, 0 );
        path.lineTo( width, height );

        // 中央線（横）を描画
        path.moveTo( 0, centerHeight );
        path.lineTo( width, centerHeight );

        Log.i("目盛り", "setEdgeCenterMemory getWidth()" + width);
    }

    /*
     * 記録時間からグラフの横幅を計算
     *   para1：hh:mm:ss
     */
    public int calcGraphWidthFromRecordTime( String recordTime ){

        // 時分秒文字列を以下の形で分割
        // hh:mm:ss → 「hh」、「mm」、「ss」
        String[] hhmmss = recordTime.split( AppCommonData.TIME_FORMAT_DELIMITER );
        int hh = Integer.parseInt( hhmmss[0] );
        int mm = Integer.parseInt( hhmmss[1] );
        int ss = Integer.parseInt( hhmmss[2] );

        // 分に変換
        int hourMin = hh * 60;
        float secondMin = ss / 60f;
        mRecordTotalMinuteTime = hourMin + mm + secondMin;

        // 横幅を返す
        return (int)(mRecordTotalMinuteTime * mMemoryLength1Min);
    }

    /*
     * onDraw
     */
    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        Log.i( "通過", "drawPath()" );
//        setDrawData();

        if( mGraghMemoryPath != null ){
            canvas.drawPath(mGraghMemoryPath, mGraghMemoryPaint);
        }

        Log.i( "通過", "getWidth()" + getWidth()  );
        Log.i( "通過", "getHeight()" + getHeight()  );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //int width = getMeasuredWidth();
/*
        int time = 200;
        int min = time / 60;
        int second = time % 60;

        int len = (int) ((min * mMemoryLength1Min) + ( mMemoryLength1Min * (mMemoryLength1Min / 60f) ));

        Log.i( "値チェック", "len=" + len  );

        setMeasuredDimension(len, heightMeasureSpec);*/
        Log.i("目盛り", "onMeasure()");
    }


}
