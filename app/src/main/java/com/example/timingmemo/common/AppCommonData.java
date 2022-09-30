package com.example.timingmemo.common;

import android.app.Application;

import com.example.timingmemo.R;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.StampMemoTable;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;
import com.example.timingmemo.ui.record.RecordFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppCommonData extends Application {

    //--------------------------------
    // アプリ間共通定数
    //--------------------------------
    public static final String TIME_FORMAT_DELIMITER = ":";

    //--------------------------------
    // アプリ間共通変数
    //--------------------------------
    private ArrayList<UserMemoTable> mUserMemos;
    private ArrayList<UserCategoryTable> mUserCategories;
    private ArrayList<RecordTable> mRecords;

    //--------------------------------
    // アプリ間共通変数 - 記録中継続情報
    //--------------------------------
    private RecordTable mRecord;
    private ArrayList<StampMemoTable> mStampMemos;
    private int mRecordPlayState;


    @Override
    public void onCreate() {
        super.onCreate();

        mStampMemos = new ArrayList<>();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        mUserMemos = null;
        mUserCategories = null;
        mRecords = null;
        mStampMemos = null;
    }


    //---------------------
    // 共通メソッド
    //---------------------
    /*
     * 現在登録されているカテゴリ名を文字列リストで取得する。
     * 　なお、先頭文字列は「カテゴリなし」を意味する文言を固定で設定する。
     */
    public List<String> getCategoryNameList() {

        List<String> names = new ArrayList<>();

        // 1つ目は「カテゴリなし」の文字列を固定で設定
        String noCategory = getResources().getString(R.string.no_category);
        names.add(noCategory);

        // 2つ目以降は、ユーザーが登録したカテゴリ名をリストに設定
        for (UserCategoryTable category : mUserCategories) {
            names.add(category.getName());
        }

        return names;
    }

    /*
     * 記録情報のクリア
     */
    public void clearRecordData(){
        // 記録
        String startDate = getNowDate();
        mRecord = new RecordTable();
        mRecord.setStartRecordingTime( startDate );
        // 記録メモをクリア
        mStampMemos.clear();
        // 記録状態
        mRecordPlayState = RecordFragment.RECORD_PLAY;
    }

    /*
     * 現在日時をyyyy/MM/dd HH:mm:ss形式で取得する
     */
    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    //---------------------
    // getter / setter
    //---------------------
    public ArrayList<UserMemoTable> getUserMemos() {
        return mUserMemos;
    }
    public void setUserMemos(ArrayList<UserMemoTable> userMemos) {
        this.mUserMemos = userMemos;
    }

    public ArrayList<UserCategoryTable> getUserCategories() {
        return mUserCategories;
    }
    public void setUserCategories(ArrayList<UserCategoryTable> userCategories) {
        this.mUserCategories = userCategories;
    }

    public ArrayList<RecordTable> getRecords() {
        return mRecords;
    }
    public void setRecords(ArrayList<RecordTable> records) {
        this.mRecords = records;
    }


    public RecordTable getRecord() {
        return mRecord;
    }
    public void setRecord(RecordTable record) {
        mRecord = record;
    }

    public ArrayList<StampMemoTable> getStampMemos() {
        return mStampMemos;
    }
    public void setStampMemos(ArrayList<StampMemoTable> stampMemos) {
        mStampMemos = stampMemos;
    }

    public int getRecordPlayState() {
        return mRecordPlayState;
    }
    public void setRecordPlayState(int recordPlayState) {
        mRecordPlayState = recordPlayState;
    }
}
