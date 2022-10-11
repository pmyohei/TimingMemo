package com.example.timingmemo.ui.memo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;
import com.example.timingmemo.db.async.AsyncCreateMemo;
import com.example.timingmemo.db.async.AsyncRemoveMemo;
import com.example.timingmemo.db.async.AsyncUpdateMemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MemoRegistrationActivity extends AppCompatActivity {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    public static final String KEY_UPDATED_PAGE_INDEX = "update_page_index";
    public static final int RESULT_MEMO_REGISTRAION = 200;

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private int mInitUserSelectedPageIndex;                 // 初めにユーザーが選択していたカテゴリのページindex
    private boolean mIsNewMemoRegistration;                 // 新規メモ登録の場合、true

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_registration);

        // 新規メモ or メモ更新 の情報を保持
        Intent intent = getIntent();
        mIsNewMemoRegistration = intent.getBooleanExtra(MemoListActivity.KEY_NEW_MEMO_REGISTRATION, true);

        // ツールバーの設定
        setToolbar();

        // 選択可能なカテゴリ設定
        setSelectableCategory();

        // 画面レイアウトの設定
        setRegistrationLayout();
    }

    /*
     * ツールバーの設定
     */
    private void setToolbar() {

        // ツールバータイトル
        String title = getString( R.string.toolbar_title_memo_registration );

        // ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_memoRegister);
        toolbar.setTitle( title );
        setSupportActionBar(toolbar);

        // 戻るボタンの表示
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /*
     * 選択可能なカテゴリの設定
     */
    private void setSelectableCategory() {

        // カテゴリリストを取得
        AppCommonData commonData = (AppCommonData) getApplication();
        List<String> categoryList = commonData.getCategoryNameList();

        // 選択肢のアダプタを設定
        Spinner sp_category = findViewById(R.id.sp_category);
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_category_item, categoryList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_category_item);
        sp_category.setAdapter(adapter);

        // 選択中だったカテゴリページをデフォルト選択肢として設定
        Intent intent = getIntent();
        int selectedCategoryPage = intent.getIntExtra(MemoListActivity.KEY_SELECTED_PAGE_INDEX, 0);
        sp_category.setSelection(selectedCategoryPage);

        // 選択中だったカテゴリページを保持
        mInitUserSelectedPageIndex = selectedCategoryPage;
    }


    /*
     * 本画面のレイアウト設定
     */
    private void setRegistrationLayout() {

        //-----------------
        // メモ名
        //-----------------
        // 更新の場合は、対象のメモ情報を設定
        if (!mIsNewMemoRegistration) {
            // 更新対象のメモ情報を取得
            Intent intent = getIntent();
            String memoName = intent.getStringExtra(MemoListActivity.KEY_MEMO_NAME);
            int memoColor = intent.getIntExtra(MemoListActivity.KEY_MEMO_COLOR, 0x000000);

            // メモ情報をレイアウトに反映
            EditText et_memoName = findViewById(R.id.et_memoName);
            et_memoName.setText(memoName);
            View v_selectedColor = findViewById(R.id.v_selectedColor);
            v_selectedColor.setBackgroundColor( memoColor );
        }

        //-----------------
        // 色選択リスト
        //-----------------
        // 色リストを取得し、アダプタを生成
        TypedArray colors = getResources().obtainTypedArray(R.array.memoSelectionColor);
        MemoSelectionColorAdapter adapter = new MemoSelectionColorAdapter( colors );
        // スクロール方向を用意
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation( LinearLayoutManager.HORIZONTAL );

        RecyclerView rv_colors = findViewById(R.id.rv_colors);
        rv_colors.setAdapter( adapter );
        rv_colors.setLayoutManager( linearLayoutManager );

        // 色クリックリスナーの設定
        adapter.setOnColorClickListener(new MemoSelectionColorAdapter.ColorClickListener() {
            @Override
            public void onColorClick(int color) {
                // 選択色を設定
                View v_color = findViewById( R.id.v_selectedColor);
                v_color.setBackgroundColor( color );
            }
        });
    }

    /*
     * メモ情報保存処理
     */
    private void saveMemo() {

        // 選択中のジャンルのpidを取得
        int categoryPid = getSelectedCategoryPid();

        // メモ名を取得
        EditText et_memoName = findViewById(R.id.et_memoName);
        String memoName = et_memoName.getText().toString();
        if (memoName.isEmpty()) {
            // メモ名未入力ならメッセージを表示
            Toast.makeText(this, R.string.toast_memo_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // メモ色の取得
        View v_selectedColor = findViewById( R.id.v_selectedColor );
        ColorDrawable colorDrawable = (ColorDrawable)v_selectedColor.getBackground();
        int color = colorDrawable.getColor();

        // 新規メモ or メモの更新
        if (mIsNewMemoRegistration) {
            saveNewMemo(categoryPid, memoName, color);
        } else {
            saveUpdateMemo(categoryPid, memoName, color);
        }
    }

    /*
     * ＤＢ保存処理 - 新規メモ
     */
    private void saveNewMemo(int categoryPid, String memoName, int color) {

        // 登録対象メモ
        UserMemoTable memo = new UserMemoTable();
        memo.setCategoryPid(categoryPid);
        memo.setName(memoName);
        memo.setColor(color);

        // DB保存処理
        AsyncCreateMemo db = new AsyncCreateMemo(this, memo, new AsyncCreateMemo.OnFinishListener() {
            @Override
            public void onFinish(int pid) {

                // 画面遷移元へのデータを設定し、終了
                setFinishIntent();
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * ＤＢ保存処理 - メモ更新
     */
    private void saveUpdateMemo(int categoryPid, String memoName, int color) {

        Intent intent = getIntent();
        int memoPid = intent.getIntExtra(MemoListActivity.KEY_MEMO_PID, -1);
        if (memoPid == -1) {
            // ガード
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新対象メモ
        UserMemoTable memo = new UserMemoTable();
        memo.setPid(memoPid);
        memo.setCategoryPid(categoryPid);
        memo.setName(memoName);
        memo.setColor(color);

        // DB保存処理
        AsyncUpdateMemo db = new AsyncUpdateMemo(this, memo, new AsyncUpdateMemo.OnFinishListener() {
            @Override
            public void onFinish(int pid) {
                // 画面遷移元へのデータを設定し、終了
                setFinishIntent();
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * ＤＢ保存処理 - メモ削除
     */
    private void saveRemoveMemo() {

        Intent intent = getIntent();
        int memoPid = intent.getIntExtra(MemoListActivity.KEY_MEMO_PID, -1);
        if (memoPid == -1) {
            // ガード
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // DB保存処理
        AsyncRemoveMemo db = new AsyncRemoveMemo(this, memoPid, new AsyncRemoveMemo.OnFinishListener() {
            @Override
            public void onFinish(int pid) {
                // 画面遷移元へのデータを設定し、終了
                setFinishIntent();
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * 選択中カテゴリのPidを取得
     */
    private int getSelectedCategoryPid() {

        // 選択中のIndexを取得
        Spinner sp_category = findViewById(R.id.sp_category);
        int selectedIndex = sp_category.getSelectedItemPosition();
        if (selectedIndex == 0) {
            return UserMemoTable.NO_CATEGORY;
        }

        // 先頭に「カテゴリなし」があるため、1つ下げる
        selectedIndex--;

        // 現在のカテゴリリスト
        AppCommonData commonData = (AppCommonData) getApplication();
        ArrayList<UserCategoryTable> userCategories = commonData.getUserCategories();

        // ガード処理
        if (selectedIndex >= userCategories.size()) {
            return UserMemoTable.NO_CATEGORY;
        }

        // 該当のPidを返す
        return userCategories.get(selectedIndex).getPid();
    }

    /*
     * 削除確認ダイアログの表示
     */
    private void confirmRemove() {

        // 各種文言
        String title = getString(R.string.dialog_title_confirm_remove);
        String content = getString(R.string.dialog_content_memo_confirm_remove);
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
                        saveRemoveMemo();
                    }
                })
                .setNegativeButton(negative, null)
                .show();

    }

    /*
     * 画面終了のindentデータを設定
     *   更新の必要なカテゴリページindexを設定する
     */
    private void setFinishIntent() {

        // 選択中のカテゴリindex
        Spinner sp_category = findViewById(R.id.sp_category);
        int selectedIndex = sp_category.getSelectedItemPosition();

        // 更新対象のページが、初期ユーザー選択カテゴリと異なっていれば、2ページ分の更新が必要
        int[] pageIndex;
        if( selectedIndex == mInitUserSelectedPageIndex ){
            pageIndex = new int[1];
        } else {
            pageIndex = new int[2];
        }

        // resultコード設定
        Intent intent = getIntent();
        intent.putExtra(KEY_UPDATED_PAGE_INDEX, pageIndex);
        setResult(RESULT_MEMO_REGISTRAION, intent);
    }

    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // 表示メニュー
        int menuId;
        if( mIsNewMemoRegistration ){
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
                saveMemo();
                return true;

            case R.id.action_remove:
                confirmRemove();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}