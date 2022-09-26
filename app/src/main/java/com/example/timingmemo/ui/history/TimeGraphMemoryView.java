package com.example.timingmemo.ui.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;
import com.example.timingmemo.db.StampMemoTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * 記録済みの目盛りグラフ
 */
public class TimeGraphMemoryView extends View {

    //---------------------------
    // 定数
    //----------------------------
    private final float STAMP_MEMO_SIZE = 40f;
    private final int TIME_TEXT_SIZE = 40;
    private final int TIME_TEXT_CHAR_HEIGHT = TIME_TEXT_SIZE;       // テキストの縦幅は、指定サイズ
    private final int TIME_TEXT_CHAR_WIDTH = TIME_TEXT_SIZE / 2;    // テキストの横幅は、指定サイズの半分
    private final float MEMORY_UNIT_LENGTH;                         // 目盛り１単位辺りの長さ
    private final float MEMORY_START_POS_X;                         // 目盛り描画開始位置（X座標）
    private final int TIME_TEXT_WIDTH;                              // 時間テキストの長さ
    private final int TIME_TEXT_HALF_WIDTH;                         // 時間テキストの半分の長さ = (文字数 / 2) * １文字当たりの横幅　）
    private final int TIME_TEXT_CHAR_NUM = 8;                       // 時間テキストフォーマット「hh:mm:ss」の文字数

    // 描画目盛りy位置 上／下 index
    final int POSY_UPPER = 0;
    final int POSY_LOWER = 1;

    //---------------------------
    // フィールド変数
    //----------------------------
    private Paint mGraghMemoryPaint;
    private Paint mGraghTextPaint;
    private Path mGraghMemoryPath;
    private String mRecordTime;
    private float mRecordTotalMinuteTime;
    private ArrayList<Float> mGraghTextPosXList;                    // 時間テキストx位置リスト
    private ArrayList<StampMemoTable> mStampMemos;
    private Map<Integer, Paint> mStampMemoPaintMap;                 // 記録メモPaintMap

    public TimeGraphMemoryView(Context context) {
        this(context, null);
    }

    public TimeGraphMemoryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeGraphMemoryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // 目盛り１単位辺りの長さ
        MEMORY_UNIT_LENGTH = getResources().getDimension(R.dimen.memory_1min_length);
        // 時間テキストの長さ
        TIME_TEXT_WIDTH = TIME_TEXT_CHAR_NUM * TIME_TEXT_CHAR_WIDTH;
        TIME_TEXT_HALF_WIDTH = TIME_TEXT_WIDTH / 2;
        // 目盛り描画スタート位置
        MEMORY_START_POS_X = TIME_TEXT_HALF_WIDTH;

        // 時間テキストx位置リスト
        mGraghTextPosXList = new ArrayList<>();
        // Path（空）生成
        mGraghMemoryPath = new Path();
        mStampMemoPaintMap = new HashMap<>();

