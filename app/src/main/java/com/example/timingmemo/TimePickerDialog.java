package com.example.timingmemo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.timingmemo.common.AppCommonData;

public class TimePickerDialog extends DialogFragment {

    // 時分秒文字列（"hh:mm:ss"）
    public static String mhhmmssStr;
    // クリックリスナー
    private PositiveClickListener mPositiveClickListener;

    //空のコンストラクタ（DialogFragmentのお約束）
    public TimePickerDialog() {
    }

    //インスタンス作成
    public static TimePickerDialog newInstance( String hhmmssStr ) {
        mhhmmssStr = hhmmssStr;
        return new TimePickerDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_time_picker);

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

        NumberPicker np_hh1 = dialog.findViewById(R.id.np_hh1);
        NumberPicker np_mm1 = dialog.findViewById(R.id.np_mm1);
        NumberPicker np_ss1 = dialog.findViewById(R.id.np_ss1);

        //----------------------------------
        // NumberPicker初期設定
        //----------------------------------
        // 時間の範囲を設定
        np_hh1.setMaxValue(99);
        np_hh1.setMinValue(0);
        np_mm1.setMaxValue(59);
        np_mm1.setMinValue(0);
        np_ss1.setMaxValue(59);
        np_ss1.setMinValue(0);

        // 数値フォーマット設定
        np_hh1.setFormatter( new PickerFormatter() );
        np_mm1.setFormatter( new PickerFormatter() );
        np_ss1.setFormatter( new PickerFormatter() );

        //----------------------------------
        // 時分秒情報をPickerに反映
        //----------------------------------
        // 時分秒文字列を以下の形で分割
        // hh:mm:ss → 「hh」、「mm」、「ss」
        String[] hhmmss = mhhmmssStr.split( AppCommonData.TIME_FORMAT_DELIMITER );
        int hh = Integer.parseInt( hhmmss[0] );
        int mm = Integer.parseInt( hhmmss[1] );
        int ss = Integer.parseInt( hhmmss[2] );

        // Pickerに反映
        np_hh1.setValue( hh );
        np_mm1.setValue( mm );
        np_ss1.setValue( ss );
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
                String hhmmss = getTimePickerStr();
                mPositiveClickListener.onPositiveClick( hhmmss );
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
        NumberPicker np_hh1 = (NumberPicker) dialog.findViewById(R.id.np_hh1);
        NumberPicker np_mm1 = (NumberPicker) dialog.findViewById(R.id.np_mm1);
        NumberPicker np_ss1 = (NumberPicker) dialog.findViewById(R.id.np_ss1);

        Integer hhValue = np_hh1.getValue();
        Integer mmValue = np_mm1.getValue();
        Integer ssValue = np_ss1.getValue();
        String hh = String.format( "%02d", hhValue );
        String mm = String.format( "%02d", mmValue );
        String ss = String.format( "%02d", ssValue );

        // 時分秒を合成
        String delimiter = AppCommonData.TIME_FORMAT_DELIMITER;
        return (hh + delimiter + mm + delimiter + ss);
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
    public class PickerFormatter implements NumberPicker.Formatter {

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
