package com.example.timingmemo.ui.memo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timingmemo.R;
import com.example.timingmemo.db.UserMemoTable;

import java.util.ArrayList;

/*
 * カテゴリ分のページ表示用アダプタ
 */
public class MemoPageAdapter extends RecyclerView.Adapter<MemoPageAdapter.MemoPageViewHolder> {

    // カテゴリ別メモリスト
    private ArrayList<ArrayList<UserMemoTable>> mMemosByCategory;

    /*
     * 1ページのビュー
     */
    class MemoPageViewHolder extends RecyclerView.ViewHolder {

        private final RecyclerView rv_memoList;

        public MemoPageViewHolder(View itemView, int position) {
            super(itemView);

            rv_memoList = itemView.findViewById(R.id.rv_memoList);
        }

        /*
         * ビューの設定
         */
        public void setView( int position ){

            // 1ページあたりのメモをリスト表示
            ArrayList<UserMemoTable> memos = mMemosByCategory.get(position);
            MemoListAdapter memoListAdapter = new MemoListAdapter( memos );
            rv_memoList.setAdapter( memoListAdapter );
        }
    }


    /*
     * コンストラクタ
     */
    public MemoPageAdapter( ArrayList<ArrayList<UserMemoTable>> memosByCategory ) {
        mMemosByCategory = memosByCategory;
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
    public MemoPageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        //ビューを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.memo_list_in_page, viewGroup, false);

        return new MemoPageViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     *  ページ毎の表示設定
     */
    @Override
    public void onBindViewHolder(@NonNull MemoPageViewHolder viewHolder, final int i) {
        //ビューの設定
        viewHolder.setView( i );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        //表示データ数を返す
        return mMemosByCategory.size();
    }

}
