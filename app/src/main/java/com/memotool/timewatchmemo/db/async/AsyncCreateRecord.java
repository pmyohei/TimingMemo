package com.memotool.timewatchmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.memotool.timewatchmemo.db.AppDatabase;
import com.memotool.timewatchmemo.db.AppDatabaseManager;
import com.memotool.timewatchmemo.db.RecordTable;
import com.memotool.timewatchmemo.db.RecordTableDao;
import com.memotool.timewatchmemo.db.StampMemoTable;
import com.memotool.timewatchmemo.db.StampMemoTableDao;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncCreateRecord extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final RecordTable mRecord;
    private final ArrayList<StampMemoTable> mStampMemos;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncCreateRecord(Context context, RecordTable record, ArrayList<StampMemoTable> stampMemos, OnFinishListener listener) {
        super(context);

        mDB = AppDatabaseManager.getInstance(context);
        mRecord = record;
        mStampMemos = stampMemos;
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
            insertDB();

            //後処理
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute();
                }
            });
        }

        /*
         * DBへ保存
         */
        @SuppressLint("ResourceType")
        private void insertDB(){

            RecordTableDao recordTableDao = mDB.daoRecordTable();
            StampMemoTableDao stampMemoTableDao = mDB.daoStampMemoTable();

            //---------------------
            // 記録の保存
            //---------------------
            int recordPid = (int)recordTableDao.insert( mRecord );

            //---------------------
            // 記録メモの保存
            //---------------------
            // 記録Pidの設定
            for( StampMemoTable stampMemo: mStampMemos ){
                stampMemo.setRecordPid( recordPid );

                // テーブルに保存
                stampMemoTableDao.insert( stampMemo );
            }
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
        mOnFinishListener.onFinish();
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish();
    }



}
