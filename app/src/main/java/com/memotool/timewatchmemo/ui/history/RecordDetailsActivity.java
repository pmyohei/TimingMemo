package com.memotool.timewatchmemo.ui.history;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.memotool.timewatchmemo.R;
import com.memotool.timewatchmemo.db.StampMemoTable;
import com.memotool.timewatchmemo.db.async.AsyncReadStampMemoCategory;
import com.memotool.timewatchmemo.db.async.AsyncRemoveRecord;
import com.memotool.timewatchmemo.db.async.AsyncUpdateRecord;
import com.memotool.timewatchmemo.ui.record.RecordFragment;

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
    public static final String KEY_TARGET_RECORD_TIME = "target_record_time";
    public static final String KEY_TARGET_MEMO_NAME = "target_memo_name";
    public static final String KEY_TARGET_MEMO_COLOR = "target_memo_color";
    public static final String KEY_TARGET_MEMO_PLAYTIME = "target_memo_playtime";

    // 画面遷移：戻り情報
    public static final int RESULT_RECORD_UPDATE = 201;
    public static final int RESULT_RECORD_REMOVE = 202;
    public static final String KEY_RECORD_PID = "record_pid";
    public static final String KEY_RECORD_NAME = "record_name";
    public static final String KEY_RECORD_TIME = "record_time";

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private ArrayList<StampMemoTable> mStampMemos;
    private ActivityResultLauncher<Intent> mStampMemoUpdateLancher;
    private boolean misInitScaleUnitSelected;
    private String mRecordTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_details);

        // 目盛り単位指定UIの初期コール
        misInitScaleUnitSelected = false;

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
                initTimeGraph();
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * タイムグラフの初期化
     */
    private void initTimeGraph() {

        // 親ビューのレイアウトが確定したタイミングで描画処理を行う
        HorizontalScrollView hsv_graph = findViewById(R.id.hsv_graph);
        hsv_graph.post(() -> {

            int parentHeight = hsv_graph.getHeight();

            //--------------------
            // グラフ初期設定
            //--------------------
            // 記録時間
            Intent intent = getIntent();
            mRecordTime = intent.getStringExtra(HistoryFragment.KEY_TARGET_RECORD_RECORDING_TIME);

            // 記録時間／目盛り単位デフォルト値の設定
            RecordTimeGraphView tgmv_graph = findViewById(R.id.tgmv_graph);
            tgmv_graph.initSizeData(parentHeight);
            tgmv_graph.setRecordTime(mRecordTime);
            tgmv_graph.setDefaultScaleUnit();
            // グラフの最大横幅
            setGraphLayoutWidth(tgmv_graph, hsv_graph);
            // 記録メモの設定
            tgmv_graph.setStampMemoList(mStampMemos);
            // 描画
            tgmv_graph.invalidate();

            //-----------------------
            // グラフ目盛り間隔UIの設定
            //-----------------------
            setGraghScaleUnitUI(tgmv_graph);
        });
    }

    /*
     * グラフ目盛り単位UIの設定
     */
    private void setGraghScaleUnitUI(RecordTimeGraphView tgmv_graph) {

        //-----------------------
        // グラフ目盛り間隔UIの設定
        //-----------------------
        // Spinnerで選択肢を提示
        Spinner sp_scaleUnit = findViewById(R.id.sp_scaleUnit);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.recordGraghScaleUnit, R.layout.spinner_scale_unit_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_scale_unit_item);
        sp_scaleUnit.setAdapter(adapter);

        // 目盛りデフォルト単位を初期設定値とする
        int scaleUnit = tgmv_graph.getScaleUnit();
        sp_scaleUnit.setSelection(scaleUnit);

        // 選択リスナー
        sp_scaleUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // 初回コールは何もしない
                if (!misInitScaleUnitSelected) {
                    misInitScaleUnitSelected = true;
                    return;
                }

                //---------------------------
                // 記録時間グラフの目盛り単位を変更
                //---------------------------
                HorizontalScrollView hsv_graph = findViewById(R.id.hsv_graph);

                // 記録時間グラフ変更
                tgmv_graph.setScaleUnit(i);
                setGraphLayoutWidth(tgmv_graph, hsv_graph);
                tgmv_graph.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /*
     * タイムグラフの横サイズを設定
     */
    private void setGraphLayoutWidth(RecordTimeGraphView tgmv_graph, ViewGroup parentView) {

        // グラフの横幅を計算
        int graghWidth = tgmv_graph.calcGraphWidthFromRecordTime();

        // 親ビューのサイズと比較し、大きい方を設定サイズとする（最低でも画面横いっぱいは描画させるため）
        int parentWidth = parentView.getWidth();
        int setWidth = Math.max(parentWidth, graghWidth);

//        Log.i("目盛り", "setGraphLayoutWidth setWidth" + setWidth);

        // レイアウトの横幅を変更
        tgmv_graph.getLayoutParams().width = setWidth;
        tgmv_graph.requestLayout();
    }

    /*
     * タイムグラフの横サイズを延長
     */
    private void extensionGraghScale(String preRecordTime, String recordTime) {

        // 時間に変化がなければ処理なし
        if (preRecordTime.compareTo(recordTime) == 0) {
            return;
        }

        //---------------------
        // グラフを延長
        //---------------------
        RecordTimeGraphView tgmv_graph = findViewById(R.id.tgmv_graph);
        HorizontalScrollView hsv_graph = findViewById(R.id.hsv_graph);

        // 記録時間を更新
        tgmv_graph.setRecordTime(recordTime);
        // 横幅を再計算
        setGraphLayoutWidth(tgmv_graph, hsv_graph);
        // グラフ再描画
        tgmv_graph.invalidate();
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

        // 記録時間
        intent.putExtra(KEY_TARGET_RECORD_TIME, mRecordTime);

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
        mStampMemoUpdateLancher.launch(intent);
    }

    /*
     * 記録メモリスト：新規追加
     */
    private void insertStampMemoList(Intent intent, StampMemoListAdapter adapter) {

        // 新規追加された記録メモ情報を取得
        StampMemoTable stampMemo = getUpdatedStampMemoFromDist(intent);

        // 「打刻時の経過時間」から、ソート済みリストの適切な位置に挿入する
        int insertPosition = RecordFragment.getInsertPosition(mStampMemos, stampMemo.getStampingPlayTime());
        mStampMemos.add(insertPosition, stampMemo);

        // アダプタに通知
        adapter.notifyItemInserted(insertPosition);
    }

    /*
     * 記録メモリスト：更新
     */
    private void updateStampMemoList(Intent intent, StampMemoListAdapter adapter) {

        //----------------------------
        // リスト更新
        //----------------------------
        // 更新された記録メモ情報を取得
        StampMemoTable stampMemo = getUpdatedStampMemoFromDist(intent);
        int stampMemoPid = stampMemo.getPid();

        // リスト上の位置を取得し、更新
        int position = getStampMemoListPos(stampMemoPid);
        mStampMemos.set(position, stampMemo);

        //----------------------------
        // リストソート処理
        //----------------------------
        boolean isChangedPlayTime = intent.getBooleanExtra(StampMemoUpdateActivity.KEY_CHANGED_PLAYTIME, false);
        if (!isChangedPlayTime) {
            // 記録時間更新なしなら、ソートなし
            // アダプタに通知
            adapter.notifyItemChanged(position);
            return;
        }

        // リストをソートし、ソート後の位置を取得
        Collections.sort(mStampMemos);
        int sortedPosition = getStampMemoListPos(stampMemoPid);
        if (position == sortedPosition) {
            // 位置が変わらなければ、ソートなし
            // アダプタに通知
            adapter.notifyItemChanged(position);
            return;
        }

        //-----------------------------------
        // アダプタに通知
        //-----------------------------------
        // 通知範囲の取得
        int startIndex = Math.min(position, sortedPosition);
        int endIndex = Math.max(position, sortedPosition);
        adapter.notifyItemRangeChanged(startIndex, endIndex);
    }

    /*
     * 記録メモリスト：削除
     */
    private void removeStampMemoList(Intent intent, StampMemoListAdapter adapter) {
        // リストから削除
        int pid = intent.getIntExtra(StampMemoUpdateActivity.KEY_STAMP_MEMO_PID, -1);
        int position = getStampMemoListPos(pid);
        mStampMemos.remove(position);

        // アダプタに通知
        adapter.notifyItemRemoved(position);
    }

    /*
     * 画面遷移先からの「新規追加 or 更新された記録メモ情報」を取得
     */
    private StampMemoTable getUpdatedStampMemoFromDist(Intent intent) {

        //-------------------------------
        // 画面遷移先からの記録メモ情報を取得
        //-------------------------------
        int pid = intent.getIntExtra(StampMemoUpdateActivity.KEY_STAMP_MEMO_PID, -1);
        int recordPid = intent.getIntExtra(StampMemoUpdateActivity.KEY_STAMP_MEMO_RECORD_PID, -1);
        String memoName = intent.getStringExtra(StampMemoUpdateActivity.KEY_STAMP_MEMO_NAME);
        int color = intent.getIntExtra(StampMemoUpdateActivity.KEY_STAMP_MEMO_COLOR, 0x00000000);
        String delayTime = intent.getStringExtra(StampMemoUpdateActivity.KEY_STAMP_MEMO_DELAYTIME);
        String playTime = intent.getStringExtra(StampMemoUpdateActivity.KEY_STAMP_MEMO_PLAYTIME);
        String systemTime = intent.getStringExtra(StampMemoUpdateActivity.KEY_STAMP_MEMO_SYSTEMTIME);

        //-------------------------------
        // 記録メモ情報を作成
        //-------------------------------
        StampMemoTable stampMemo = new StampMemoTable();
        stampMemo.setPid(pid);
        stampMemo.setRecordPid(recordPid);
        stampMemo.setMemoName(memoName);
        stampMemo.setMemoColor(color);
        stampMemo.setDelayTime(delayTime);
        stampMemo.setStampingPlayTime(playTime);
        stampMemo.setStampingSystemTime(systemTime);

        return stampMemo;
    }

    /*
     * 記録メモリストの位置取得
     */
    private int getStampMemoListPos(int targetPid) {

        int position = 0;
        for (StampMemoTable stampMemo : mStampMemos) {

            int searchPid = stampMemo.getPid();
            if (searchPid == targetPid) {
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
        String negative = getString(android.R.string.cancel);

        // 確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
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
                setFinishIntent(RESULT_RECORD_REMOVE, pid, null, null);
                finish();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * 記録メモの中で最も記録時間の長いメモの記録時間を取得
     */
    private String getLongestStampMemoTime() {

        int lastIndex = mStampMemos.size() - 1;
        if( lastIndex < 0 ){
            // 記録メモなしなら、記録時間初期値を返す
            String initTime = getString( R.string.init_record_time );
            return initTime;
        }

        // リスト最後の記録時間を返す
        return mStampMemos.get( lastIndex ).getStampingPlayTime();
    }

    /*
     * 記録名編集ダイアログの表示
     */
    private void showRecordEditDialog() {

        // 記録情報
        Intent intent = getIntent();
        String preRecordName = intent.getStringExtra(HistoryFragment.KEY_TARGET_RECORD_NAME);
        String preRecordingTime = mRecordTime;

        // 記録された時間が最も長いメモの記録時間
        String longestStampMemoTime = getLongestStampMemoTime();

        // 時間設定Dialogを開く
        RecordEditDialog dialog = RecordEditDialog.newInstance();
        dialog.setOnPositiveClickListener(new RecordEditDialog.PositiveClickListener() {
                @Override
                public void onPositiveClick(String recordName, String recordTime) {
                    // 記録名に反映
                    Toolbar toolbar = findViewById(R.id.toolbar_recordDetails);
                    toolbar.setTitle(recordName);

                    // 記録グラフの目盛りに反映
                    extensionGraghScale( preRecordingTime, recordTime );
                    mRecordTime = recordTime;

                    // 保存処理
                    saveUpdateRecord( recordName, recordTime );
                }
            }
        );
        dialog.setRecordName( preRecordName );
        dialog.setRecordedTime( preRecordingTime );
        dialog.setLongestStampMemoTime( longestStampMemoTime );
        dialog.show( getSupportFragmentManager(), "SHOW" );
    }

    /*
     * ＤＢ保存処理 - 記録更新
     */
    private void saveUpdateRecord( String recordName, String recordTime ) {

        Intent intent = getIntent();
        int recordPid = intent.getIntExtra(HistoryFragment.KEY_TARGET_RECORD_PID, -1);
        if (recordPid == -1) {
            // ガード
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // DB更新処理
        AsyncUpdateRecord db = new AsyncUpdateRecord(this, recordPid, recordName, recordTime, new AsyncUpdateRecord.OnFinishListener() {
            @Override
            public void onFinish(int pid, String updatedRecordName, String updatedRecordTime) {
                // 画面遷移元へのデータを設定し、終了
                setFinishIntent( RESULT_RECORD_UPDATE, pid, updatedRecordName, updatedRecordTime);
            }
        });
        // 非同期処理開始
        db.execute();
    }


    /*
     * 画面終了のindentデータを設定
     */
    private void setFinishIntent( int resultCode, int pid, String recordName, String recordTime ) {

        Intent intent = getIntent();
        intent.putExtra(KEY_RECORD_PID, pid);

        // 更新の場合
        if( resultCode == RESULT_RECORD_UPDATE ){
            intent.putExtra(KEY_RECORD_NAME, recordName);
            intent.putExtra(KEY_RECORD_TIME, recordTime);
        }

        // resultコード設定
        setResult(resultCode, intent);
    }

    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューを割り当て
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_add_remove_edit, menu);

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
            // 記録メモの追加
            case R.id.action_add:
                transitionStampMemoUpdate( null );
                return true;

            // 記録削除
            case R.id.action_remove:
                confirmRemove();
                return true;

            // 記録名の編集
            case R.id.action_edit:
                showRecordEditDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}