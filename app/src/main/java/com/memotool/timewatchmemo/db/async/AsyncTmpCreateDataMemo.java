package com.memotool.timewatchmemo.db.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.memotool.timewatchmemo.db.AppDatabase;
import com.memotool.timewatchmemo.db.AppDatabaseManager;
import com.memotool.timewatchmemo.db.RecordTable;
import com.memotool.timewatchmemo.db.RecordTableDao;
import com.memotool.timewatchmemo.db.StampMemoTable;
import com.memotool.timewatchmemo.db.StampMemoTableDao;
import com.memotool.timewatchmemo.db.UserCategoryTable;
import com.memotool.timewatchmemo.db.UserCategoryTableDao;
import com.memotool.timewatchmemo.db.UserMemoTable;
import com.memotool.timewatchmemo.db.UserMemoTableDao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        private void insertDB() {

            //---------------------------
            // 疑似メモとカテゴリを作成
            //---------------------------
            createMemoCategory();

            //---------------------------
            // 疑似記録データを作成
            //---------------------------
            createRecordData();

        }


        private void createMemoCategory() {

            //仮の保存処理 DB作成用
            UserMemoTableDao memoDao = mDB.daoUserMemoTable();
            UserCategoryTableDao categoryDao = mDB.daoUserCategoryTable();

/*
            memoDao.deleteAll();
            categoryDao.deleteAll();
*/

            if (memoDao.getAll().size() > 0) {
                return;
            }

            //---------------------------
            // 疑似メモとカテゴリを作成
            //---------------------------
            UserMemoTable memo = new UserMemoTable();
            UserMemoTable memo2 = new UserMemoTable();
            UserMemoTable memo3 = new UserMemoTable();
            UserCategoryTable category = new UserCategoryTable();

            memo.setName("Favorite");
            memo2.setName("important");
            memo3.setName("Test3");
            memo.setColor( 0xffc41a30 );
            memo2.setColor( 0xffFFAC84 );
            memo3.setColor( 0xffFCE45C );

            category.setName("Movie");

            memoDao.insert(memo);
            memoDao.insert(memo2);
            memoDao.insert(memo3);
            categoryDao.insert(category);

        }

        private void createRecordData(){

            RecordTableDao recordTableDao = mDB.daoRecordTable();
            StampMemoTableDao stampMemoTableDao = mDB.daoStampMemoTable();

            recordTableDao.deleteAll();
            stampMemoTableDao.deleteAll();

            // システム時間の取得
            @SuppressLint("SimpleDateFormat") final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            final Date dateStart = new Date( System.currentTimeMillis() - ( 1000 * 60 * 20 ) );     //20min前
            final Date dateEnd = new Date( System.currentTimeMillis() );
            String startTime = df.format(dateStart);
            String endTime = df.format(dateEnd);

            // レコード保存
            RecordTable recordTable = new RecordTable();
            recordTable.setName( "Record 1" );
            recordTable.setStartRecordingTime( startTime );
            recordTable.setEndRecordingTime( endTime );
            recordTable.setRecordingTime( "00:20:00" );

            int rPid = (int)recordTableDao.insert( recordTable );

            // 打刻メモの保存
            StampMemoTable stamp1 = new StampMemoTable();
            StampMemoTable stamp2 = new StampMemoTable();
            StampMemoTable stamp3 = new StampMemoTable();

            stamp1.setRecordPid( rPid );
            stamp1.setMemoName( "Memo1" );
            stamp1.setMemoColor( 0xFFc41a30 );
            stamp1.setDelayTime( "00:05" );
            stamp1.setStampingPlayTime( "00:02:12" );
            stamp1.setStampingSystemTime( startTime );

            stamp2.setRecordPid( rPid );
            stamp2.setMemoName( "Memo2" );
            stamp2.setMemoColor( 0xFFFCE45C );
            stamp2.setDelayTime( "00:23" );
            stamp2.setStampingPlayTime( "00:08:01" );
            stamp2.setStampingSystemTime( startTime );

            stamp3.setRecordPid( rPid );
            stamp3.setMemoName( "Memo3" );
            stamp3.setMemoColor( 0xFF494544 );
            stamp3.setDelayTime( "01:05" );
            stamp3.setStampingPlayTime( "00:18:31" );
            stamp3.setStampingSystemTime( startTime );

            stampMemoTableDao.insert( stamp1 );
            stampMemoTableDao.insert( stamp2 );
            stampMemoTableDao.insert( stamp3 );

            // レコード保存
            RecordTable recordTable2 = new RecordTable();
            recordTable2.setName( "Record 1234567890123456789012345678901234567890" );
            recordTable2.setStartRecordingTime( startTime );
            recordTable2.setEndRecordingTime( endTime );
            recordTable2.setRecordingTime( "01:12:40" );

            recordTableDao.insert( recordTable2 );

            // レコード保存
            RecordTable recordTable3 = new RecordTable();
            recordTable3.setName( "Record short" );
            recordTable3.setStartRecordingTime( startTime );
            recordTable3.setEndRecordingTime( endTime );
            recordTable3.setRecordingTime( "00:01:30" );

            recordTableDao.insert( recordTable3 );

            // レコード保存
            RecordTable recordTable4 = new RecordTable();
            recordTable4.setName( "Record short2" );
            recordTable4.setStartRecordingTime( startTime );
            recordTable4.setEndRecordingTime( endTime );
            recordTable4.setRecordingTime( "00:04:59" );

            recordTableDao.insert( recordTable4 );

            // レコード保存
            RecordTable recordTable5 = new RecordTable();
            recordTable5.setName( "Record short3" );
            recordTable5.setStartRecordingTime( startTime );
            recordTable5.setEndRecordingTime( endTime );
            recordTable5.setRecordingTime( "00:09:59" );

            recordTableDao.insert( recordTable5 );

            // レコード保存
            RecordTable recordTable6 = new RecordTable();
            recordTable6.setName( "Record short3" );
            recordTable6.setStartRecordingTime( startTime );
            recordTable6.setEndRecordingTime( endTime );
            recordTable6.setRecordingTime( "00:00:30" );

            recordTableDao.insert( recordTable6 );

            // レコード保存
            RecordTable recordTable7 = new RecordTable();
            recordTable7.setName( "Record short3" );
            recordTable7.setStartRecordingTime( startTime );
            recordTable7.setEndRecordingTime( endTime );
            recordTable7.setRecordingTime( "00:07:30" );

            recordTableDao.insert( recordTable7 );
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
