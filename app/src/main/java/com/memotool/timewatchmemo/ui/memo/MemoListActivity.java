package com.memotool.timewatchmemo.ui.memo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.memotool.timewatchmemo.R;
import com.memotool.timewatchmemo.common.AppCommonData;
import com.memotool.timewatchmemo.db.UserCategoryTable;
import com.memotool.timewatchmemo.db.UserMemoTable;
import com.memotool.timewatchmemo.db.async.AsyncReadMemoCategory;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MemoListActivity extends AppCompatActivity implements MemoListAdapter.MemoClickListener {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    public static final String KEY_NEW_MEMO_REGISTRATION = "new_memo_registration";
    public static final String KEY_SELECTED_PAGE_INDEX = "selected_page_index";
    public static final String KEY_MEMO_PID = "memo_pid";
    public static final String KEY_MEMO_NAME = "memo_name";
    public static final String KEY_MEMO_COLOR = "memo_color";

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private final int TOP_PAGE_INDEX = 0;

    private ArrayList<UserMemoTable> mUserMemos;
    private ArrayList<UserCategoryTable> mUserCategories;
    private ActivityResultLauncher<Intent> mMemoRegistrationLancher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        // 画面遷移ランチャーの生成
        setMemoRegistrationLancher();

        // メモとカテゴリを取得
        boolean isGet = getUserData();
        if ( isGet ) {
            // メモを一覧表示
            setMemoList( TOP_PAGE_INDEX );
        } else {
            // なければ、DBから取得
            getOnDB( TOP_PAGE_INDEX );
        }

        // ツールバーの設定
        setToolbar();
    }

    /*
     * 画面遷移ランチャーを生成
     */
    private void setMemoRegistrationLancher() {

        mMemoRegistrationLancher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        // ResultCodeの取得
                        int resultCode = result.getResultCode();
                        if( resultCode != MemoRegistrationActivity.RESULT_MEMO_REGISTRAION) {
                            // 該当外ならなにもせず終了
                            return;
                        }

                        // DBからデータを取得して画面情報を再度表示
                        Intent intent = result.getData();
                        int selectedPage = intent.getIntExtra( MemoRegistrationActivity.KEY_UPDATED_PAGE_INDEX, TOP_PAGE_INDEX );
                        getOnDB( selectedPage );
                    }
                });
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

        // 保持済みであれば
        if (mUserMemos != null) {
            return true;
        }

        return false;
    }

    /*
     * DBからメモとカテゴリデータを取得
     */
    private void getOnDB( int selectedPage ) {

        // DB保存処理
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
                setMemoList( selectedPage );
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * メモの一覧表示設定
     */
    private void setMemoList( int selectedPage ) {

        //--------------------------
        // ViewPager2の設定
        //--------------------------
        // ページ毎（カテゴリ別）のメモリストを生成
        ArrayList<ArrayList<UserMemoTable>> memosByCategory = MemoPageAdapter.getMemosByCategoryList( mUserCategories, mUserMemos );

        // Pageアダプタを設定
        MemoPageAdapter memoPageAdapter = new MemoPageAdapter(memosByCategory);
        ViewPager2 vp2_memoList = findViewById(R.id.vp2_memoList);
        vp2_memoList.setAdapter(memoPageAdapter);

        // 指定されたページを選択中にする
        vp2_memoList.setCurrentItem( selectedPage );

        // メモクリックリスナーの設定
        memoPageAdapter.setOnMemoClickListener( this );

        //--------------------------
        // インジケータの設定
        //--------------------------
        // インジケータに表示する文字列リスト
        AppCommonData commonData = (AppCommonData) getApplication();
        List<String> tabCategoryName = commonData.getCategoryNameList();

        // インジケータの設定
        TabLayout tab_category = findViewById(R.id.tab_category);
        new TabLayoutMediator(tab_category, vp2_memoList,
                (tab, position) -> tab.setText(tabCategoryName.get(position))
        ).attach();
    }

    /*
     * ツールバーの設定
     */
    private void setToolbar() {

        // ツールバータイトル
        String title = getString( R.string.toolbar_title_memo_list );

        // ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_memoList);
        toolbar.setTitle( title );
        setSupportActionBar(toolbar);

        // 戻るボタンの表示
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull( actionBar ).setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /*
     * 画面遷移 - 新規メモ作成画面
     */
    private void transitionNewMemo() {

        // 開いているカテゴリページ位置を取得
        ViewPager2 vp2_memoList = findViewById(R.id.vp2_memoList);
        int selectedCategoryPage = vp2_memoList.getCurrentItem();

        // 画面遷移
        Intent intent = new Intent(this, MemoRegistrationActivity.class);
        intent.putExtra( KEY_NEW_MEMO_REGISTRATION, true );
        intent.putExtra( KEY_SELECTED_PAGE_INDEX, selectedCategoryPage );

        mMemoRegistrationLancher.launch( intent );
    }

    /*
     * 画面遷移 - メモ編集／削除画面
     */
    private void transitionUpdateMemo( UserMemoTable memo) {

        // 開いているカテゴリページ位置を取得
        ViewPager2 vp2_memoList = findViewById(R.id.vp2_memoList);
        int selectedCategoryPage = vp2_memoList.getCurrentItem();

        // 画面遷移
        Intent intent = new Intent(this, MemoRegistrationActivity.class);
        intent.putExtra( KEY_NEW_MEMO_REGISTRATION, false );
        intent.putExtra( KEY_SELECTED_PAGE_INDEX, selectedCategoryPage );
        intent.putExtra( KEY_MEMO_PID, memo.getPid() );
        intent.putExtra( KEY_MEMO_NAME, memo.getName() );
        intent.putExtra( KEY_MEMO_COLOR, memo.getColor() );

        mMemoRegistrationLancher.launch( intent );
    }



    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //初期メニュー
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_add, menu);

        return true;
    }

    /*
     * ツールバー 戻るボタン押下処理
     */
    @Override
    public boolean onSupportNavigateUp() {
        //アクティビティ終了
        finish();

        return super.onSupportNavigateUp();
    }

    /*
     * ツールバーアクション選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new:
                // メモの新規作成
                transitionNewMemo();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
     * 本メソッドは、各メモクリック時処理として実装
     */
    @Override
    public void onMemoClick(UserMemoTable userMemo ) {
//        Log.i("メモクリック", "メモ名=" + userMemo.getName());
        // メモ更新画面へ遷移
        transitionUpdateMemo( userMemo );
    }


}