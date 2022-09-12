package com.example.timingmemo.ui.memo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timingmemo.R;
import com.example.timingmemo.db.UserMemoTable;

import java.util.ArrayList;

/*
 * １ページあたりのメモリストアダプタ
 *  (カテゴリ別に表示するメモリスト)
 */
public class MemoListAdapter extends RecyclerView.Adapter<MemoListAdapter.MemoListViewHolder> {

    // メモリスト（あるカテゴリのメモ）
    private ArrayList<UserMemoTable> mMemos;
    // メモクリックリスナー
    private MemoListAdapter.MemoClickListener mMemoClickListener;

    /*
     * 1ページ内のメモ
     */
    class MemoListViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_memoName;

        public MemoListViewHolder(View itemView, int position) {
            super(itemView);

            tv_memoName = itemView.findViewById( R.id.tv_memoName );
        }

        /*
         * ビューの設定
         */
        public void setView( int position ){

            UserMemoTable memo = mMemos.get( position );

            // メモ名を設定
            String memoName = memo.getName();
            tv_memoName.setText( memoName );

            // メモクリックリスナー
            tv_memoName.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         mMemoClickListener.onMemoClick(memo);
                     }
                 }
            );
        }
    }


    /*
     * コンストラクタ
     */
    public MemoListAdapter(ArrayList<UserMemoTable> memos ) {
        mMemos = memos;
    }


    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public MemoListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        // 1データあたりのレイアウトを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_memo_on_list, viewGroup, false);

        return new MemoListViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     *  ページ毎の表示設定
     */
    @Override
    public void onBindViewHolder(@NonNull MemoListViewHolder viewHolder, final int i) {
        // ビューの設定
        viewHolder.setView( i );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        // 表示データ数を返す
        return mMemos.size();
    }

    /*
     * メモクリックリスナーの設定
     */
    public void setOnMemoClickListener( MemoListAdapter.MemoClickListener listener ) {
        mMemoClickListener = listener;
    }


    /*
     * 処理結果通知用のインターフェース
     */
    public interface MemoClickListener {
        // メモクリックリスナー
        void onMemoClick(UserMemoTable userMemo );
    }
}
