package com.memotool.timewatchmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.memotool.timewatchmemo.db.AppDatabase;
import com.memotool.timewatchmemo.db.AppDatabaseManager;
import com.memotool.timewatchmemo.db.StampMemoTableDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncRemoveStampMemo extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final int mStampMemoPid;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncRemoveStampMemo(Context context, int memoPid, OnFinishListener listener) {
        super(context);

        mDB = AppDatabaseManager.getInstance(context);
        mStampMemoPid = memoPid;
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
         * DBから削除
         */
        @SuppressLint("ResourceType")
        private void removeDB(){
            // 記録メモの削除
            StampMemoTableDao dao = mDB.daoStampMemoTable();
            dao.delete( mStampMemoPid );
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
        mOnFinishListener.onFinish( mStampMemoPid );
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        void onFinish( int pid );
    }



}
