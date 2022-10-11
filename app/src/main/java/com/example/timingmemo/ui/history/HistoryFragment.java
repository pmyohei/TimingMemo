package com.example.timingmemo.ui.history;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timingmemo.R;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.RecordTableDao;
import com.example.timingmemo.db.async.AsyncReadRecordCategory;
import com.example.timingmemo.ui.memo.CategoryListAdapter;
import com.example.timingmemo.ui.memo.CategoryRegistrationActivity;
import com.example.timingmemo.ui.memo.MemoRegistrationActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    public static final String KEY_TARGET_RECORD_PID = "target_record_pid";
    public static final String KEY_TARGET_RECORD_NAME = "target_record_name";
    public static final String KEY_TARGET_RECORD_RECORDING_TIME = "target_recording_time";

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private final int NO_DATA = -1;
    private ArrayList<RecordTable> mRecords;
    private ActivityResultLauncher<Intent> mRecordDetailsLancher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        // 広告表示設定
        setAdvertisement(root);

        // 画面遷移ランチャーを生成
        setRecordDetailsLancher(root);

        // 記録をDBから取得
        getRecordsOnDB(root);

        return root;
    }

    /*
     * 広告設定
     */
    private void setAdvertisement( View root ) {
        // Admobロード
        AdView adView = root.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    /*
     * DBから記録を取得
     */
    private void getRecordsOnDB( View root ) {

        // DB読み込み処理
        AsyncReadRecordCategory db = new AsyncReadRecordCategory(getContext(), new AsyncReadRecordCategory.OnFinishListener() {
            @Override
            public void onFinish(ArrayList<RecordTable> records) {
                // 記録をリストに設定
                mRecords = records;
                setRecordList(root, records);
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * 画面遷移ランチャーを生成
     */
    private void setRecordDetailsLancher( View root ) {

        mRecordDetailsLancher = registerForActivityResult(
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

                        //---------------------------------
                        // 通知対象の記録のリスト位置を取得
                        //---------------------------------
                        Intent intent = result.getData();
                        int pid = intent.getIntExtra(RecordDetailsActivity.KEY_RECORD_PID, NO_DATA);
                        if (pid == NO_DATA) {
                            // フェイルセーフ
                            return;
                        }

                        // 記録リストから、指定Pidの記録の位置を取得
                        int position = getRecordListPos(pid);
                        if (position == NO_DATA) {
                            // フェイルセーフ
                            return;
                        }

                        //------------------
                        // アダプタへ更新通知
                        //------------------
                        RecyclerView rv_recordList = root.findViewById(R.id.rv_recordList);
                        RecordListAdapter adapter = (RecordListAdapter) rv_recordList.getAdapter();

                        // 操作に応じた通知処理
                        switch (resultCode) {
                            case RecordDetailsActivity.RESULT_RECORD_UPDATE:
                                // リストを更新して通知
                                String recordName = intent.getStringExtra(RecordDetailsActivity.KEY_RECORD_NAME);
                                mRecords.get( position ).setName( recordName );

                                adapter.notifyItemChanged(position);
                                break;

                            case RecordDetailsActivity.RESULT_RECORD_REMOVE:
                                // リストから記録を削除して通知
                                mRecords.remove( position );

                                adapter.notifyItemRemoved(position);
                                break;
                        }
                    }
                });
    }

    /*
     * 記録をリストで表示
     */
    private void setRecordList( View root, ArrayList<RecordTable> records) {

        // 記録をリスト表示
        RecyclerView rv_recordList = root.findViewById(R.id.rv_recordList);
        RecordListAdapter adapter = new RecordListAdapter(records);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        rv_recordList.setAdapter(adapter);
        rv_recordList.setLayoutManager(linearLayoutManager);

        // 記録クリックリスナーの設定
        adapter.setOnRecordItemClickListener(new RecordListAdapter.RecordItemClickListener() {
            @Override
            public void onItemClick(RecordTable record) {
                // 画面遷移 → 記録詳細画面
                Intent intent = new Intent(getActivity(), RecordDetailsActivity.class);
                intent.putExtra(KEY_TARGET_RECORD_PID, record.getPid());
                intent.putExtra(KEY_TARGET_RECORD_NAME, record.getName());
                intent.putExtra(KEY_TARGET_RECORD_RECORDING_TIME, record.getRecordingTime());
                mRecordDetailsLancher.launch(intent);
            }
        });
    }

    /*
     * 指定記録Pidの記録リスト上の位置を取得
     */
    private int getRecordListPos( int targetRecordPid ) {

        int position = 0;
        for(RecordTable record: mRecords){
            int listPid = record.getPid();
            if( listPid == targetRecordPid ){
                return position;
            }

            position++;
        }

        return NO_DATA;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}