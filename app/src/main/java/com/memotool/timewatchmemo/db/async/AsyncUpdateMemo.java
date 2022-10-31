package com.memotool.timewatchmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.memotool.timewatchmemo.db.AppDatabase;
import com.memotool.timewatchmemo.db.AppDatabaseManager;
import com.memotool.timewatchmemo.db.UserMemoTable;
import com.memotool.timewatchmemo.db.UserMemoTableDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUpdateMemo extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final UserMemoTable mUserMemo;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncUpdateMemo(Context context, UserMemoTable memo, OnFinishListener listener) {
        super(context);

        mDB = AppDatabaseManager.getInstance(context);
        mUserMemo = memo;
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

            // 更新対象と更新後情報
            int pid = mUserMemo.getPid();
            String memoName = mUserMemo.getName();
            int memoColor = mUserMemo.getColor();
            int categoryPid = mUserMemo.getCategoryPid();

            // メモの更新
            UserMemoTableDao memoDao = mDB.daoUserMemoTable();

            // 更新対象のメモをテーブルから取得し、データを更新
            UserMemoTable targetMemo = memoDao.getUserMemo( pid );
            targetMemo.setCategoryPid( categoryPid );
            targetMemo.setName( memoName );
            targetMemo.setColor( memoColor );

            // 更新
            memoDao.update( targetMemo );
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

        mOnFinishListener.onFinish( 0 );
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( int pid );
    }



}
