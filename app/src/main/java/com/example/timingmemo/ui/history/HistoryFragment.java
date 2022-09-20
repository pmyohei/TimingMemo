package com.example.timingmemo.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timingmemo.R;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.async.AsyncReadRecordCategory;
import com.example.timingmemo.ui.memo.CategoryRegistrationActivity;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    //--------------------------------
    // 画面遷移 - キー文字列
    //--------------------------------
    public static final String KEY_TARGET_RECORD_PID = "target_record_pid";
    public static final String KEY_TARGET_RECORD_NAME = "target_record_name";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        // 記録をDBから取得
        getRecordsOnDB();

        return root;
    }

    /*
     * DBから記録を取得
     */
    private void getRecordsOnDB() {

        // DB読み込み処理
        AsyncReadRecordCategory db = new AsyncReadRecordCategory( getContext(), new AsyncReadRecordCategory.OnFinishListener() {
            @Override
            public void onFinish(ArrayList<RecordTable> records) {

                setRecordList( records );
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * 記録をリストで表示
     */
    private void setRecordList( ArrayList<RecordTable> records ) {

        // 記録をリスト表示
        RecyclerView rv_recordList = getActivity().findViewById(R.id.rv_recordList);
        RecordListAdapter adapter = new RecordListAdapter(records);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( getContext() );

        rv_recordList.setAdapter(adapter);
        rv_recordList.setLayoutManager(linearLayoutManager);

        // 記録クリックリスナーの設定
        adapter.setOnRecordItemClickListener(new RecordListAdapter.RecordItemClickListener() {
            @Override
            public void onItemClick(RecordTable record) {
                // 画面遷移 → 記録詳細画面
                Intent intent = new Intent(getActivity(), RecordDetailsActivity.class);
                intent.putExtra( KEY_TARGET_RECORD_PID, record.getPid() );
                intent.putExtra( KEY_TARGET_RECORD_NAME, record.getName() );
                startActivity( intent );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}