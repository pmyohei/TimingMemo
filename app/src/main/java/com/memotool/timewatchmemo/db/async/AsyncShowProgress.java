package com.memotool.timewatchmemo.db.async;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class AsyncShowProgress {
    private final Context mContext;
    private DialogFragment mProgressDialog;

    /*
     * コンストラクタ
     */
    public AsyncShowProgress(Context context) {
        mContext = context;
    }

    /*
     * バックグラウンド前処理
     */
    void onPreExecute() {

        //画面の向きを現在の向きで固定化
        //※処理中ダイアログ表示中に向きが変わると落ちるため
        Configuration config = mContext.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ((FragmentActivity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ((FragmentActivity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //処理中ダイアログを開く
        mProgressDialog = ProgressingDialog.newInstance();
        mProgressDialog.setCancelable(false);   //キャンセル不可
        mProgressDialog.show( ((FragmentActivity)mContext).getSupportFragmentManager(), "SHOW" );
    }

    /*
     * バックグランド処理終了後の処理
     */
    void onPostExecute() {
        //処理待ち用のダイアログをクローズ
        if (mProgressDialog != null && mProgressDialog.getShowsDialog()) {
            mProgressDialog.dismiss();
        }

        //画面向き固定化解除
        ((FragmentActivity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
