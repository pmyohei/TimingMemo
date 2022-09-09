package com.example.timingmemo.common;

import android.app.Application;

import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;

import java.util.ArrayList;

public class AppCommonData extends Application {

    private ArrayList<UserMemoTable> mUserMemos;
    private ArrayList<UserCategoryTable> mUserCategories;
    private ArrayList<RecordTable> mRecords;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        mUserMemos = null;
        mUserCategories = null;
        mRecords = null;
    }

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
}
