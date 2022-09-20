package com.example.timingmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.timingmemo.db.AppDatabase;
import com.example.timingmemo.db.AppDatabaseManager;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.RecordTableDao;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserCategoryTableDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncReadRecordCategory extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final OnFinishListener mOnFinishListener;
    private ArrayList<RecordTable> mRecords;


    /*
     * コンストラクタ
     */
    public AsyncReadRecordCategory(Context context, OnFinishListener listener) {
        super(context);

        mDB = AppDatabaseManager.getInstance(context);
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
            readDB();

            //後処理
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute();
                }
            });
        }

        /*
         * DB読み込み
         */
        @SuppressLint("ResourceType")
        private void readDB(){
            // 記録を全て取得
            RecordTableDao dao = mDB.daoRecordTable();
            List<RecordTable> records = dao.getAll();
            mRecords = new ArrayList<>( records );
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
        //完了リスナー処理
        mOnFinishListener.onFinish(mRecords);
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( ArrayList<RecordTable> records );
    }

}