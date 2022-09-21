package com.example.timingmemo.ui.record;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;
import com.example.timingmemo.db.async.AsyncReadMemoCategory;
import com.example.timingmemo.ui.memo.MemoListAdapter;
import com.example.timingmemo.ui.memo.MemoPageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends Fragment implements MemoListAdapter.MemoClickListener {

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private ArrayList<UserMemoTable> mUserMemos;
    private ArrayList<UserCategoryTable> mUserCategories;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_record, container, false);

        // メモとカテゴリ情報を取得
        boolean isGet = getUserData();
        if (isGet) {
            // メモを一覧表示
            setMemoList( root );
        } else {
            // なければ、DBから取得
            getOnDB( root );
        }

        return root;
    }

    /*
     * メモとカテゴリデータを取得
     */
    private boolean getUserData() {

        //-------------------
        // 共通データから取得
        //-------------------
        AppCommonData commonData = (AppCommonData) getActivity().getApplication();
        mUserMemos = commonData.getUserMemos();
        mUserCategories = commonData.getUserCategories();

        // 保持済みであれば
        if (mUserMemos != null) {
            return true;
        }

        return false;
    }

    /*
     * DBからメモとカテゴリデータを取得
     */
    private void getOnDB( View root ) {

        // DB保存処理
        AsyncReadMemoCategory db = new AsyncReadMemoCategory(getContext(), new AsyncReadMemoCategory.OnFinishListener() {
            @Override
            public void onFinish(ArrayList<UserMemoTable> memos, ArrayList<UserCategoryTable> categories) {

                // DBの情報を保持
                mUserMemos = memos;
                mUserCategories = categories;

                // 共通データ側も更新
                AppCommonData commonData = (AppCommonData) getActivity().getApplication();
                commonData.setUserMemos(mUserMemos);
                commonData.setUserCategories(mUserCategories);

                // メモを一覧表示
                setMemoList( root );
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * メモの一覧表示設定
     */
    private void setMemoList( View root ) {

        Activity activity = getActivity();

        //--------------------------
        // ViewPager2の設定
        //--------------------------
        // ページ毎（カテゴリ別）のメモリストを生成
        ArrayList<ArrayList<UserMemoTable>> memosByCategory = MemoPageAdapter.getMemosByCategoryList( mUserCategories, mUserMemos );

        // Pageアダプタを設定
        MemoPageAdapter memoPageAdapter = new MemoPageAdapter(memosByCategory);
        ViewPager2 vp2_memoList = root.findViewById(R.id.vp2_memoList);
        vp2_memoList.setAdapter(memoPageAdapter);

        // メモクリックリスナーの設定
        memoPageAdapter.setOnMemoClickListener( this );

        //--------------------------
        // インジケータの設定
        //--------------------------
        // インジケータに表示する文字列リスト
        AppCommonData commonData = (AppCommonData) activity.getApplication();
        List<String> tabCategoryName = commonData.getCategoryNameList();

        // インジケータの設定
        TabLayout tab_category = root.findViewById(R.id.tab_category);
        new TabLayoutMediator(tab_category, vp2_memoList,
                (tab, position) -> tab.setText(tabCategoryName.get(position))
        ).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    /*
     * 本メソッドは、各メモクリック時処理として実装
     */
    @Override
    public void onMemoClick(UserMemoTable userMemo ) {
    }

}