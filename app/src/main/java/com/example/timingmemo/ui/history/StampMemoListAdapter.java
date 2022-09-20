package com.example.timingmemo.ui.history;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timingmemo.R;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.StampMemoTable;

import java.util.ArrayList;

/*
 * １ページあたりの記録リストアダプタ
 */
public class StampMemoListAdapter extends RecyclerView.Adapter<StampMemoListAdapter.StampMemoListViewHolder> {

    // 記録済みメモリスト
    private final ArrayList<StampMemoTable> mStampMemos;
    // クリックリスナー
    private ItemClickListener mItemClickListener;

    /*
     * 記録済みメモアイテム情報
     */
    class StampMemoListViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout cl_stampMemo;
        private final View v_memoColor;
        private final TextView tv_stampMemo;
        private final TextView tv_stampTime;
        private final TextView tv_systemTime;

        public StampMemoListViewHolder(View itemView, int position) {
            super(itemView);

            cl_stampMemo = itemView.findViewById( R.id.cl_stampMemo);
            v_memoColor = itemView.findViewById( R.id.v_memoColor );
            tv_stampMemo = itemView.findViewById( R.id.tv_stampMemo);
            tv_stampTime = itemView.findViewById( R.id.tv_stampTime);
            tv_systemTime = itemView.findViewById( R.id.tv_systemTime);
        }

        /*
         * ビューの設定
         */
        public void setView( int position ){

            //---------------------
            // 記録メモ情報
            //---------------------
            StampMemoTable stampMemo = mStampMemos.get( position );
            int memoColor = stampMemo.getMemoColor();
            String memoName = stampMemo.getMemoName();
            String stampTime = stampMemo.getStampingPlayTime();
            String systemTime = stampMemo.getStampingSystemTime();
            String delayTime = stampMemo.getDelayTime();

            // 記録メモ時間の組み立て：メモ時間＋遅延時間
            String stampTimeStr = stampTime + "(-" + delayTime + ")";

            //---------------------
            // 記録メモ情報の設定
            //---------------------
            v_memoColor.setBackgroundColor( memoColor );
            tv_stampMemo.setText( memoName );
            tv_stampTime.setText( stampTimeStr );
            tv_systemTime.setText( systemTime );

            // クリックリスナー
            cl_stampMemo.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         mItemClickListener.onItemClick( stampMemo );
                     }
                 }
            );
        }
    }


    /*
     * コンストラクタ
     */
    public StampMemoListAdapter(ArrayList<StampMemoTable> memos ) {
        mStampMemos = memos;
    }


    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return mStampMemos.get(position).getPid();
    }

    @Override
    public long getItemId(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return mStampMemos.get(position).getPid();
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public StampMemoListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        // 1データあたりのレイアウトを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_stamp_memo_on_list, viewGroup, false);

        return new StampMemoListViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     *  ページ毎の表示設定
     */
    @Override
    public void onBindViewHolder(@NonNull StampMemoListViewHolder viewHolder, final int i) {
        // ビューの設定
        viewHolder.setView( i );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        // 表示データ数を返す
        return mStampMemos.size();
    }

    /*
     * クリックリスナーの設定
     */
    public void setOnItemClickListener( ItemClickListener listener ) {
        mItemClickListener = listener;
    }

    /*
     * クリック検出用インターフェース
     */
    public interface ItemClickListener {
        // クリックリスナー
        void onItemClick(StampMemoTable stampMemo );
    }
}
