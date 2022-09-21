package com.example.timingmemo.ui.history;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.timingmemo.R;
import com.example.timingmemo.db.async.AsyncRemoveMemo;
import com.example.timingmemo.ui.memo.MemoListActivity;

import java.util.Objects;

public class StampMemoUpdateActivity extends AppCompatActivity {

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private boolean mIsAddStampMemo;                 // 新規メモ追加の場合、true

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stamp_memo_update);

        // ツールバーの設定
        setToolbar();



    }

    /*
     * ツールバーの設定
     */
    private void setToolbar() {

        // ツールバータイトル
        String title = getString( R.string.toolbar_title_stamp_memo_update );

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
     * 削除確認ダイアログの表示
     */
    private void confirmRemove() {

        // 各種文言
        String title = getString(R.string.dialog_title_confirm_remove);
        String content = getString(R.string.dialog_content_stampmemo_confirm_remove);
        String positive = getString(R.string.dialog_positive_confirm_remove);
        String negative = getString( android.R.string.cancel );

        // 確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle( title )
            .setMessage( content )
            .setPositiveButton( positive, new DialogInterface.OnClickListener() {
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
     * ＤＢ保存処理 - 記録削除
     */
    private void saveRemoveStampMemo() {

        Intent intent = getIntent();
        int memoPid = intent.getIntExtra(MemoListActivity.KEY_MEMO_PID, -1);
        if (memoPid == -1) {
            // ガード
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        //ここ未対応
        // DB保存処理
        AsyncRemoveMemo db = new AsyncRemoveMemo(this, memoPid, new AsyncRemoveMemo.OnFinishListener() {
            @Override
            public void onFinish(int pid) {
                // 画面遷移元へのデータを設定し、終了
//                setFinishIntent();
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
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
            case R.id.action_add:
//                transitionStampMemoUpdate( null );
                return true;

            case R.id.action_remove:
                confirmRemove();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}