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
public class RecordTimeGraphView extends View {

    //---------------------------
    // 定数
    //----------------------------
    private final float STAMP_MEMO_SIZE = 40f;
    private final int TIME_TEXT_SIZE = 40;
    private final int TIME_TEXT_CHAR_HEIGHT = TIME_TEXT_SIZE;       // テキストの縦幅は、指定サイズ
    private final int TIME_TEXT_CHAR_WIDTH = TIME_TEXT_SIZE / 2;    // テキストの横幅は、指定サイズの半分
    private final float SCALE_UNIT_LENGTH;                          // 目盛り１単位辺りの長さ
    private final float SCALE_START_POS_X;                          // 目盛り描画開始位置（X座標）
    private final int TIME_TEXT_WIDTH;                              // 時間テキストの長さ
    private final int TIME_TEXT_HALF_WIDTH;                         // 時間テキストの半分の長さ = (文字数 / 2) * １文字当たりの横幅　）
    private final int TIME_TEXT_CHAR_NUM = 8;                       // 時間テキストフォーマット「hh:mm:ss」の文字数

    // 描画目盛りy位置 上／下 index
    final int POSY_UPPER = 0;
    final int POSY_LOWER = 1;

    // 目盛り単位
    final int SCALE_UNIT_10_MIN = 0;   // 10min刻み：最小目盛り間隔=1min
    final int SCALE_UNIT_1_MIN = 1;    // 1min(60s)刻み：最小目盛り間隔=6s

    // 目盛り高さ種別
    final int SCALE_HEIGHT_HIGH = 0;
    final int SCALE_HEIGHT_MIDDLE = 1;
    final int SCALE_HEIGHT_LOW = 2;

    //---------------------------
    // フィールド変数
    //----------------------------
    private Paint mGraghScalePaint;
    private Paint mGraghTextPaint;
    private Path mGraghScalePath;
    private String mRecordTime;
    private float mRecordTotalMinute;
    private int mScaleUnit;
    private ArrayList<Float> mGraghTextPosXList;                    // 時間テキストx位置リスト
    private ArrayList<StampMemoTable> mStampMemos;
    private Map<Integer, Paint> mStampMemoPaintMap;                 // 記録メモPaintMap



    public RecordTimeGraphView(Context context) {
        this(context, null);
    }

    public RecordTimeGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordTimeGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // 目盛り１単位辺りの長さ
        SCALE_UNIT_LENGTH = getResources().getDimension(R.dimen.scale_1min_length);
        // 時間テキストの長さ
        TIME_TEXT_WIDTH = TIME_TEXT_CHAR_NUM * TIME_TEXT_CHAR_WIDTH;
        TIME_TEXT_HALF_WIDTH = TIME_TEXT_WIDTH / 2;
        // 目盛り描画スタート位置
        SCALE_START_POS_X = TIME_TEXT_HALF_WIDTH;

        // 時間テキストx位置リスト
        mGraghTextPosXList = new ArrayList<>();
        // Path（空）生成
        mGraghScalePath = new Path();
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
        int scaleColor = getResources().getColor(R.color.mainColor);

        // Paint設定
        mGraghScalePaint = new Paint();
        mGraghScalePaint.setStyle(Paint.Style.STROKE);
        mGraghScalePaint.setColor(scaleColor);
        mGraghScalePaint.setStrokeWidth(4);

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
    private void setGraghScalePath() {

        //----------------
        // Path設定可否判定
        //----------------
        // 記録時間が設定されるまで、目盛りは描画しない
        if (mRecordTime == null) {
            return;
        }

        //----------------
        // Path設定
        //----------------
        // Pathリセット
        mGraghScalePath.reset();

        // 目盛りPathの設定
        setGraghScaleStartMiddlePath(mGraghScalePath);
        setGraghScaleEndPath(mGraghScalePath);

        // Pathをclose
        mGraghScalePath.close();
    }

