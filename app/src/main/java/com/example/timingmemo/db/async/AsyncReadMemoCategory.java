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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncReadMemoCategory extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final OnFinishListener mOnFinishListener;
    private ArrayList<UserMemoTable> mUserMemos;
    private ArrayList<UserCategoryTable> mUserCategories;


    /*
     * コンストラクタ
     */
    public AsyncReadMemoCategory(Context context, OnFinishListener listener) {
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

            // メモとカテゴリを全て取得
            UserMemoTableDao memoDao = mDB.daoUserMemoTable();
            UserCategoryTableDao categoryDao = mDB.daoUserCategoryTable();

            List<UserMemoTable> memos = memoDao.getAll();
            mUserMemos = new ArrayList<>(memos);
            List<UserCategoryTable> categories = categoryDao.getAll();
            mUserCategories = new ArrayList<>(categories);
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
        mOnFinishListener.onFinish( mUserMemos, mUserCategories );
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( ArrayList<UserMemoTable> memos, ArrayList<UserCategoryTable> categories );
    }

}
