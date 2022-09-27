package com.example.timingmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.timingmemo.R;
import com.example.timingmemo.db.AppDatabase;
import com.example.timingmemo.db.AppDatabaseManager;
import com.example.timingmemo.db.StampMemoTable;
import com.example.timingmemo.db.StampMemoTableDao;
import com.example.timingmemo.db.UserMemoTable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUpdateStampMemo extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final Context mContext;
    private final StampMemoTable mStampMemo;
    private StampMemoTable mUpdatedStampMemo;
    private boolean mIsChangedPlayTime;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncUpdateStampMemo(Context context, StampMemoTable stampMemo, OnFinishListener listener) {
        super(context);

        mContext = context;
        mDB = AppDatabaseManager.getInstance(context);
        mStampMemo = stampMemo;
        mOnFinishListener = listener;
    }

    /*
     * 非同期処理
     */
    private class AsyncRunnable implements Runnable {

        Handler handler = new Handler(Looper.getMainLooper());

        /*
         * バックグラウンド処理
         */
        @Override
        public void run() {

            //メイン処理
            updateDB();

            //後処理
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute();
                }
            });
        }

        /*
         * DBへ更新
         */
        @SuppressLint("ResourceType")
        private void updateDB(){

            StampMemoTableDao dao = mDB.daoStampMemoTable();

            //----------------------
            // 更新情報を保持
            //----------------------
            int targetPid = mStampMemo.getPid();
            String updatedMemoName = mStampMemo.getMemoName();
            int updatedMemoColor = mStampMemo.getMemoColor();
            String updatedPlayTime = mStampMemo.getStampingPlayTime();

            //----------------------
            // 更新対象の記録メモを更新
            //----------------------
            StampMemoTable targetStampMemo = dao.getStampMemo( targetPid );

            // メモ名とメモ色はそのまま上書き
            targetStampMemo.setMemoName( updatedMemoName );
            targetStampMemo.setMemoColor( updatedMemoColor );

            // 更新前の記録時間を保持し、記録時間を上書き
            String prePlayTime = targetStampMemo.getStampingPlayTime();
            targetStampMemo.setStampingPlayTime( updatedPlayTime );

            // 記録時間の変更有無：変更ありの場合true
            mIsChangedPlayTime = !( prePlayTime.equals( updatedPlayTime ) );

            // 遅延時間と記録時間（システム時間）は、記録時間に変更がある場合更新
            if( mIsChangedPlayTime ){

                // 遅延時間 → クリア
                String clearDelayTime = mContext.getString( R.string.clear_delay_time );
                // 記録時間（システム） → 現在時刻に更新
                @SuppressLint("SimpleDateFormat") final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                final Date currentDate = new Date( System.currentTimeMillis());
                String currentTime = df.format(currentDate);

                targetStampMemo.setDelayTime( clearDelayTime );
                targetStampMemo.setStampingSystemTime( currentTime );
            }

            // テーブル更新
            dao.update( targetStampMemo );

            // 更新後情報として保持
            mUpdatedStampMemo = targetStampMemo;
        }
    }

    /*
     * バックグラウンド前処理
     */
    void onPreExecute() {
        super.onPreExecute();
    }

    /*
     * 実行
     */
    public void execute() {
        //バックグランド前処理
        onPreExecute();
        //シングルスレッド（キューなし）で動作するexecutorを作成
        ExecutorService executorService  = Executors.newSingleThreadExecutor();
        //非同期処理を送信
        executorService.submit(new AsyncRunnable());
    }

    /*
     * バックグランド処理終了後の処理
     */
    void onPostExecute() {
        super.onPostExecute();

        mOnFinishListener.onFinish( mUpdatedStampMemo, mIsChangedPlayTime );
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( StampMemoTable stampMemo, boolean isChangedPlayTime );
    }
}