    /*
     * グラフ目盛りのPathを設定
     *   設定対象：開始の縦線、単位毎の縦線
     */
    private void setGraghScaleStartMiddlePath(Path path) {

        //---------------------------
        // 目盛りPath生成初期化
        //---------------------------
        // 目盛りx位置の初期位置（時間テキスト分を考慮）
        float xPos = SCALE_START_POS_X;
        // リストクリア
        mGraghTextPosXList.clear();
        // 目盛り間隔に応じた記録時間を取得
        int totalTime = getDrawScaleTotalTime();
        // １目盛り進む時間を取得
        int timeAdvance = get1ScaleTimeAdvance();

        //---------------------------
        // 目盛りPath生成
        //---------------------------
        // 1単位毎に目盛りPathを設定
        for (int drawTime = 0; drawTime <= totalTime; drawTime += timeAdvance) {

            //---------------------------------------
            // 時間テキストx位置
            //---------------------------------------
            // 時間テキストを表示するx位置をリストに追加
            addTextPosXList(xPos, drawTime);

            //---------------------------------------
            // 目盛り
            //---------------------------------------
            // グラフ縦線の高さの範囲（上限・下限）を取得
            float[] drawHeightRange = getDrawHeightRange(drawTime);

            // 縦線を設定
            path.moveTo(xPos, drawHeightRange[POSY_UPPER]);
            path.lineTo(xPos, drawHeightRange[POSY_LOWER]);

            // １目盛り分横へ移動
            xPos += SCALE_UNIT_LENGTH;
        }
    }


