package com.memotool.timewatchmemo.ui.memo;

import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.memotool.timewatchmemo.R;

/*
 * １ページあたりのメモリストアダプタ
 *  (カテゴリ別に表示するメモリスト)
 */
public class MemoSelectionColorAdapter extends RecyclerView.Adapter<MemoSelectionColorAdapter.ColorListViewHolder> {

    // 色選択候補リスト
    private final TypedArray mColors;
    // クリックリスナー
    private ColorClickListener mColorClickListener;

    /*
     * 1ページ内のメモ
     */
    class ColorListViewHolder extends RecyclerView.ViewHolder {

        private final View v_color;

        public ColorListViewHolder(View itemView, int position) {
            super(itemView);
            v_color = itemView.findViewById( R.id.v_selectedColor);
        }

        /*
         * ビューの設定
         */
        public void setView( int position ){
            // 色の設定
            int color = mColors.getColor( position, 0x003300 );
            v_color.setBackgroundColor( color );

            Log.i("色選択", "color=" + color);

            // 色クリックリスナー
            v_color.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         mColorClickListener.onColorClick( color );
                     }
                 }
            );
        }
    }

    /*
     * コンストラクタ
     */
    public MemoSelectionColorAdapter(TypedArray colors ) {
        mColors = colors;
    }

    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return position;
    }

    @Override
    public long getItemId(int position) {
        //positionをそのまま返すとアイテム削除時にちらつくため、pidで管理
        return position;
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public ColorListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        // 1データあたりのレイアウトを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_selection_color, viewGroup, false);

        return new ColorListViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     *  ページ毎の表示設定
     */
    @Override
    public void onBindViewHolder(@NonNull ColorListViewHolder viewHolder, final int i) {
        // ビューの設定
        viewHolder.setView( i );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        // 表示データ数を返す
        return mColors.length();
    }

    /*
     * メモクリックリスナーの設定
     */
    public void setOnColorClickListener( ColorClickListener listener ) {
        mColorClickListener = listener;
    }

    /*
     * 処理結果通知用のインターフェース
     */
    public interface ColorClickListener {
        // メモクリックリスナー
        void onColorClick( int color );
    }
}