        // Paint生成
        setGraghPaint();
    }

    /*
     * 目盛り用／テキスト用Paintの生成
     */
    private void setGraghPaint() {

        //-----------------------------
        // 目盛り
        //-----------------------------
        // 目盛り色
        int memoryColor = getResources().getColor(R.color.mainColor);

        // Paint設定
        mGraghMemoryPaint = new Paint();
        mGraghMemoryPaint.setStyle(Paint.Style.STROKE);
        mGraghMemoryPaint.setColor(memoryColor);
        mGraghMemoryPaint.setStrokeWidth(4);

        //-----------------------------
        // テキスト
        //-----------------------------
        // テキスト色
        int textColor = getResources().getColor(R.color.mainColor);

        // Paint設定
        mGraghTextPaint = new Paint();
        mGraghTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mGraghTextPaint.setColor(textColor);
        mGraghTextPaint.setTextSize(TIME_TEXT_SIZE);
        mGraghTextPaint.setStrokeWidth(1);
    }


    /*
     * グラフ目盛りのPathを設定
     *   設定対象：開始の縦線、単位毎の縦線
     */
    public void setGraghMemoryPath() {

        //----------------
        // Path設定可否判定
        //----------------
        // 記録時間が設定されるまで、目盛りは描画しない
        if( mRecordTime == null ){
            return;
        }

        //----------------
        // Path設定
        //----------------
        // Pathリセット
        mGraghMemoryPath.reset();

        // 目盛りPathの設定
        setGraghMemoryStartMiddlePath( mGraghMemoryPath );
        setGraghMemoryEndPath( mGraghMemoryPath );

        // Pathをclose
        mGraghMemoryPath.close();
    }

    /*
     * グラフ目盛りのPathを設定
     *   設定対象：開始の縦線、単位毎の縦線
     */
    public void setGraghMemoryStartMiddlePath( Path path ) {

        //---------------------------
        // 目盛りPath生成初期化
        //---------------------------
        // 目盛りx位置の初期位置（時間テキスト分を考慮）
        float xPos = MEMORY_START_POS_X;
        // リストクリア
        mGraghTextPosXList.clear();
        // 記録時間の分データ（小数点（秒情報）は切り捨て：例) 00:20:10 → 20 ）
        int totalMinute = (int) Math.floor(mRecordTotalMinuteTime);

        //---------------------------
        // 目盛りPath生成
        //---------------------------
        // 1分毎に縦の目盛りPathを設定
        for (int drawMinute = 0; drawMinute <= totalMinute; drawMinute++) {

            //---------------------------------------
            // 時間テキストx位置
            //---------------------------------------
            // 時間テキストを表示するx位置をリストに追加
            addTextPosXList(xPos, drawMinute);

            //---------------------------------------
            // 目盛り
            //---------------------------------------
            // グラフ縦線の高さの範囲（上限・下限）を取得
            float[] drawHeightRange = getDrawHeightRange(drawMinute);

            // 縦線を設定
            path.moveTo(xPos, drawHeightRange[POSY_UPPER]);
            path.lineTo(xPos, drawHeightRange[POSY_LOWER]);

            // １目盛り分横へ移動
            xPos += MEMORY_UNIT_LENGTH;
        }
    }


    /*
     * グラフ目盛りのPathを設定
     *   設定対象：終端の縦線
     */
    private void setGraghMemoryEndPath(Path path) {

        // 目盛り端の判定を得るための値
        final int FOR_EDGE = 0;

        // 記録時間のx位置を算出
        float posX = getPosXFromTime( mRecordTime );
        // 記録時間のy位置を算出（両端のY位置を取得するために、指定時間として０を指定）
        float[] yRange = getDrawHeightRange( FOR_EDGE );

        //----------------------------------------------------------------------
        // 終端の目盛り位置をリストに追加（両端のY位置を取得するために、指定時間として０を指定）
        //----------------------------------------------------------------------
        addTextPosXList(posX, FOR_EDGE);

        //------------------
        // 終端目盛りを設定
        //------------------
        path.moveTo(posX, yRange[POSY_UPPER]);
        path.lineTo(posX, yRange[POSY_LOWER]);
    }

    /*
     * グラフ目盛りの高さ範囲を取得
     */
    private float[] getDrawHeightRange(int minute) {

        //---------------------------
        // 各目盛りのY座標
        //---------------------------
        // 縦線開始Y座標（上側）は、時間テキスト分を考慮
        float height = getHeight();
        // 両端
        final float[] everyEdgeRange = new float[]{
                // 時間テキスト高さちょうどだと詰まって見えるため、両端だけ少し下げる
                TIME_TEXT_CHAR_HEIGHT + 6,
                height
        };
        // 10単位毎
        final float[] every10Range = new float[]{
                (height * 0.1f) + TIME_TEXT_CHAR_HEIGHT,
                (height * 0.9f)
        };
        // 5単位毎
        final float[] every5Range = new float[]{
                (height * 0.15f) + TIME_TEXT_CHAR_HEIGHT,
                (height * 0.85f)
        };
        // 1単位毎
        final float[] every1Range = new float[]{
                (height * 0.25f) + TIME_TEXT_CHAR_HEIGHT,
                (height * 0.75f)
        };

        //---------------------
        // 目盛り両端判定
        //---------------------
        if ( minute == 0 ) {
            return everyEdgeRange;
        }

        //---------------------
        // 目盛り両端の間判定
        //---------------------
        if ((minute % 10) == 0) {
            // 10刻み
            return every10Range;
        } else if ((minute % 5) == 0) {
            // 5刻み
            return every5Range;
        } else {
            // 1刻み
            return every1Range;
        }
    }

    /*
     * 時間テキスト描画X位置リストへのX位置追加
     */
    private void addTextPosXList(float xPos, int minute) {

        // 10刻み or 記録時刻の場合
        if ((minute % 10) == 0) {
            mGraghTextPosXList.add(xPos);
        }
    }

    /*
     * 記録時間からグラフの横幅を計算
     *   para1：hh:mm:ss
     */
    public int calcGraphWidthFromRecordTime(String recordTime) {
        // 記録時間を保持
        mRecordTime = recordTime;
        // 記録時間を分として取得
        mRecordTotalMinuteTime = getMinuteFromTime( recordTime );

        // 記録時間に対して必要な横幅
        int timeGraghWidth = (int) ((mRecordTotalMinuteTime * MEMORY_UNIT_LENGTH) + TIME_TEXT_WIDTH);
        return timeGraghWidth;
    }

    /*
     * 記録メモリストの設定
     */
    public void setStampMemoList(ArrayList<StampMemoTable> stampMemos) {
        mStampMemos = stampMemos;
    }

    /*
     * 指定時間「hh:mm:ss」の分変換して取得
     *   例)「01:02:30」 → 62.5
     */
    private float getMinuteFromTime( String hhmmss ) {

        // 時分秒文字列を以下の形で分割
        // hh:mm:ss → 「hh」、「mm」、「ss」
        String[] hhmmssSplit = hhmmss.split(AppCommonData.TIME_FORMAT_DELIMITER);
        int hh = Integer.parseInt(hhmmssSplit[0]);
        int mm = Integer.parseInt(hhmmssSplit[1]);
        int ss = Integer.parseInt(hhmmssSplit[2]);

        // 分に変換
        int hourMin = hh * 60;
        float secondMin = ss / 60f;

        return (hourMin + mm + secondMin);
    }

    /*
     * 時間テキストの描画
     */
    private void drawTimeText(Canvas canvas) {

        //------------------------------------------------------------
        // 記録時間の直前の時間テキストが、記録時間と重複するならリストから削除
        //------------------------------------------------------------
        int last = mGraghTextPosXList.size() - 1;
        int preLast = last - 1;
        boolean isCovered = isCoveredTimeText(preLast, last);
        if (isCovered) {
            // 表示がかぶる場合、リストから削除（表示しない）
            mGraghTextPosXList.remove(preLast);
        }

        //----------------------------
        // 時間テキストを１０単位毎に描画
        //----------------------------
        // リスト最後の直前までの情報を描画
        last = mGraghTextPosXList.size() - 1;
        for (int i = 0, minute = 0; i < last; i++, minute += 10) {
            // 分を「hh:mm:ss」に変換
            String timeText = formatHHMMSS(minute);
            // 描画
            float posX = mGraghTextPosXList.get(i);
            canvas.drawText(timeText, posX - TIME_TEXT_HALF_WIDTH, TIME_TEXT_CHAR_HEIGHT, mGraghTextPaint);
        }

        //----------------------------
        // 記録時間を描画
        //----------------------------
        float posX = mGraghTextPosXList.get(last);
        canvas.drawText(mRecordTime, posX - TIME_TEXT_HALF_WIDTH, TIME_TEXT_CHAR_HEIGHT, mGraghTextPaint);
    }

    /*
     * 指定indexの時間テキストの表示が重複するかチェック
     *   true：重複
     */
    private boolean isCoveredTimeText(int preLast, int last) {

        // X座標の間隔
        float preLastPosX = mGraghTextPosXList.get(preLast);
        float lastPosX = mGraghTextPosXList.get(last);
        float distance = lastPosX - preLastPosX;

        // 時間テキストの横幅よりも小さい場合、重複
        return (distance < TIME_TEXT_WIDTH);
    }

    /*
     * 指定時間を「hh:mm:ss」フォーマットに変換する
     */
    private String formatHHMMSS(int minute) {

        int hh = minute / 60;
        int mm = minute - (hh * 60);

        return String.format("%02d:%02d:00", hh, mm);
    }

    /*
     * 記録メモの描画
     */
    private void drawStampMemos(Canvas canvas) {

        // グラフ中央位置を取得
        float centerY = getGraghCenterY();

        // 記録メモを全て描画
        for (StampMemoTable stampMemo : mStampMemos) {
            // メモ色に対応するPaintを取得
            int color = stampMemo.getMemoColor();
            Paint memoPaint = getStampMemoPaint( color );

            // 描画するX座標を取得
            String playTime = stampMemo.getStampingPlayTime();
            float posX = getPosXFromTime( playTime );

            // 描画
            canvas.drawCircle(posX, centerY, STAMP_MEMO_SIZE, memoPaint);
        }
    }


    /*
     * 記録メモ用Paintの取得
     */
    private Paint getStampMemoPaint(int keyColor) {

        // 色に対応するPaintを取得
        Paint memoPaint = mStampMemoPaintMap.get(keyColor);
        if (memoPaint == null) {
            // 色に対応するPaintがなければ、新しく生成して追加
            memoPaint = createStampMempPaint(keyColor);
            mStampMemoPaintMap.put(keyColor, memoPaint);
        }

        return memoPaint;
    }

    /*
     * 記録メモ用Paintの生成
     */
    private Paint createStampMempPaint(int color) {

        // Paint設定
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(color);

        return paint;
    }

    /*
     * 指定時間に対応するグラフ上のX位置を取得
     *   @para1：フォーマット「hh:mm:ss」
     */
    private float getPosXFromTime(String hhmmss) {
        // 時間に変換
        float minuteSecond = getMinuteFromTime( hhmmss );

        // 座標：グラフ描画スタート位置 + 指定時間分の長さ
        return MEMORY_START_POS_X + ( minuteSecond * MEMORY_UNIT_LENGTH );
    }

    /*
     * 目盛りの中央Y位置を取得（時間テキストを考慮）
     */
    private float getGraghCenterY() {
        // 時間テキストを引いた本ビューの高さ
        float heightSubTimeTextSize = getHeight() - TIME_TEXT_CHAR_HEIGHT;
        // 時間テキストを考慮した時のグラフ中央位置
        return (heightSubTimeTextSize / 2) + TIME_TEXT_CHAR_HEIGHT;
    }

    /*
     * onDraw
     */
    @Override
    protected void onDraw(Canvas canvas) {

        Log.i("通過", "drawPath()");

        // Path情報があれば描画
        if ( !mGraghMemoryPath.isEmpty() ) {
            // 目盛り描画
            canvas.drawPath(mGraghMemoryPath, mGraghMemoryPaint);
            // 時間テキストの描画
            drawTimeText( canvas );
            // 記録メモを描画
            drawStampMemos( canvas );
        }

        Log.i("通過", "getWidth()" + getWidth());
        Log.i("通過", "getHeight()" + getHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i("目盛り", "onMeasure()");
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom ){
        super.onLayout( changed, left, top, right, bottom);
        Log.i("目盛り", "onLayout() w=" + (right - left));

        // 目盛りPathの設定
        setGraghMemoryPath();
    }

}
