package com.example.timingmemo.ui.record;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

import com.example.timingmemo.R;
import com.example.timingmemo.common.AppCommonData;

public class TimePickerMMSSDialog extends DialogFragment {

    // 分秒文字列（"mm:ss"）
    public String mmmssStr;
    // クリックリスナー
    private PositiveClickListener mPositiveClickListener;

    //空のコンストラクタ（DialogFragmentのお約束）
    public TimePickerMMSSDialog() {
    }

    //インスタンス作成
    public static TimePickerMMSSDialog newInstance() {
        return new TimePickerMMSSDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_time_picker_mmss);

        // Time Pickerの設定
        setTimePicker( dialog );
        // ユーザーOKイメージ押下設定
        setPositiveImage( dialog );

        return dialog;
    }

    /*
     * Time Pickerの設定
     */
    private void setTimePicker( Dialog dialog ) {

        NumberPicker np_m = dialog.findViewById(R.id.np_m);
        NumberPicker np_s = dialog.findViewById(R.id.np_s);

        //----------------------------------
        // NumberPicker初期設定
        //----------------------------------
        // 時間の範囲を設定
        np_m.setMaxValue(59);
        np_m.setMinValue(0);
        np_s.setMaxValue(59);
        np_s.setMinValue(0);

        // 数値フォーマット設定
        np_m.setFormatter( new PickerFormatter() );
        np_s.setFormatter( new PickerFormatter() );

        //----------------------------------
        // 時分秒情報をPickerに反映
        //----------------------------------
        // 時分秒文字列を以下の形で分割
        // mm:ss → 「mm」、「ss」
        String[] mmss = mmmssStr.split( AppCommonData.TIME_FORMAT_DELIMITER );
        int mm = Integer.parseInt( mmss[0] );
        int ss = Integer.parseInt( mmss[1] );

        // Pickerに反映
        np_m.setValue( mm );
        np_s.setValue( ss );
    }

    /*
     * ユーザーOKイメージ押下設定
     */
    private void setPositiveImage( Dialog dialog ) {
        ImageView iv_save = dialog.findViewById(R.id.iv_save);
        iv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ユーザーが設定した時分秒を返す
                String mmss = getTimePickerStr();
                mPositiveClickListener.onPositiveClick( mmss );
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
        NumberPicker np_m = dialog.findViewById(R.id.np_m);
        NumberPicker np_s = dialog.findViewById(R.id.np_s);

        Integer mmValue = np_m.getValue();
        Integer ssValue = np_s.getValue();
        String mm = String.format( "%02d", mmValue );
        String ss = String.format( "%02d", ssValue );

        // 時分秒を合成
        String delimiter = AppCommonData.TIME_FORMAT_DELIMITER;
        return (mm + delimiter + ss);
    }

    /*
     * 時間の設定
     */
    public void setTime( String mmssStr ) {
        mmmssStr = mmssStr;
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
        void onPositiveClick( String hhmmssStr );
    }

    /*
     * NumberPicker Formatter
     */
    private class PickerFormatter implements NumberPicker.Formatter {

        public PickerFormatter() {}

        @Override
        public String format(int num) {

            // 値が１桁の場合は、２桁目を０埋め
            if( num <= 9 ){
                return "0" + Integer.toString( num );
            }

            // 値が２桁の場合は、変更なし
            return Integer.toString( num );
        }

    }
}
