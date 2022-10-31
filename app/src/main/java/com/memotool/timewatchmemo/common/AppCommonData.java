package com.memotool.timewatchmemo.common;

import android.app.Application;

import com.memotool.timewatchmemo.R;
import com.memotool.timewatchmemo.db.RecordTable;
import com.memotool.timewatchmemo.db.StampMemoTable;
import com.memotool.timewatchmemo.db.UserCategoryTable;
import com.memotool.timewatchmemo.db.UserMemoTable;
import com.memotool.timewatchmemo.ui.record.RecordFragment;

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
    private long mRecordStartSystemTime;
    private long mRecordPauseSystemTime;
    private String mRecordTime;
    private String mDelayTime;
    private boolean mIsRenewRecordStartTime;


    @Override
    public void onCreate() {
        super.onCreate();

        mStampMemos = new ArrayList<>();
        mRecordPlayState = RecordFragment.RECORD_STOP;
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
     * 記録情報の一時保存処理
     */
    public void tmpSaveRecordData( RecordTable record, int state, ArrayList<StampMemoTable> stampMemos,
                                   long startTime, long pauseTime, String recordTime, String delayTime, boolean isRenewRecordStartTime ){
        // 記録情報を一時保持
        mRecord = record;
        mRecordPlayState = state;
        mStampMemos = stampMemos;
        mRecordStartSystemTime = startTime;
        mRecordPauseSystemTime = pauseTime;
        mRecordTime = recordTime;
        mDelayTime = delayTime;
        mIsRenewRecordStartTime = isRenewRecordStartTime;
    }

    /*
     * 現在日時をyyyy/MM/dd HH:mm:ss形式で取得する
     */
    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    //-------------------------------------
    // getter / setter
    //-------------------------------------
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


    public long getRecordStartSystemTime() {
        return mRecordStartSystemTime;
    }
    public void setRecordStartSystemTime(long recordStartSystemTime) {
        mRecordStartSystemTime = recordStartSystemTime;
    }

    public long getRecordPauseSystemTime() {
        return mRecordPauseSystemTime;
    }
    public void setRecordPauseSystemTime(long recordPauseSystemTime) {
        mRecordPauseSystemTime = recordPauseSystemTime;
    }

    public String getRecordTime() {
        return mRecordTime;
    }

    public String getDelayTime() {
        return mDelayTime;
    }

    public boolean isRenewRecordStartTime() {
        return mIsRenewRecordStartTime;
    }
}
