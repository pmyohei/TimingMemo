package com.example.timingmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.timingmemo.db.AppDatabase;
import com.example.timingmemo.db.AppDatabaseManager;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserCategoryTableDao;
import com.example.timingmemo.db.UserMemoTable;
import com.example.timingmemo.db.UserMemoTableDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTmpCreateDataMemo extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncTmpCreateDataMemo(Context context, OnFinishListener listener) {
        super(context);

        mDB               = AppDatabaseManager.getInstance(context);
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

            //仮の保存処理 DB作成用
            UserMemoTableDao memoDao = mDB.daoUserMemoTable();
            UserCategoryTableDao categoryDao = mDB.daoUserCategoryTable();

/*
            memoDao.deleteAll();
            categoryDao.deleteAll();
*/

            if( memoDao.getAll().size() > 0 ){
                return;
            }

            UserMemoTable memo = new UserMemoTable();
            UserMemoTable memo2 = new UserMemoTable();
            UserMemoTable memo3 = new UserMemoTable();
            UserCategoryTable category = new UserCategoryTable();

            memo.setName( "Favorite" );
            memo2.setName( "important" );
            memo3.setName( "Test3" );
            category.setName( "Movie" );

            memoDao.insert( memo );
            memoDao.insert( memo2 );
            memoDao.insert( memo3 );
            categoryDao.insert( category );
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

        Log.i("DB確認", "onPostExecute");

        //完了
//        mOnFinishListener.onFinish(mMapPid);
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( int pid );
    }



}
