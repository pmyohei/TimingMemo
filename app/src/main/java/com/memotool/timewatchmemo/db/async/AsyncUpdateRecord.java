package com.memotool.timewatchmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.memotool.timewatchmemo.db.AppDatabase;
import com.memotool.timewatchmemo.db.AppDatabaseManager;
import com.memotool.timewatchmemo.db.RecordTableDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUpdateRecord extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final int mRecordPid;
    private final String mRecordName;
    private final String mRecordTime;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncUpdateRecord(Context context, int recordPid, String recordName, String recordTime, OnFinishListener listener) {
        super(context);

        mDB = AppDatabaseManager.getInstance(context);
        mRecordPid = recordPid;
        mRecordName = recordName;
        mRecordTime = recordTime;
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
         * DB更新処理
         */
        @SuppressLint("ResourceType")
        private void updateDB(){
            // 記録更新
            RecordTableDao dao = mDB.daoRecordTable();
            dao.updateRecordNameTime( mRecordPid, mRecordName, mRecordTime);
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

        mOnFinishListener.onFinish( mRecordPid, mRecordName, mRecordTime );
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( int pid, String updatedRecordName, String updatedRecordTime );
    }



}
