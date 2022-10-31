package com.example.timingmemo.ui.history;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.timingmemo.R;
import com.example.timingmemo.TimePickerHHMMSSDialog;
import com.example.timingmemo.common.AppCommonData;

public class RecordEditDialog extends DialogFragment {

    // 設定中の記録名
    private String mRecordName;
    // 設定中の記録時間
    private String mRecordedTime;
    // 記録された時間が最も長いメモの記録時間
    private String mLongestStampMemoTime;
    // クリックリスナー
    private PositiveClickListener mPositiveClickListener;

    //空のコンストラクタ（DialogFragmentのお約束）
    public RecordEditDialog() {
    }

    //インスタンス作成
    public static RecordEditDialog newInstance() {
        return new RecordEditDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_record_edit);

        // 記録名を設定
        setRecordName(dialog);

        // 記録時間を設定
        setRecordedTime(dialog);

        // ユーザーOKイメージ押下設定
        setPositiveImage(dialog);

        return dialog;
    }

    /*
     * 記録名の設定
     */
    private void setRecordName(Dialog dialog) {
        EditText et_recordName = dialog.findViewById(R.id.et_recordName);
        et_recordName.setText(mRecordName);
    }

    /*
     * 記録時間の設定
     */
    private void setRecordedTime(Dialog dialog) {

        NumberPicker np_h = dialog.findViewById(R.id.np_hh1);
        NumberPicker np_m = dialog.findViewById(R.id.np_m);
        NumberPicker np_s = dialog.findViewById(R.id.np_s);

        //----------------------------------
        // NumberPicker初期設定
        //----------------------------------
        // 時間の範囲を設定
        np_h.setMaxValue(99);
        np_h.setMinValue(0);
        np_m.setMaxValue(59);
        np_m.setMinValue(0);
        np_s.setMaxValue(59);
        np_s.setMinValue(0);

        // 数値フォーマット設定
        np_h.setFormatter(new TimePickerHHMMSSDialog.PickerFormatter());
        np_m.setFormatter(new TimePickerHHMMSSDialog.PickerFormatter());
        np_s.setFormatter(new TimePickerHHMMSSDialog.PickerFormatter());

        //----------------------------------
        // 時分秒情報をPickerに反映
        //----------------------------------
        // 時分秒文字列を以下の形で分割
        // hh:mm:ss → 「hh」、「mm」、「ss」
        String[] hhmmss = mRecordedTime.split(AppCommonData.TIME_FORMAT_DELIMITER);
        int hh = Integer.parseInt(hhmmss[0]);
        int mm = Integer.parseInt(hhmmss[1]);
        int ss = Integer.parseInt(hhmmss[2]);

        // Pickerに反映
        np_h.setValue(hh);
        np_m.setValue(mm);
        np_s.setValue(ss);
    }

    /*
     * ユーザーOKイメージ押下設定
     */
    private void setPositiveImage(Dialog dialog) {
        ImageView iv_save = dialog.findViewById(R.id.iv_save);
        iv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //----------------------
                // 記録名
                //----------------------
                // 記録名を取得
                EditText et_recordName = dialog.findViewById(R.id.et_recordName);
                String recordName = et_recordName.getText().toString();

                // 空判定
                if (recordName.isEmpty()) {
                    Toast.makeText(getContext(), R.string.toast_record_name_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                //----------------------
                // 記録時間
                //----------------------
                String settingRecordTime = getTimePickerStr();
                boolean isUnderLongestTime = isUnderLongestTime( settingRecordTime );
                if ( isUnderLongestTime ) {
                    Toast.makeText(getContext(), R.string.toast_record_time_lower, Toast.LENGTH_SHORT).show();
                    return;
                }

                // リスナー処理
                mPositiveClickListener.onPositiveClick( recordName, settingRecordTime );
                dismiss();
            }
        });
    }

    /*
     * 時間情報の文字列取得
     */
    private String getTimePickerStr() {

        Dialog dialog = getDialog();

        // 設定された時分秒を取得
        NumberPicker np_h = dialog.findViewById(R.id.np_hh1);
        NumberPicker np_m = dialog.findViewById(R.id.np_m);
        NumberPicker np_s = dialog.findViewById(R.id.np_s);

        Integer hhValue = np_h.getValue();
        Integer mmValue = np_m.getValue();
        Integer ssValue = np_s.getValue();
        String hh = String.format("%02d", hhValue);
        String mm = String.format("%02d", mmValue);
        String ss = String.format("%02d", ssValue);

        // 時分秒を合成
        String delimiter = AppCommonData.TIME_FORMAT_DELIMITER;
        return (hh + delimiter + mm + delimiter + ss);
    }


    /*
     * 時間前後判定
     *   @return：true：設定時間が最小メモ時間を下回っている場合
     */
    private boolean isUnderLongestTime(String newTime ) {

        // 「設定後の時間」＜「最長メモ時間」
        if ( newTime.compareTo(mLongestStampMemoTime) < 0) {
            return true;
        }

        return false;
    }

    /*
     * 記録名の設定
     */
    public void setRecordName( String recordName ) {
        mRecordName = recordName;
    }
    /*
     * 記録時間の設定
     */
    public void setRecordedTime(String recordedTime ) {
        mRecordedTime = recordedTime;
    }
    /*
     * 記録された時間が最も長いメモの記録時間の設定
     */
    public void setLongestStampMemoTime( String longestStampMemoTime ) {
        mLongestStampMemoTime = longestStampMemoTime;
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
        void onPositiveClick( String recordName, String recordTime );
    }

}
