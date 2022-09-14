package com.example.timingmemo.ui.memo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timingmemo.R;
import com.example.timingmemo.db.UserCategoryTable;

import java.util.ArrayList;

/*
 * １ページあたりのメモリストアダプタ
 *  (カテゴリ別に表示するメモリスト)
 */
public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoryListViewHolder> {

    // メモリスト（あるカテゴリのメモ）
    private final ArrayList<UserCategoryTable> mCategories;
    // メモクリックリスナー
    private CategoryClickListener mCategoryClickListener;

    /*
     * 1ページ内のメモ
     */
    class CategoryListViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_categoryName;

        public CategoryListViewHolder(View itemView, int position) {
            super(itemView);

            tv_categoryName = itemView.findViewById( R.id.tv_categoryName );
        }

        /*
         * ビューの設定
         */
        public void setView( int position ){

            UserCategoryTable category = mCategories.get( position );

            // カテゴリ名を設定
            String categoryName = category.getName();
            tv_categoryName.setText( categoryName );

            // メモクリックリスナー
            tv_categoryName.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Log.i("削除アニメ", "onClick = " + category.getName() + " " + category.getPid());
                         mCategoryClickListener.onCategoryClick( category );
                     }
                 }
            );
        }
    }


    /*
     * コンストラクタ
     */
    public CategoryListAdapter(ArrayList<UserCategoryTable> memos ) {
        mCategories = memos;
    }


    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return mCategories.get(position).getPid();
    }

    @Override
    public long getItemId(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return mCategories.get(position).getPid();
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public CategoryListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        // 1データあたりのレイアウトを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_category_on_list, viewGroup, false);

        return new CategoryListViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     *  ページ毎の表示設定
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryListViewHolder viewHolder, final int i) {
        // ビューの設定
        viewHolder.setView( i );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        // 表示データ数を返す
        return mCategories.size();
    }

    /*
     * メモクリックリスナーの設定
     */
    public void setOnCategoryClickListener( CategoryClickListener listener ) {
        mCategoryClickListener = listener;
    }

    /*
     * 処理結果通知用のインターフェース
     */
    public interface CategoryClickListener {
        // メモクリックリスナー
        void onCategoryClick( UserCategoryTable userCategory );
    }
}
