package com.memotool.timewatchmemo.db.async;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.memotool.timewatchmemo.R;

public class ProgressingDialog extends DialogFragment {

    //空のコンストラクタ（DialogFragmentのお約束）
    public ProgressingDialog(){}

    //インスタンス作成
    public static ProgressingDialog newInstance() {
        return new ProgressingDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.progress_dialog);

        //画面外タッチ時のクローズ不可
        dialog.setCanceledOnTouchOutside(false);

        /*-- キャンセル不可（setCancelable(false)）は、このタイミングで設定しても反映されないため注意 --*/

        //タイトル
        String title = getResources().getString(R.string.updating);
        dialog.setTitle(title);

        return dialog;
    }
}
