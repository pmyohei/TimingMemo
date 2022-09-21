package com.example.timingmemo.ui.memo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timingmemo.R;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;

import java.util.ArrayList;
import java.util.List;

/*
 * カテゴリ分のページ表示用アダプタ
 */
public class MemoPageAdapter extends RecyclerView.Adapter<MemoPageAdapter.MemoPageViewHolder> {

    // カテゴリ別メモリスト
    private ArrayList<ArrayList<UserMemoTable>> mMemosByCategory;
    // メモクリックリスナー
    private MemoListAdapter.MemoClickListener mMemoClickListener;

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
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager( rv_memoList.getContext() );

            memoListAdapter.setOnMemoClickListener( mMemoClickListener );

            rv_memoList.setAdapter( memoListAdapter );
            rv_memoList.setLayoutManager( linearLayoutManager );
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

    /*
     * メモクリックリスナーの設定（橋渡し用）
     */
    public void setOnMemoClickListener( MemoListAdapter.MemoClickListener listener ) {
        mMemoClickListener = listener;
    }

    /*
     * メモリストをカテゴリ別リストとして生成
     */
    public static ArrayList<ArrayList<UserMemoTable>> getMemosByCategoryList( ArrayList<UserCategoryTable> userCategories, ArrayList<UserMemoTable> userMemos ) {

        //-------------------------------------------------
        // カテゴリ別でメモを振り分けるため、カテゴリpidリストを用意
        //-------------------------------------------------
        List<Integer> categoryPids = new ArrayList<>();
        // リストの先頭はカテゴリなしの値を設定
        categoryPids.add( UserMemoTable.NO_CATEGORY );
        // カテゴリのpidをリストへ追加
        for (UserCategoryTable category : userCategories) {
            int pid = category.getPid();
            categoryPids.add(pid);
        }

        //-------------------------------------------------
        // カテゴリ別でメモを振り分け
        //-------------------------------------------------
        ArrayList<ArrayList<UserMemoTable>> memosByCategory = new ArrayList<>();

        // カテゴリ分繰り返し
        for (int categoryPid : categoryPids) {
            // 1カテゴリ当たりのメモリストを追加
            ArrayList<UserMemoTable> memosPerCategory = new ArrayList<>();

            // メモ数分繰り返し
            for (UserMemoTable memoTable : userMemos) {
                // カテゴリpidと「メモが割り当てられたカテゴリpid」が一致している場合、リストへメモを追加
                int memoCategoryPid = memoTable.getCategoryPid();
                if (categoryPid == memoCategoryPid) {
                    memosPerCategory.add( memoTable );
                }
            }

            // あるカテゴリのメモリストを追加
            memosByCategory.add(memosPerCategory);
        }

        return memosByCategory;
    }

}
