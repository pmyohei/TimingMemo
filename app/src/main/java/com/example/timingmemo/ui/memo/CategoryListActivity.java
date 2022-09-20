package com.example.timingmemo.ui.memo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.async.AsyncReadCategory;

import java.util.ArrayList;
import java.util.Objects;

public class CategoryListActivity extends AppCompatActivity implements CategoryListAdapter.CategoryClickListener {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    public static final String KEY_NEW_CATEGORY_REGISTRATION = "new_category_registration";
    public static final String KEY_CATEGORY_PID = "category_pid";
    public static final String KEY_CATEGORY_NAME = "category_name";

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private ArrayList<UserCategoryTable> mUserCategories;
    private ActivityResultLauncher<Intent> mCategoryRegistrationLancher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        // 画面遷移ランチャーの生成
        setCategoryRegistrationLancher();

        // メモとカテゴリを取得
        boolean isGet = getUserData();
        if (isGet) {
            // カテゴリを一覧表示
            setCategoryList();
        } else {
            // なければ、DBから取得
            getOnDB();
        }

        // ツールバーの設定
        setToolbar();
    }

    /*
     * ツールバーの設定
     */
    private void setToolbar() {

        // ツールバータイトル
        String title = getString( R.string.toolbar_title_category_list );

        // ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_categoryList);
        toolbar.setTitle( title );
        setSupportActionBar(toolbar);

        // 戻るボタンの表示
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /*
     * メモとカテゴリデータを取得
     */
    private void setCategoryRegistrationLancher() {

        mCategoryRegistrationLancher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    // ResultCodeの取得
                    int resultCode = result.getResultCode();
                    if( resultCode == Activity.RESULT_CANCELED ){
                        // 戻るボタンでの終了なら何もしない
                        return;
                    }

                    // 更新通知対象のリスト位置を取得
                    Intent intent = result.getData();
                    int position = intent.getIntExtra( CategoryRegistrationActivity.KEY_UPDATED_POSITION, -1 );
                    if( position == -1 ){
                        // フェイルセーフ
                        return;
                    }

                    // アダプタへ更新通知
                    RecyclerView rv_categoryList = findViewById(R.id.rv_categoryList);
                    CategoryListAdapter adapter = (CategoryListAdapter)rv_categoryList.getAdapter();

                    // 操作に応じた通知処理
                    switch ( resultCode ){
                        case CategoryRegistrationActivity.RESULT_CATEGORY_NEW:
                            adapter.notifyItemInserted( position );
                            break;

                        case CategoryRegistrationActivity.RESULT_CATEGORY_UPDATE:
                            adapter.notifyItemChanged( position );
                            break;

                        case CategoryRegistrationActivity.RESULT_CATEGORY_REMOVE:
                            adapter.notifyItemRemoved( position );
                            break;
                    }

                    Log.i("positionテスト", "戻り=" + position);
                }
            });
    }

    /*
     * とカテゴリデータを取得
     */
    private boolean getUserData() {

        //-------------------
        // 共通データから取得
        //-------------------
        AppCommonData commonData = (AppCommonData) getApplication();
        mUserCategories = commonData.getUserCategories();

        // 保持済みであれば
        if (mUserCategories != null) {
            return true;
        }

        return false;
    }

    /*
     * DBからメモとカテゴリデータを取得
     */
    private void getOnDB() {

        // DB保存処理
        AsyncReadCategory db = new AsyncReadCategory(this, new AsyncReadCategory.OnFinishListener() {
            @Override
            public void onFinish(ArrayList<UserCategoryTable> categories) {

                // DBの情報を保持
                mUserCategories = categories;

                // 共通データ側も更新
                AppCommonData commonData = (AppCommonData) getApplication();
                commonData.setUserCategories(mUserCategories);

                // メモを一覧表示
                setCategoryList();
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * メモの一覧表示設定
     */
    private void setCategoryList() {

        // カテゴリをリスト表示
        RecyclerView rv_categoryList = findViewById(R.id.rv_categoryList);
        CategoryListAdapter categoryListAdapter = new CategoryListAdapter(mUserCategories);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        rv_categoryList.setAdapter(categoryListAdapter);
        rv_categoryList.setLayoutManager(linearLayoutManager);

        // カテゴリクリックリスナーの設定
        categoryListAdapter.setOnCategoryClickListener(this);

        // アイテムアニメーションの設定
//        rv_categoryList.setItemAnimator( new DefaultItemAnimator(){
//            @Override
//            public boolean animateRemove( RecyclerView.ViewHolder holder ){
//                // 必ずコールが必要
//                dispatchRemoveFinished( holder );
//                return false;
//            }
//        });

    }

    /*
     * 画面遷移 - 新規カテゴリ作成画面
     */
    private void transitionNewCategory() {

        // 画面遷移
        Intent intent = new Intent(this, CategoryRegistrationActivity.class);
        intent.putExtra( KEY_NEW_CATEGORY_REGISTRATION, true );

        mCategoryRegistrationLancher.launch( intent );
    }

    /*
     * 画面遷移 - カテゴリ更新画面
     */
    private void transitionUpdateCategory( UserCategoryTable category ) {

        // 画面遷移
        Intent intent = new Intent(this, CategoryRegistrationActivity.class);
        intent.putExtra( KEY_NEW_CATEGORY_REGISTRATION, false );
        intent.putExtra( KEY_CATEGORY_PID, category.getPid() );
        intent.putExtra( KEY_CATEGORY_NAME, category.getName() );

        mCategoryRegistrationLancher.launch( intent );
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
                // カテゴリの新規作成
                transitionNewCategory();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
     * 本メソッドは、各カテゴリクリック時処理として実装
     */
    @Override
    public void onCategoryClick(UserCategoryTable userCategory ) {
        // カテゴリ更新画面へ遷移
        transitionUpdateCategory( userCategory );
    }
}