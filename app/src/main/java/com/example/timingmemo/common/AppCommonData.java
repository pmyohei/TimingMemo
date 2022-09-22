package com.example.timingmemo.common;

import android.app.Application;

import com.example.timingmemo.R;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;

import java.util.ArrayList;
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
}
