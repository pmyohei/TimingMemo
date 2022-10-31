package com.example.timingmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.timingmemo.R;
import com.example.timingmemo.db.AppDatabase;
import com.example.timingmemo.db.AppDatabaseManager;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.RecordTableDao;
import com.example.timingmemo.db.StampMemoTable;
import com.example.timingmemo.db.StampMemoTableDao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncCreateStampMemo extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final Context mContext;
    private final StampMemoTable mStampMemo;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncCreateStampMemo(Context context, StampMemoTable stampMemo, OnFinishListener listener) {
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

            // メイン処理
            insertDB();

            // 後処理
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute();
                }
            });
        }

        /*
         * DBへ追加
         */
        @SuppressLint("ResourceType")
        private void insertDB() {

            //--------------------------
            // 新規記録メモ情報に情報追加
            //--------------------------
            // 遅延時間 → クリア
            String clearDelayTime = mContext.getString(R.string.clear_delay_time);

            // 記録時間（システム） → 現在時刻
            @SuppressLint("SimpleDateFormat") final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            final Date currentDate = new Date(System.currentTimeMillis());
            String currentTime = df.format(currentDate);

            mStampMemo.setDelayTime(clearDelayTime);
            mStampMemo.setStampingSystemTime(currentTime);

            // テーブルに追加
            StampMemoTableDao dao = mDB.daoStampMemoTable();
            int pid = (int) dao.insert(mStampMemo);

            // 終了リスナー用にPidを設定
            mStampMemo.setPid(pid);
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

        mOnFinishListener.onFinish( mStampMemo );
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( StampMemoTable stampMemo );
    }
}
