package com.memotool.timewatchmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.memotool.timewatchmemo.db.AppDatabase;
import com.memotool.timewatchmemo.db.AppDatabaseManager;
import com.memotool.timewatchmemo.db.UserCategoryTable;
import com.memotool.timewatchmemo.db.UserCategoryTableDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUpdateCategory extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final UserCategoryTable mUserCategory;          // 更新先のデータを保持する
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncUpdateCategory(Context context, UserCategoryTable category, OnFinishListener listener) {
        super(context);

        mDB = AppDatabaseManager.getInstance(context);
        mUserCategory = category;
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
            int pid = mUserCategory.getPid();
            String categoryName = mUserCategory.getName();

            // 更新対象のメモをテーブルから取得し、データを更新
            UserCategoryTableDao categoryDao = mDB.daoUserCategoryTable();
            UserCategoryTable targetCategory = categoryDao.getUserCategory( pid );
            targetCategory.setName( categoryName );

            // 更新
            categoryDao.update( targetCategory );
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

        mOnFinishListener.onFinish( mUserCategory );
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( UserCategoryTable category );
    }



}
