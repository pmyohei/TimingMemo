package com.example.timingmemo.ui.history;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.timingmemo.R;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.StampMemoTable;
import com.example.timingmemo.db.async.AsyncReadRecordCategory;
import com.example.timingmemo.db.async.AsyncReadStampMemoCategory;

import java.util.ArrayList;
import java.util.Objects;

/*
 * 記録詳細画面
 */
public class RecordDetailsActivity extends AppCompatActivity {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    public static final String KEY_ID_ADD = "is_add";
    public static final String KEY_TARGET_MEMO_PID = "target_memo_pid";
    public static final String KEY_TARGET_MEMO_NAME = "target_memo_name";
    public static final String KEY_TARGET_MEMO_COLOR = "target_memo_color";
    public static final String KEY_TARGET_MEMO_DELAYTIME = "target_memo_delaytime";
    public static final String KEY_TARGET_MEMO_STAMPTIME = "target_memo_stamptime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_details);

        // ツールバーの設定
        setToolbar();

        // 記録メモをDBから取得
        getStampMemoOnDB();


    }

    /*
     * ツールバーの設定
     */
    private void setToolbar() {

        // ツールバータイトル
        Intent intent = getIntent();
        String recordName = intent.getStringExtra(HistoryFragment.KEY_TARGET_RECORD_NAME);

        // ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_recordDetails);
        toolbar.setTitle(recordName);
        setSupportActionBar(toolbar);

        // 戻るボタンの表示
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /*
     * DBから記録を取得
     */
    private void getStampMemoOnDB() {

        // 表示対象の記録のPIDを取得
        Intent intent = getIntent();
        int pid = intent.getIntExtra(HistoryFragment.KEY_TARGET_RECORD_PID, -1);
        if (pid == -1) {
            // フェールセーフ
            return;
        }

        // DB読み込み処理
        AsyncReadStampMemoCategory db = new AsyncReadStampMemoCategory(this, pid, new AsyncReadStampMemoCategory.OnFinishListener() {
            @Override
            public void onFinish(ArrayList<StampMemoTable> stampMemos) {
                // 取得した記録メモをリスト表示
                setStampMempList(stampMemos);
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * 記録メモをリストで表示
     */
    private void setStampMempList(ArrayList<StampMemoTable> stampMemos) {

        // 記録メモをリスト表示
        RecyclerView rv_stampMemo = findViewById(R.id.rv_stampMemo);
        StampMemoListAdapter adapter = new StampMemoListAdapter(stampMemos);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        rv_stampMemo.setAdapter(adapter);
        rv_stampMemo.setLayoutManager(linearLayoutManager);

        // 記録メモクリックリスナーの設定
        adapter.setOnItemClickListener(new StampMemoListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(StampMemoTable stampMemo) {
                // 画面遷移　→　記録メモ更新画面
                transitionStampMemoUpdate( stampMemo );
            }
        });
    }

    /*
     * 画面遷移 - 記録メモ更新画面
     */
    private void transitionStampMemoUpdate( StampMemoTable stampMemo ) {

        Intent intent = new Intent(this, StampMemoUpdateActivity.class);
        boolean isAdd = true;

        // 記録メモの更新の場合の設定
        if( stampMemo != null ){
            intent.putExtra( KEY_TARGET_MEMO_NAME, stampMemo.getMemoName() );
            intent.putExtra( KEY_TARGET_MEMO_COLOR, stampMemo.getMemoColor() );
            intent.putExtra( KEY_TARGET_MEMO_DELAYTIME, stampMemo.getDelayTime() );
            intent.putExtra( KEY_TARGET_MEMO_STAMPTIME, stampMemo.getStampingPlayTime() );

            // 新規追加ではない
            isAdd = false;
        }

        // 新規・更新 共通設定
        intent.putExtra( KEY_ID_ADD, isAdd );

        // 画面遷移開始
        startActivity( intent );
    }

    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューを割り当て
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_add_remove, menu);

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
                transitionStampMemoUpdate( null );
                return true;

            case R.id.action_remove:
//                confirmRemove();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}