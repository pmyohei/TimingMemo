package com.example.timingmemo.ui.memo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.async.AsyncCreateCategory;
import com.example.timingmemo.db.async.AsyncRemoveCategory;
import com.example.timingmemo.db.async.AsyncUpdateCategory;

import java.util.ArrayList;
import java.util.Objects;

public class CategoryRegistrationActivity extends AppCompatActivity {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    public static final int RESULT_CATEGORY_NEW = 200;
    public static final int RESULT_CATEGORY_UPDATE = 201;
    public static final int RESULT_CATEGORY_REMOVE = 202;
    public static final String KEY_UPDATED_POSITION = "updated_position";   // 更新通知対象のposition(カテゴリリストindex)

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private boolean mIsNewCategoryRegistration;                 // 新規カテゴリ登録の場合、true


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_registration);

        // 新規カテゴリ or カテゴリ更新 の情報を保持
        Intent intent = getIntent();
        mIsNewCategoryRegistration = intent.getBooleanExtra(CategoryListActivity.KEY_NEW_CATEGORY_REGISTRATION, true);

        // ツールバーの設定
        setToolbar();
        // 画面レイアウトの設定
        setRegistrationLayout();
    }

    /*
     * ツールバーの設定
     */
    private void setToolbar() {

        // ツールバータイトル
        String title = getString( R.string.toolbar_title_category_registration );

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
     * 本画面のレイアウト設定
     *   新規カテゴリの登録 or 既存カテゴリの更新
     */
    private void setRegistrationLayout() {

        // 更新の場合は、選択されたカテゴリ名を設定
        if ( !mIsNewCategoryRegistration ) {
            Intent intent = getIntent();
            String name = intent.getStringExtra(CategoryListActivity.KEY_CATEGORY_NAME);

            EditText et_categoryName = findViewById(R.id.et_categoryName);
            et_categoryName.setText(name);
        }
    }

    /*
     * カテゴリ情報保存処理
     */
    private void saveCategory() {

        // カテゴリ名を取得
        EditText et_categoryName = findViewById(R.id.et_categoryName);
        String categoryName = et_categoryName.getText().toString();
        if (categoryName.isEmpty()) {
            // カテゴリ名未入力ならメッセージを表示
            Toast.makeText(this, R.string.toast_category_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // 新規カテゴリ or カテゴリの更新
        if (mIsNewCategoryRegistration) {
            // 新規保存
            saveNewCategory(categoryName);

        } else {
            // カテゴリPidを画面遷移元から取得
            Intent intent = getIntent();
            int categoryPid = intent.getIntExtra(CategoryListActivity.KEY_CATEGORY_PID, -1);
            // 更新
            saveUpdateCategory(categoryPid, categoryName);
        }
    }

    /*
     * ＤＢ保存処理 - 新規カテゴリ
     */
    private void saveNewCategory(String categoryName) {

        // 登録対象カテゴリ
        UserCategoryTable category = new UserCategoryTable();
        category.setName(categoryName);

        // DB保存処理
        AsyncCreateCategory db = new AsyncCreateCategory(this, category, new AsyncCreateCategory.OnFinishListener() {
            @Override
            public void onFinish(UserCategoryTable newCategory) {
                // 新規カテゴリをリストへ追加
                int position = addCategoryCommonList(newCategory);

                // 画面遷移元へのデータを設定し、終了
                setFinishIntent( RESULT_CATEGORY_NEW, position );
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * ＤＢ保存処理 - カテゴリ更新
     */
    private void saveUpdateCategory(int categoryPid, String categoryName) {

        // 更新対象カテゴリ
        UserCategoryTable category = new UserCategoryTable();
        category.setPid(categoryPid);
        category.setName(categoryName);

        // DB保存処理
        AsyncUpdateCategory db = new AsyncUpdateCategory(this, category, new AsyncUpdateCategory.OnFinishListener() {
            @Override
            public void onFinish(UserCategoryTable updatedCategory) {
                // 共通データのリストのカテゴリを更新
                int position = updateCategoryCommonList( updatedCategory );

                // 画面遷移元へのデータを設定し、終了
                setFinishIntent( RESULT_CATEGORY_UPDATE, position );
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * ＤＢ保存処理 - カテゴリ削除
     */
    private void saveRemoveCategory() {

        Intent intent = getIntent();
        int categoryPid = intent.getIntExtra(CategoryListActivity.KEY_CATEGORY_PID, -1);
        if (categoryPid == -1) {
            // ガード
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i("削除アニメ", "削除直前 categoryPid=" + categoryPid);

        // DB保存処理
        AsyncRemoveCategory db = new AsyncRemoveCategory(this, categoryPid, new AsyncRemoveCategory.OnFinishListener() {
            @Override
            public void onFinish(int pid) {
                // 共通データのリストのカテゴリを削除
                int position = removeCategoryCommonList( pid );

                // 画面遷移元へのデータを設定し、終了
                setFinishIntent( RESULT_CATEGORY_REMOVE, position );
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * 共通データのカテゴリリストにカテゴリを追加
     */
    private int addCategoryCommonList(UserCategoryTable category ) {
        // 共通データのリストにカテゴリを追加
        AppCommonData commonData = (AppCommonData) getApplication();
        ArrayList<UserCategoryTable> categories = commonData.getUserCategories();
        categories.add( category );

        // 追加カテゴリのindexを返す
        return (categories.size() - 1);
    }

    /*
     * 共通データのカテゴリリストのカテゴリを更新
     */
    private int updateCategoryCommonList( UserCategoryTable category ) {

        // 共通データのリストのカテゴリを更新
        AppCommonData commonData = (AppCommonData) getApplication();
        ArrayList<UserCategoryTable> categories = commonData.getUserCategories();

        // 更新対象カテゴリのリスト内の位置を取得
        int targetPid = category.getPid();
        int position = getCategoryIndexInList( categories, targetPid );

        // 更新
        UserCategoryTable targetCategory = categories.get( position );
        targetCategory.setName( category.getName() );

        return position;
    }

    /*
     * 共通データのカテゴリリストのカテゴリを削除
     */
    private int removeCategoryCommonList( int removedPid ) {
        // 共通データのリスト
        AppCommonData commonData = (AppCommonData) getApplication();
        ArrayList<UserCategoryTable> categories = commonData.getUserCategories();

        Log.i("削除アニメ", "removedPid=" + removedPid);

        // 更新対象カテゴリのリスト内の位置を取得し、リストから削除
        int position = getCategoryIndexInList( categories, removedPid );
        categories.remove( position );

        return position;
    }

    /*
     * 共通データのカテゴリリストのカテゴリを削除
     */
    private int getCategoryIndexInList( ArrayList<UserCategoryTable> categories, int pid ) {

        int i = 0;
        for( UserCategoryTable categoryInList: categories ){
            int pidInList = categoryInList.getPid();
            if( pidInList == pid ){
                return i;
            }

            i++;
        }

        return -1;
    }

    /*
     * 削除確認ダイアログの表示
     */
    private void confirmRemove() {

        // 各種文言
        String title = getString(R.string.dialog_title_confirm_remove);
        String content = getString(R.string.dialog_content_category_confirm_remove);
        String positive = getString(R.string.dialog_positive_confirm_remove);
        String negative = getString( android.R.string.cancel );

        // 確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle( title )
                .setMessage( content )
                .setPositiveButton( positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 削除処理へ
                        saveRemoveCategory();
                    }
                })
                .setNegativeButton(negative, null)
                .show();

    }

    /*
     * 画面終了のindentデータを設定
     *   更新通知対象のリスト位置を設定
     */
    private void setFinishIntent( int resultCode, int position ) {

        // 変更されたカテゴリのリスト内における位置を設定
        Intent intent = getIntent();
        intent.putExtra( KEY_UPDATED_POSITION, position );

        // resultコード設定
        setResult(resultCode, intent );
    }

    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // 表示メニュー
        int menuId;
        if( mIsNewCategoryRegistration ){
            menuId = R.menu.toolbar_save;
        } else {
            menuId = R.menu.toolbar_save_remove;
        }

        // メニューを割り当て
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(menuId, menu);

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
            case R.id.action_save:
                saveCategory();
                return true;

            case R.id.action_remove:
                // 削除確認処理
                confirmRemove();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
