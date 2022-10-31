package com.memotool.timewatchmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.memotool.timewatchmemo.db.AppDatabase;
import com.memotool.timewatchmemo.db.AppDatabaseManager;
import com.memotool.timewatchmemo.db.UserCategoryTableDao;
import com.memotool.timewatchmemo.db.UserMemoTable;
import com.memotool.timewatchmemo.db.UserMemoTableDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncRemoveCategory extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final int mUserCategoryPid;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncRemoveCategory(Context context, int categoryPid, OnFinishListener listener) {
        super(context);

        mDB = AppDatabaseManager.getInstance(context);
        mUserCategoryPid = categoryPid;
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
            removeDB();

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
        private void removeDB(){

            //--------------
            // カテゴリ削除
            //--------------
            UserCategoryTableDao categoryDao = mDB.daoUserCategoryTable();
            categoryDao.delete( mUserCategoryPid );

            //---------------------------------------
            // メモの所属カテゴリを「カテゴリなし」に更新
            //---------------------------------------
            // 該当のメモを取得
            UserMemoTableDao memoDao = mDB.daoUserMemoTable();
            List<UserMemoTable> memos = memoDao.getUserMemosBelongCategory( mUserCategoryPid );

            // 対象のメモのカテゴリを「カテゴリなし」に更新
            for( UserMemoTable memo: memos ){
                memo.setCategoryPid( UserMemoTable.NO_CATEGORY );
                memoDao.update( memo );
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
        mOnFinishListener.onFinish( mUserCategoryPid );
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( int pid );
    }
}
