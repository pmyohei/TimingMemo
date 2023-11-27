package com.memotool.timewatchmemo.ui.memo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.memotool.timewatchmemo.R;

public class MemoFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_memo, container, false);

        // メモリスト画面へ遷移
        root.findViewById( R.id.tv_memo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MemoListActivity.class);
                startActivity(intent);
            }
        });

        // カテゴリリスト画面へ遷移
        root.findViewById( R.id.tv_category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                startActivity(intent);
            }
        });

        // ライセンス画面へ遷移
        setOnLicensesClicked(root);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /*
     * licenses押下時処理
     */
    public void setOnLicensesClicked(View root) {
        root.findViewById( R.id.tv_licenses).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OssLicensesMenuActivity.class);
                startActivity(intent);
            }
        });
    }
}