    /*
     * グラフ目盛りのPathを設定
     *   設定対象：終端の縦線
     */
    private void setGraghScaleEndPath(Path path) {

        // 目盛り端の判定を得るための値
        final int FOR_EDGE = 0;

        // 記録時間のx位置を算出
        float posX = getPosXFromTime(mRecordTime);
        // 記録時間のy位置を算出（両端のY位置を取得するために、指定時間として０を指定）
        float[] yRange = getDrawHeightRange(FOR_EDGE);

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
     * 目盛り描画の合計時間を取得
     */
    private int getDrawScaleTotalTime() {

        if (mScaleUnit == SCALE_UNIT_10_MIN) {
            //  １０分刻みの場合は、記録時間の分データ（小数点（秒情報）は切り捨て：例) 00:20:10 → 20 ）
            return (int) Math.floor(mRecordTotalMinute);

        } else {
            //  １０分刻みの場合は、記録時間の秒データ
            return (int) (mRecordTotalMinute * 60f);
        }
    }

    /*
     * 1目盛り進む時間を取得
     */
    private int get1ScaleTimeAdvance() {

        if (mScaleUnit == SCALE_UNIT_10_MIN) {
            // １０分刻みの場合は、最小目盛りは１分単位
            return 1;
        } else {
            // １分刻みの場合は、最小目盛りは６秒単位
            return 6;
        }
    }

    /*
     * 10目盛り進む時間を取得
     */
    private int get10ScaleTimeAdvance() {

        if (mScaleUnit == SCALE_UNIT_10_MIN) {
            // １０分刻みの場合は、目盛りは10分単位
            return 10;
        } else {
            // １分刻みの場合は、最小目盛りは1分単位
            return 1;
        }
    }

    /*
     * 指定時間における目盛りの長さの取得
     */
    private float getScaleLengthFromTime(float time) {

        // 目盛り間隔に応じて目盛りの長さを計算
        if (mScaleUnit == SCALE_UNIT_10_MIN) {
            return getScale10UnitLength(time);
        } else {
            return getScale1UnitLength(time);
        }
    }

    /*
     * 指定時間における目盛りの長さの取得：目盛り１０分刻み
     */
    private float getScale10UnitLength(float minuteSecond) {

        // 指定時間の目盛りの長さ
        return (minuteSecond * SCALE_UNIT_LENGTH);
    }

    /*
     * 指定時間における目盛りの長さの取得：目盛り１分刻み
     */
    private float getScale1UnitLength(float minuteSecond) {

        // 分／秒を秒に変換
        float second = minuteSecond * 60;
        // 目盛り数
        int scaleNum = (int) (second / 6);

        // 指定時間の目盛りの長さ
        return (scaleNum * SCALE_UNIT_LENGTH);
    }

    /*
     * グラフ目盛りの高さ範囲を取得
     */
    private float[] getDrawHeightRange(int time) {

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
        final float[] everyHighRange = new float[]{
                (height * 0.1f) + TIME_TEXT_CHAR_HEIGHT,
                (height * 0.9f)
        };
        // 5単位毎
        final float[] everyMiddleRange = new float[]{
                (height * 0.15f) + TIME_TEXT_CHAR_HEIGHT,
                (height * 0.85f)
        };
        // 1単位毎
        final float[] everyLowRange = new float[]{
                (height * 0.25f) + TIME_TEXT_CHAR_HEIGHT,
                (height * 0.75f)
        };

        //---------------------
        // 目盛り両端判定
        //---------------------
        if (time == 0) {
            return everyEdgeRange;
        }

        //---------------------
        // 目盛りの高さ範囲の取得
        //---------------------
        // 目盛りの高さ判定
        int drawHeight;
        if (mScaleUnit == SCALE_UNIT_10_MIN) {
            drawHeight = getDrawHeight10min(time);
        } else {
            drawHeight = getDrawHeight1min(time);
        }

        // 高さに応じた範囲を取得
        if (drawHeight == SCALE_HEIGHT_HIGH) {
            return everyHighRange;
        } else if (drawHeight == SCALE_HEIGHT_MIDDLE) {
            return everyMiddleRange;
        } else {
            return everyLowRange;
        }
    }

    /*
     * グラフ目盛りの高さ種別を取得：１０分刻み
     */
    private int getDrawHeight10min(int minute) {

        if ((minute % 10) == 0) {
            // 10min刻み
            return SCALE_HEIGHT_HIGH;
        } else if ((minute % 5) == 0) {
            // 5min刻み
            return SCALE_HEIGHT_MIDDLE;
        } else {
            // 1min刻み
            return SCALE_HEIGHT_LOW;
        }
    }

    /*
     * グラフ目盛りの高さ種別を取得：１分刻み
     */
    private int getDrawHeight1min(int second) {

        if ((second % 60) == 0) {
            // 60s刻み
            return SCALE_HEIGHT_HIGH;
        } else if ((second % 30) == 0) {
            // 30s刻み
            return SCALE_HEIGHT_MIDDLE;
        } else {
            // 6s刻み
            return SCALE_HEIGHT_LOW;
        }
    }

    /*
     * 時間テキスト描画X位置リストへのX位置追加
     */
    private void addTextPosXList(float xPos, int time) {

        // 時間テキストの表示時間間隔
        int timeTextJustTime;
        if (mScaleUnit == SCALE_UNIT_10_MIN) {
            // 10分刻みなら、10分毎
            timeTextJustTime = 10;
        } else {
            // 1分刻みなら、60秒毎
            timeTextJustTime = 60;
        }

        // 指定時間が時間テキストの表示時間の場合
        if ((time % timeTextJustTime) == 0) {
            // リストに位置を追加
            mGraghTextPosXList.add(xPos);
        }
    }

    /*
     * 記録時間からグラフの横幅を計算
     *   para1：hh:mm:ss
     */
    public int calcGraphWidthFromRecordTime() {

        //-----------------------
        // 記録時間に応じた横幅の計算
        //-----------------------
        // 記録時間分の横幅を取得
        float scaleLength = getScaleLengthFromTime( mRecordTotalMinute );

        // 記録時間に対して必要な横幅を計算
        int timeGraghWidth = (int) (scaleLength + TIME_TEXT_WIDTH);
        return timeGraghWidth;
    }

    /*
     * 記録時間の設定
     */
    public void setRecordTime( String recordTime ) {
        // 記録時間の設定
        mRecordTime = recordTime;
        // 記録時間を分として保持
        mRecordTotalMinute = getMinuteFromTime( recordTime );
    }

    /*
     * 記録時間の設定
     */
    public void setDefaultScaleUnit() {
        // デフォルトメモリ単位の設定
        mScaleUnit = getDefaultScaleUnit(mRecordTotalMinute);
    }

    /*
     * 記録メモリストの設定
     */
    public void setStampMemoList(ArrayList<StampMemoTable> stampMemos) {
        mStampMemos = stampMemos;
    }

    /*
     * 目盛り単位の設定
     */
    public void setScaleUnit(int unit ) {
        mScaleUnit = unit;
    }

    /*
     * 目盛り単位の取得
     */
    public int getScaleUnit() {
        return mScaleUnit;
    }

    /*
     * デフォルトの目盛り単位の取得
     */
    private int getDefaultScaleUnit(float minute ) {

        if( minute <= 10 ){
            return SCALE_UNIT_1_MIN;
        } else {
            return SCALE_UNIT_10_MIN;
        }
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
        // 時間テキストを大目盛り単位毎に描画
        //----------------------------
        // 進める時間
        int timeAdvance = get10ScaleTimeAdvance();

        // リスト最後の直前までの情報を描画
        last = mGraghTextPosXList.size() - 1;
        for (int i = 0, minute = 0; i < last; i++, minute += timeAdvance) {
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
        // 時間の目盛りの長さを取得
        float scaleLength = getScaleLengthFromTime( minuteSecond );

        // 座標：グラフ描画スタート位置 + 指定時間分の長さ
        return SCALE_START_POS_X + scaleLength;
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
        if ( !mGraghScalePath.isEmpty() ) {
            // 目盛り描画
            canvas.drawPath(mGraghScalePath, mGraghScalePaint);
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
        setGraghScalePath();
    }

}
