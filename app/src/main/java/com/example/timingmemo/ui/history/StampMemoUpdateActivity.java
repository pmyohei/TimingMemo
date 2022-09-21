package com.example.timingmemo.ui.history;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;
import com.example.timingmemo.db.StampMemoTable;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;
import com.example.timingmemo.db.async.AsyncCreateStampMemo;
import com.example.timingmemo.db.async.AsyncReadMemoCategory;
import com.example.timingmemo.db.async.AsyncRemoveMemo;
import com.example.timingmemo.db.async.AsyncRemoveStampMemo;
import com.example.timingmemo.db.async.AsyncUpdateStampMemo;
import com.example.timingmemo.ui.memo.MemoListActivity;
import com.example.timingmemo.ui.memo.MemoListAdapter;
import com.example.timingmemo.ui.memo.MemoPageAdapter;
import com.example.timingmemo.ui.memo.MemoSelectionColorAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StampMemoUpdateActivity extends AppCompatActivity implements MemoListAdapter.MemoClickListener {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    // 画面遷移：戻り情報
    public static final int RESULT_STAMP_MEMO_ADD = 201;
    public static final int RESULT_STAMP_MEMO_UPDATE = 202;
    public static final int RESULT_STAMP_MEMO_REMOVE = 203;

    public static final String KEY_STAMP_MEMO_PID = "stamp_memo_pid";
    public static final String KEY_STAMP_MEMO_RECORD_PID = "stamp_memo_record_pid";
    public static final String KEY_STAMP_MEMO_NAME = "stamp_memo_name";
    public static final String KEY_STAMP_MEMO_COLOR = "stamp_memo_color";
    public static final String KEY_STAMP_MEMO_DELAYTIME = "stamp_memo_delaytime";
    public static final String KEY_STAMP_MEMO_STAMPTIME = "stamp_memo_stamptime";
    public static final String KEY_STAMP_MEMO_SYSTEMTIME = "stamp_memo_systemtime";

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private boolean mIsAddStampMemo;                 // 新規での記録メモ追加の場合、true
    private ArrayList<UserMemoTable> mUserMemos;
    private ArrayList<UserCategoryTable> mUserCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stamp_memo_update);

        // 新規メモ追加 or 記録メモ更新 の情報を保持
        Intent intent = getIntent();
        mIsAddStampMemo = intent.getBooleanExtra(RecordDetailsActivity.KEY_ID_ADD, true);

        // ツールバーの設定
        setToolbar();

        boolean isGet = getUserData();
        if (isGet) {
            // メモを一覧表示
            setLayout();
        } else {
            // なければ、DBから取得
            getOnDB();
        }
    }

    /*
     * ツールバーの設定
     */
    private void setToolbar() {

        // ツールバータイトル
        String title;
        if (mIsAddStampMemo) {
            title = getString(R.string.toolbar_title_stamp_memo_new);
        } else {
            title = getString(R.string.toolbar_title_stamp_memo_update);
        }

        // ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_stampMemoUpdate);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        // 戻るボタンの表示
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
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
    private void getOnDB() {

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
                setLayout();
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * 本画面のレイアウト設定
     */
    private void setLayout() {

        //-----------------
        // 記録メモ情報
        //-----------------
        setStampedMemoInfo();

        //-----------------
        // ユーザーメモリスト
        //-----------------
        //--------------------------
        // ViewPager2の設定
        //--------------------------
        // ページ毎（カテゴリ別）のメモリストを生成
        ArrayList<ArrayList<UserMemoTable>> memosByCategory = MemoPageAdapter.getMemosByCategoryList(mUserCategories, mUserMemos);

        // Pageアダプタを設定
        MemoPageAdapter memoPageAdapter = new MemoPageAdapter(memosByCategory);
        ViewPager2 vp2_memoList = findViewById(R.id.vp2_memoList);
        vp2_memoList.setAdapter(memoPageAdapter);

        // メモクリックリスナーの設定
        memoPageAdapter.setOnMemoClickListener(this);

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

        //-----------------
        // 色選択リスト
        //-----------------
        // 色リストを取得し、アダプタを生成
        TypedArray colors = getResources().obtainTypedArray(R.array.memoSelectionColor);
        MemoSelectionColorAdapter adapter = new MemoSelectionColorAdapter(colors);
        // スクロール方向を用意
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        RecyclerView rv_colors = findViewById(R.id.rv_colors);
        rv_colors.setAdapter(adapter);
        rv_colors.setLayoutManager(linearLayoutManager);

        // 色クリックリスナーの設定
        adapter.setOnColorClickListener(new MemoSelectionColorAdapter.ColorClickListener() {
            @Override
            public void onColorClick(int color) {
                // 選択色を設定
                View v_color = findViewById(R.id.v_selectedColor);
                v_color.setBackgroundColor(color);
            }
        });
    }

    /*
     * 記録メモ情報を本画面に反映
     */
    private void setStampedMemoInfo() {

        // 記録メモの更新でなければ何もしない
        if (mIsAddStampMemo) {
            return;
        }

        //------------------
        // 記録メモ情報の取得
        //------------------
        Intent intent = getIntent();
        String memoName = intent.getStringExtra(RecordDetailsActivity.KEY_TARGET_MEMO_NAME);
        int memoColor = intent.getIntExtra(RecordDetailsActivity.KEY_TARGET_MEMO_COLOR, 0x00000000);
        String stampTime = intent.getStringExtra(RecordDetailsActivity.KEY_TARGET_MEMO_STAMPTIME);

        //------------------
        // 記録メモ情報の設定
        //------------------
        // 記録メモ時間の設定
        TextView tv_stampTime = findViewById(R.id.tv_stampTime);
        tv_stampTime.setText(stampTime);

        // メモ名・メモ色の設定
        setSelectedMemoInfo(memoName, memoColor);
    }

    /*
     * 本画面のメモ名／メモ色の設定
     */
    private void setSelectedMemoInfo(String memoName, int memoColor) {

        EditText et_memoName = findViewById(R.id.et_memoName);
        View v_selectedColor = findViewById(R.id.v_selectedColor);

        et_memoName.setText(memoName);
        v_selectedColor.setBackgroundColor(memoColor);
    }

    /*
     * 記録メモ保存前処理
     *   メモ名の未入力チェックを行い、入力済みであれば新規追加or更新処理に移る
     */
    private void preSaveStampMemoAction() {

        //--------------------
        // メモ名入力チェック
        //--------------------
        boolean isMemoNameEmpty = isEmptyMemoName();
        if( isMemoNameEmpty ){
            // 未入力なら警告メッセージを表示
            Toast.makeText(this, R.string.toast_memo_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        //--------------------
        // 記録メモ保存処理
        //--------------------
        if( mIsAddStampMemo ){
            // 記録メモ新規追加
            saveAddStampMemo();
        } else {
            // 更新確認ダイアログの表示
            confirmUpdate();
        }
    }


    /*
     * 更新確認ダイアログの表示
     */
    private void confirmUpdate() {
        // 各種文言
        String title = getString(R.string.dialog_title_confirm_rewrire);
        String content = getString(R.string.dialog_content_memo_confirm_rewrire);
        String positive = getString(android.R.string.ok);
        String negative = getString(android.R.string.cancel);

        // 確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 更新処理へ
                        saveUpdateStampMemo();
                    }
                })
                .setNegativeButton(negative, null)
                .show();
    }

    /*
     * 削除確認ダイアログの表示
     */
    private void confirmRemove() {

        // 各種文言
        String title = getString(R.string.dialog_title_confirm_remove);
        String content = getString(R.string.dialog_content_stampmemo_confirm_remove);
        String positive = getString(R.string.dialog_positive_confirm_remove);
        String negative = getString(android.R.string.cancel);

        // 確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 削除処理へ
                        saveRemoveStampMemo();
                    }
                })
                .setNegativeButton(negative, null)
                .show();
    }

    /*
     * ＤＢ保存処理 - 記録メモ新規追加
     */
    private void saveAddStampMemo() {

        //--------------------------
        // 保存データ設定
        //--------------------------
        // 入力された記録メモ情報を取得
        StampMemoTable stampMemo = getInputStampMemoInfo();

        // 紐づいている記録のPidを設定
        Intent intent = getIntent();
        int recordPid = intent.getIntExtra(RecordDetailsActivity.KEY_TARGET_RECORD_PID, -1);
        stampMemo.setRecordPid( recordPid );

        //--------------------------
        // DB保存処理
        //--------------------------
        AsyncCreateStampMemo db = new AsyncCreateStampMemo(this, stampMemo, new AsyncCreateStampMemo.OnFinishListener() {
            @Override
            public void onFinish(StampMemoTable stampMemo) {
                // 画面遷移元へのデータを設定し、終了
                setFinishIntent( RESULT_STAMP_MEMO_ADD, stampMemo, -1 );
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * ＤＢ保存処理 - 記録メモ更新
     */
    private void saveUpdateStampMemo() {

        //--------------------------
        // 保存データ設定
        //--------------------------
        // 入力された記録メモ情報を取得する
        StampMemoTable stampMemo = getInputStampMemoInfo();

        // 更新対象の記録メモのPidも設定
        Intent intent = getIntent();
        int pid = intent.getIntExtra(RecordDetailsActivity.KEY_TARGET_MEMO_PID, -1);
        stampMemo.setPid( pid );

        //--------------------------
        // DB保存処理
        //--------------------------
        AsyncUpdateStampMemo db = new AsyncUpdateStampMemo(this, stampMemo, new AsyncUpdateStampMemo.OnFinishListener() {
            @Override
            public void onFinish(StampMemoTable stampMemo) {
                // 画面遷移元へのデータを設定し、終了
                setFinishIntent( RESULT_STAMP_MEMO_UPDATE, stampMemo, -1 );
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * ＤＢ保存処理 - 記録メモ削除
     */
    private void saveRemoveStampMemo() {

        //--------------------------
        // 削除対象の記録メモPidを取得
        //--------------------------
        Intent intent = getIntent();
        int memoPid = intent.getIntExtra(RecordDetailsActivity.KEY_TARGET_MEMO_PID, -1);
        if (memoPid == -1) {
            // ガード
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        //--------------------------
        // DB保存処理
        //--------------------------
        AsyncRemoveStampMemo db = new AsyncRemoveStampMemo(this, memoPid, new AsyncRemoveStampMemo.OnFinishListener() {
            @Override
            public void onFinish(int pid) {
                // 画面遷移元へのデータを設定し、終了
                setFinishIntent( RESULT_STAMP_MEMO_REMOVE, null, pid );
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * 記録メモ入力情報取得
     *   レイアウト上の記録メモ情報を取得する
     */
    private StampMemoTable getInputStampMemoInfo() {

        //---------------------------
        // レイアウトから記録メモ情報を取得
        //---------------------------
        TextView tv_stampTime = findViewById(R.id.tv_stampTime);
        EditText et_memoName = findViewById(R.id.et_memoName);
        View v_selectedColor = findViewById(R.id.v_selectedColor);

        String stampTime = tv_stampTime.getText().toString();
        String memoName = et_memoName.getText().toString();
        ColorDrawable colorDrawable = (ColorDrawable) v_selectedColor.getBackground();
        int memoColor = colorDrawable.getColor();

        //---------------------------
        // 記録メモを生成
        //---------------------------
        StampMemoTable stampMemo = new StampMemoTable();
        stampMemo.setStampingPlayTime(stampTime);
        stampMemo.setMemoName(memoName);
        stampMemo.setMemoColor(memoColor);

        return stampMemo;
    }

    /*
     * 記録メモ未入力チェック
     */
    private boolean isEmptyMemoName() {
        // メモ名未入力チェック
        EditText et_memoName = findViewById(R.id.et_memoName);
        String memoName = et_memoName.getText().toString();
        return memoName.isEmpty();
    }

    /*
     * 画面終了のindentデータを設定
     */
    private void setFinishIntent( int resultCode, StampMemoTable stampMemo, int pid ) {

        Intent intent = getIntent();
        int stampMemoPid;
        if( resultCode == RESULT_STAMP_MEMO_REMOVE ){
            // 削除の場合、該当のPidのみ
            stampMemoPid = pid;

        } else {
            stampMemoPid = stampMemo.getPid();

            // 記録メモ情報を画面遷移元へ渡す
            intent.putExtra(KEY_STAMP_MEMO_RECORD_PID, stampMemo.getRecordPid());
            intent.putExtra(KEY_STAMP_MEMO_NAME, stampMemo.getMemoName());
            intent.putExtra(KEY_STAMP_MEMO_COLOR, stampMemo.getMemoColor());
            intent.putExtra(KEY_STAMP_MEMO_DELAYTIME, stampMemo.getDelayTime());
            intent.putExtra(KEY_STAMP_MEMO_STAMPTIME, stampMemo.getStampingPlayTime());
            intent.putExtra(KEY_STAMP_MEMO_SYSTEMTIME, stampMemo.getStampingSystemTime());
        }

        // Pidの設定
        intent.putExtra(KEY_STAMP_MEMO_PID, stampMemoPid);
        // resultコード設定
        setResult(resultCode, intent);
    }

    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // 表示メニュー
        int menuId;
        if( mIsAddStampMemo ){
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
                // 記録メモ保存前処理
                preSaveStampMemoAction();
                return true;

            case R.id.action_remove:
                // 削除確認ダイアログの表示
                confirmRemove();
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
        // 押下されたメモを画面に反映
        setSelectedMemoInfo( userMemo.getName(), userMemo.getColor() );
    }
}