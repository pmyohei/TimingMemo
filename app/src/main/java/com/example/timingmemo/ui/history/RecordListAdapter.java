package com.example.timingmemo.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timingmemo.R;
import com.example.timingmemo.db.RecordTable;

import java.util.ArrayList;

/*
 * １ページあたりの記録リストアダプタ
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.RecordListViewHolder> {

    // 記録リスト
    private final ArrayList<RecordTable> mRecords;
    // 記録クリックリスナー
    private RecordItemClickListener mRecordClickListener;

    /*
     * 記録１アイテム情報
     */
    class RecordListViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout cl_record;
        private final TextView tv_recordName;
        private final TextView tv_recordingTime;
        private final TextView tv_startTime;
        private final TextView tv_endTime;

        public RecordListViewHolder(View itemView, int position) {
            super(itemView);

            cl_record = itemView.findViewById( R.id.cl_record );
            tv_recordName = itemView.findViewById( R.id.tv_recordName );
            tv_recordingTime = itemView.findViewById( R.id.tv_recordingTime );
            tv_startTime = itemView.findViewById( R.id.tv_startTime );
            tv_endTime = itemView.findViewById( R.id.tv_endTime );
        }

        /*
         * ビューの設定
         */
        public void setView( int position ){

            //---------------------
            // 記録情報
            //---------------------
            RecordTable record = mRecords.get( position );
            String name = record.getName();
            String recordingTime = record.getRecordingTime();
            String startTime = record.getStartRecordingTime();
            String endTime = record.getEndRecordingTime();

            //---------------------
            // 記録情報の設定
            //---------------------
            tv_recordName.setText( name );
            tv_recordingTime.setText( recordingTime );
            tv_startTime.setText( startTime );
            tv_endTime.setText( endTime );

            // クリックリスナー
            cl_record.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         mRecordClickListener.onItemClick( record );
                     }
                 }
            );
        }
    }


    /*
     * コンストラクタ
     */
    public RecordListAdapter(ArrayList<RecordTable> memos ) {
        mRecords = memos;
    }


    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return mRecords.get(position).getPid();
    }

    @Override
    public long getItemId(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return mRecords.get(position).getPid();
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public RecordListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        // 1データあたりのレイアウトを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_record_on_list, viewGroup, false);

        return new RecordListViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     *  ページ毎の表示設定
     */
    @Override
    public void onBindViewHolder(@NonNull RecordListViewHolder viewHolder, final int i) {
        // ビューの設定
        viewHolder.setView( i );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        // 表示データ数を返す
        return mRecords.size();
    }

    /*
     * クリックリスナーの設定
     */
    public void setOnRecordItemClickListener( RecordItemClickListener listener ) {
        mRecordClickListener = listener;
    }

    /*
     * 処理結果通知用のインターフェース
     */
    public interface RecordItemClickListener {
        // クリックリスナー
        void onItemClick(RecordTable record );
    }
}
