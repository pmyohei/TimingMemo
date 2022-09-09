package com.example.timingmemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.timingmemo.common.AppCommonData;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;
import com.example.timingmemo.db.async.AsyncReadMemoCategory;
import com.example.timingmemo.ui.memo.MemoPageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MemoListActivity extends AppCompatActivity {

    private ArrayList<UserMemoTable> mUserMemos;
    private ArrayList<UserCategoryTable> mUserCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        // メモとカテゴリを取得
        boolean isGet = getUserData();
        if (!isGet) {
            // なければ、DBから取得
            getOnDB();
        }
    }

    /*
     * メモとカテゴリデータを取得
     */
    private boolean getUserData() {

        //-------------------
        // 共通データから取得
        //-------------------
        AppCommonData commonData = (AppCommonData) getApplication();
        mUserMemos = commonData.getUserMemos();
        mUserCategories = commonData.getUserCategories();

        // 保持済みであれば、終了
        if (mUserMemos != null) {
            // メモを一覧表示
            setMemoList();
            return true;
        }

        return false;
    }

    /*
     * DBからメモとカテゴリデータを取得
     */
    private void getOnDB() {

        //DB保存処理
        AsyncReadMemoCategory db = new AsyncReadMemoCategory(this, new AsyncReadMemoCategory.OnFinishListener() {
            @Override
            public void onFinish(ArrayList<UserMemoTable> memos, ArrayList<UserCategoryTable> categories) {

                // DBの情報を保持
                mUserMemos = memos;
                mUserCategories = categories;

                // 共通データ側も更新
                AppCommonData commonData = (AppCommonData) getApplication();
                commonData.setUserMemos(mUserMemos);
                commonData.setUserCategories(mUserCategories);

                // メモを一覧表示
                setMemoList();
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * メモの一覧表示設定
     */
    private void setMemoList() {

        //--------------------------
        // ViewPager2の設定
        //--------------------------
        // ページ毎（カテゴリ別）のメモリストを生成
        ArrayList<ArrayList<UserMemoTable>> memosByCategory = getMemosByCategoryList();

        // Pageアダプタを設定
        MemoPageAdapter memoPageAdapter = new MemoPageAdapter( memosByCategory );
        ViewPager2 vp2_memoList = findViewById(R.id.vp2_memoList);
        vp2_memoList.setAdapter(memoPageAdapter);

        //--------------------------
        // インジケータの設定
        //--------------------------
        // インジケータに表示する文字列リスト
        List<String> tabCategoryName = getCategoryNameInIndicator();

        // インジケータの設定
        TabLayout tab_category = findViewById(R.id.tab_category);
        new TabLayoutMediator(tab_category, vp2_memoList,
                (tab, position) -> tab.setText( tabCategoryName.get(position) )
        ).attach();
    }

    /*
     * インジケータとして表示するカテゴリ名を、文字列リストで取得
     */
    private List<String> getCategoryNameInIndicator() {

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
     * カテゴリ名を文字列リストとして取得
     */
    private ArrayList<ArrayList<UserMemoTable>> getMemosByCategoryList(){

        //-------------------------------------------------
        // カテゴリ別でメモを振り分けるため、カテゴリpidリストを用意
        //-------------------------------------------------
        List<Integer> categoryPids = new ArrayList<>();
        // リストの先頭はカテゴリなしの値を設定
        categoryPids.add( UserMemoTable.NO_CATEGORY );
        // カテゴリのpidをリストへ追加
        for( UserCategoryTable category: mUserCategories ){
            int pid = category.getPid();
            categoryPids.add( pid );
        }

        //-------------------------------------------------
        // カテゴリ別でメモを振り分け
        //-------------------------------------------------
        ArrayList<ArrayList<UserMemoTable>> memosByCategory = new ArrayList<>();

        // カテゴリ分繰り返し
        for( int categoryPid: categoryPids ){
            // 新規リストを追加
            ArrayList<UserMemoTable> memos = new ArrayList<>();

            // メモ数分繰り返し
            for( UserMemoTable memoTable: mUserMemos ){
                // カテゴリpidと「メモが割り当てられたカテゴリpid」が一致している場合、リストへメモを追加
                int memoPid = memoTable.getPid();
                if( categoryPid == memoPid ){
                    memos.add( memoTable );
                }
            }

            // あるカテゴリのメモリストを追加
            memosByCategory.add( memos );
        }

        return memosByCategory;
    }

}