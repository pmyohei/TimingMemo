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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.example.timingmemo.R;
import com.example.timingmemo.db.StampMemoTable;
import com.example.timingmemo.db.async.AsyncReadStampMemoCategory;
import com.example.timingmemo.db.async.AsyncRemoveRecord;

import java.util.ArrayList;
import java.util.Collections;
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
    public static final String KEY_TARGET_MEMO_PLAYTIME = "target_memo_playtime";

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

                        // 操作に応じた通知処理
                        switch (resultCode) {
                            // 新規追加
                            case StampMemoUpdateActivity.RESULT_STAMP_MEMO_ADD:
                                insertStampMemoList(intent, adapter);
                                break;

                            // 更新
                            case StampMemoUpdateActivity.RESULT_STAMP_MEMO_UPDATE:
                                updateStampMemoList(intent, adapter);
                                break;

                            // 削除
                            case StampMemoUpdateActivity.RESULT_STAMP_MEMO_REMOVE:
                                removeStampMemoList(intent, adapter);
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
            public void onFinish(ArrayList<StampMemoTable> sortedStampMemos) {
                // 取得した記録メモをリスト表示
                setStampMemoList(sortedStampMemos);
                // タイムグラフの描画
                drawTimeGraph();
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * タイムグラフの描画
     */
    private void drawTimeGraph() {

        // 親ビューのレイアウトが確定したタイミングで描画処理を行う
        HorizontalScrollView hsv_graph = findViewById(R.id.hsv_graph);
        hsv_graph.post(() -> {

            TimeGraphMemoryView tgmv_graph = findViewById(R.id.tgmv_graph);

            // グラフの横幅を設定
            setGraphLayoutWidth( tgmv_graph, hsv_graph );
            // グラフメモリ設定
            tgmv_graph.setStampMemoList( mStampMemos );
            // 描画
            tgmv_graph.invalidate();
        });
    }

    /*
     * タイムグラフの横サイズを設定
     */
    private void setGraphLayoutWidth( TimeGraphMemoryView tgmv_graph, ViewGroup parentView ) {
        // 記録時間
        Intent intent = getIntent();
        String recordingTime = intent.getStringExtra(HistoryFragment.KEY_TARGET_RECORD_RECORDING_TIME);

        // グラフの横幅を計算
        int graghWidth = tgmv_graph.calcGraphWidthFromRecordTime(recordingTime);

        // 親ビューのサイズと比較し、大きい方を設定サイズとする（最低でも画面横いっぱいは描画させるため）
        int parentWidth = parentView.getWidth();
        int setWidth = Math.max(parentWidth, graghWidth);

        Log.i("目盛り", "setGraphLayoutWidth setWidth" + setWidth);

        // レイアウトの横幅を変更
        tgmv_graph.getLayoutParams().width = setWidth;
        tgmv_graph.requestLayout();
    }

    /*
     * 記録メモをリストで表示
     */
    private void setStampMemoList(ArrayList<StampMemoTable> stampMemos) {
        // リスト保持
        mStampMemos = stampMemos;

        // 記録メモをリスト表示
        RecyclerView rv_stampMemo = findViewById(R.id.rv_stampMemo);
        StampMemoListAdapter adapter = new StampMemoListAdapter(mStampMemos);
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
            intent.putExtra(KEY_TARGET_MEMO_PLAYTIME, stampMemo.getStampingPlayTime());

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
        mStampMemoUpdateLancher.launch( intent );
    }

    /*
     * 記録メモリスト：新規追加
     */
    private void insertStampMemoList( Intent intent, StampMemoListAdapter adapter ) {

        // 新規追加された記録メモ情報を取得
        StampMemoTable stampMemo = getUpdatedStampMemoFromDist( intent );

        // 「打刻時の経過時間」から、ソート済みリストの適切な位置に挿入する
        int insertPosition = getInsertPosition( stampMemo.getStampingPlayTime() );
        mStampMemos.add( insertPosition, stampMemo );

        // アダプタに通知
        adapter.notifyItemInserted( insertPosition );
    }

    /*
     * 記録メモリスト：更新
     */
    private void updateStampMemoList( Intent intent, StampMemoListAdapter adapter ) {

        //----------------------------
        // リスト更新
        //----------------------------
        // 更新された記録メモ情報を取得
        StampMemoTable stampMemo = getUpdatedStampMemoFromDist( intent );
        int stampMemoPid = stampMemo.getPid();

        // リスト上の位置を取得し、更新
        int position = getStampMemoListPos( stampMemoPid );
        mStampMemos.set( position, stampMemo );

        //----------------------------
        // リストソート処理
        //----------------------------
        boolean isChangedPlayTime = intent.getBooleanExtra( StampMemoUpdateActivity.KEY_CHANGED_PLAYTIME, false );
        if( !isChangedPlayTime ){
            // 記録時間更新なしなら、ソートなし
            // アダプタに通知
            adapter.notifyItemChanged( position );
            return;
        }

        // リストをソートし、ソート後の位置を取得
        Collections.sort( mStampMemos );
        int sortedPosition = getStampMemoListPos( stampMemoPid );
        if( position == sortedPosition ){
            // 位置が変わらなければ、ソートなし
            // アダプタに通知
            adapter.notifyItemChanged( position );
            return;
        }

        //-----------------------------------
        // アダプタに通知
        //-----------------------------------
        // 通知範囲の取得
        int startIndex = Math.min( position, sortedPosition );
        int endIndex = Math.max( position, sortedPosition );
        adapter.notifyItemRangeChanged( startIndex, endIndex );
    }

    /*
     * 記録メモリスト：削除
     */
    private void removeStampMemoList( Intent intent, StampMemoListAdapter adapter ) {
        // リストから削除
        int pid = intent.getIntExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_PID, -1 );
        int position = getStampMemoListPos( pid );
        mStampMemos.remove( position );

        // アダプタに通知
        adapter.notifyItemRemoved( position );
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
        String playTime = intent.getStringExtra( StampMemoUpdateActivity.KEY_STAMP_MEMO_PLAYTIME);
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
        stampMemo.setStampingPlayTime( playTime );
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
     * 記録メモリストに対して、指定された「打刻時の経過時間」の挿入位置を取得
     *   例）para：「00:20:00」
     *   [0]：「00:10:00」
     *   [1]：「00:30:00」
     *   → この場合、「1」を返す
     */
    private int getInsertPosition( String targetPlayTime ) {

        //-------------------------
        // リスト内挿入位置検索
        //-------------------------
        int position = 0;
        for( StampMemoTable stampMemo: mStampMemos ){
            // リスト内の時間が、対象の時間よりも後の場合
            String searchPlayTime = stampMemo.getStampingPlayTime();
            if( searchPlayTime.compareTo( targetPlayTime ) > 0){
                return position;
            }
            position++;
        }

        //---------------------------
        // 見つからなければ、終端位置を返す
        //---------------------------
        return position;
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