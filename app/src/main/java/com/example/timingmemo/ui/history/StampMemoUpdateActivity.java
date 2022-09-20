package com.example.timingmemo.ui.history;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.timingmemo.R;

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
//                confirmRemove();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}