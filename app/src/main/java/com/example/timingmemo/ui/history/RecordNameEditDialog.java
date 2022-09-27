package com.example.timingmemo.ui.history;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;

public class RecordNameEditDialog extends DialogFragment {

    // 設定中の記録名
    private static String mRecordName;
    // クリックリスナー
    private PositiveClickListener mPositiveClickListener;

    //空のコンストラクタ（DialogFragmentのお約束）
    public RecordNameEditDialog() {
    }

    //インスタンス作成
    public static RecordNameEditDialog newInstance( String recordName ) {
        mRecordName = recordName;
        return new RecordNameEditDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_record_name_edit);

        // 記録名を設定
        setRecordName(dialog);
        // ユーザーOKイメージ押下設定
        setPositiveImage(dialog);

        return dialog;
    }

    /*
     * 記録名の設定
     */
    private void setRecordName( Dialog dialog ) {
        EditText et_recordName = dialog.findViewById(R.id.et_recordName);
        et_recordName.setText( mRecordName );
    }

    /*
     * ユーザーOKイメージ押下設定
     */
    private void setPositiveImage( Dialog dialog ) {
        ImageView iv_save = dialog.findViewById(R.id.iv_save);
        iv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 記録名を取得
                EditText et_recordName = dialog.findViewById(R.id.et_recordName);
                String recordName = et_recordName.getText().toString();

                // リスナー処理
                mPositiveClickListener.onPositiveClick( recordName );
                dismiss();
            }
        });
    }

    /*
     * クリックリスナーの設定
     */
    public void setOnPositiveClickListener( PositiveClickListener listener ) {
        mPositiveClickListener = listener;
    }

    /*
     * クリック検出用インターフェース
     */
    public interface PositiveClickListener {
        // クリックリスナー
        void onPositiveClick( String recordName );
    }

}
