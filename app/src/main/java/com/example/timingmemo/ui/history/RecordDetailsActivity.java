package com.example.timingmemo.ui.history;

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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.timingmemo.R;
import com.example.timingmemo.db.StampMemoTable;
import com.example.timingmemo.db.async.AsyncReadStampMemoCategory;
import com.example.timingmemo.db.async.AsyncRemoveRecord;

import java.util.ArrayList;
import java.util.Objects;

/*
 * 記録詳細画面
 */
public class RecordDetailsActivity extends AppCompatActivity {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    // 画面遷移：送信情報
    public static final String KEY_ID_ADD = "is_add";
    public static final String KEY_TARGET_MEMO_PID = "target_memo_pid";
    public static final String KEY_TARGET_RECORD_PID = "target_record_pid";
    public static final String KEY_TARGET_MEMO_NAME = "target_memo_name";
    public static final String KEY_TARGET_MEMO_COLOR = "target_memo_color";
    public static final String KEY_TARGET_MEMO_STAMPTIME = "target_memo_stamptime";

    // 画面遷移：戻り情報
    public static final int RESULT_RECORD_UPDATE = 201;
    public static final int RESULT_RECORD_REMOVE = 202;
    public static final String KEY_RECORD_PID = "record_pid";

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private ArrayList<StampMemoTable> mStampMemos;
    private ActivityResultLauncher<Intent> mStampMemoUpdateLancher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_details);

        // ツールバーの設定
        setToolbar();

        // 画面遷移ランチャーの生成
        setStampMemoUpdateLancher();

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
     * 画面遷移ランチャーの生成
     */
    private void setStampMemoUpdateLancher() {

        // 記録メモ更新画面遷移ランチャーの作成
        mStampMemoUpdateLancher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    // ResultCodeの取得
                    int resultCode = result.getResultCode();
                    if (resultCode == Activity.RESULT_CANCELED) {
                        // 戻るボタンでの終了なら何もしない
                        return;
                    }

                    //--------------------
                    // アダプタへ更新通知
                    //--------------------
                    RecyclerView rv_stampMemo = findViewById(R.id.rv_stampMemo);
                    StampMemoListAdapter adapter = (StampMemoListAdapter) rv_stampMemo.getAdapter();

                    Intent intent = result.getData();
                    int position;

                    // 操作に応じた通知処理
                    switch (resultCode) {
                        // 新規追加
                        case StampMemoUpdateActivity.RESULT_STAMP_MEMO_ADD:
                            position = insertStampMemoList( intent );
                            adapter.notifyItemInserted( position );
                            break;

                        // 更新
                        case StampMemoUpdateActivity.RESULT_STAMP_MEMO_UPDATE:
                            position = updateStampMemoList( intent );
                            adapter.notifyItemChanged( position );
                            break;

                        // 削除
                        case StampMemoUpdateActivity.RESULT_STAMP_MEMO_REMOVE:
                            position = removeStampMemoList( intent );
                            adapter.notifyItemRemoved( position );
                            break;
                    }
                }
            });
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
                mStampMemos = stampMemos;
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
                transitionStampMemoUpdate(stampMemo);
            }
        });
    }

    /*
     * 画面遷移 - 記録メモ更新画面
     */
    private void transitionStampMemoUpdate(StampMemoTable stampMemo) {

        Intent intent = new Intent(this, StampMemoUpdateActivity.class);
        boolean isAdd = true;

        // 記録メモの更新の場合の設定
        if (stampMemo != null) {
            intent.putExtra(KEY_TARGET_MEMO_PID, stampMemo.getPid());
            intent.putExtra(KEY_TARGET_MEMO_NAME, stampMemo.getMemoName());
            intent.putExtra(KEY_TARGET_MEMO_COLOR, stampMemo.getMemoColor());
            intent.putExtra(KEY_TARGET_MEMO_STAMPTIME, stampMemo.getStampingPlayTime());

            // 新規追加ではない
            isAdd = false;
        }

        // 表示対象の記録のPIDを取得
        int recordPid = getIntent().getIntExtra(HistoryFragment.KEY_TARGET_RECORD_PID, -1);
        if (recordPid == -1) {
            // フェールセーフ
            return;
        }

        // 新規・更新 共通設定
        intent.putExtra(KEY_ID_ADD, isAdd);
        intent.putExtra(KEY_TARGET_RECORD_PID, recordPid);

        // 画面遷移開始
        startActivity(intent);
    }

    /*
     * 記録メモリスト：新規追加
     */
    private int insertStampMemoList( Intent intent ) {

        // 新規追加 or 更新された記録メモ情報を取得
        StampMemoTable stampMemo = getUpdatedStampMemoFromDist( intent );

        // リストに追加し、追加後のindexを返す
        mStampMemos.add( stampMemo );
        return (mStampMemos.size() - 1);
    }

    /*
     * 記録メモリスト：更新
     */
    private int updateStampMemoList( Intent intent ) {

        // 新規追加 or 更新された記録メモ情報を取得
        StampMemoTable stampMemo = getUpdatedStampMemoFromDist( intent );
        int stampMemoPid = stampMemo.getPid();

        // リスト上の位置を取得
        int position = getStampMemoListPos( stampMemoPid );
        // 更新
        StampMemoTable targetStampMemo = mStampMemos.get(position);
        targetStampMemo = stampMemo;

        return position;
    }

    /*
     * 記録メモリスト：削除
     */
    private int removeStampMemoList( Intent intent ) {
        // リストから削除
        int pid = intent.getIntExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_PID, -1 );
        int position = getStampMemoListPos( pid );
        mStampMemos.remove( position );

        return position;
    }

    /*
     * 画面遷移先からの「新規追加 or 更新された記録メモ情報」を取得
     */
    private StampMemoTable getUpdatedStampMemoFromDist(Intent intent ) {

        //-------------------------------
        // 画面遷移先からの記録メモ情報を取得
        //-------------------------------
        int pid = intent.getIntExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_PID, -1 );
        int recordPid = intent.getIntExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_RECORD_PID, -1 );
        String memoName = intent.getStringExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_NAME );
        int color = intent.getIntExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_COLOR, 0x00000000 );
        String delayTime = intent.getStringExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_DELAYTIME );
        String stampTime = intent.getStringExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_STAMPTIME );
        String systemTime = intent.getStringExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_SYSTEMTIME );

        //-------------------------------
        // 記録メモ情報を作成
        //-------------------------------
        StampMemoTable stampMemo = new StampMemoTable();
        stampMemo.setPid( pid );
        stampMemo.setRecordPid( recordPid );
        stampMemo.setMemoName( memoName );
        stampMemo.setMemoColor( color );
        stampMemo.setDelayTime( delayTime );
        stampMemo.setStampingPlayTime( stampTime );
        stampMemo.setStampingSystemTime( systemTime );

        return stampMemo;
    }

    /*
     * 記録メモリストの位置取得
     */
    private int getStampMemoListPos( int targetPid ) {

        int position = 0;
        for( StampMemoTable stampMemo: mStampMemos ){

            int searchPid = stampMemo.getPid();
            if( searchPid == targetPid ){
                return position;
            }
            position++;
        }

        return -1;
    }

    /*
     * 削除確認ダイアログの表示
     */
    private void confirmRemove() {

        // 各種文言
        String title = getString(R.string.dialog_title_confirm_remove);
        String content = getString(R.string.dialog_content_record_confirm_remove);
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
                    saveRemoveRecord();
                }
            })
            .setNegativeButton(negative, null)
            .show();
    }

    /*
     * ＤＢ保存処理 - 記録削除
     */
    private void saveRemoveRecord() {

        Intent intent = getIntent();
        int recordPid = intent.getIntExtra(HistoryFragment.KEY_TARGET_RECORD_PID, -1);
        if (recordPid == -1) {
            // ガード
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // DB削除処理
        AsyncRemoveRecord db = new AsyncRemoveRecord(this, recordPid, new AsyncRemoveRecord.OnFinishListener() {
            @Override
            public void onFinish(int pid) {
                // 画面遷移元へのデータを設定し、終了
                setFinishIntent( pid );
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * 画面終了のindentデータを設定
     */
    private void setFinishIntent( int pid ) {
        // resultコード設定
        Intent intent = getIntent();
        intent.putExtra(KEY_RECORD_PID, pid);
        setResult(RESULT_RECORD_REMOVE, intent);
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
                confirmRemove();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}