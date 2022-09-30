package com.example.timingmemo.ui.record;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.timingmemo.R;
import com.example.timingmemo.RecordNameEditDialog;
import com.example.timingmemo.TimePickerHHMMSSDialog;
import com.example.timingmemo.common.AppCommonData;
import com.example.timingmemo.db.RecordTable;
import com.example.timingmemo.db.StampMemoTable;
import com.example.timingmemo.db.UserCategoryTable;
import com.example.timingmemo.db.UserMemoTable;
import com.example.timingmemo.db.async.AsyncCreateRecord;
import com.example.timingmemo.db.async.AsyncReadMemoCategory;
import com.example.timingmemo.ui.memo.MemoListAdapter;
import com.example.timingmemo.ui.memo.MemoPageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends Fragment implements MemoListAdapter.MemoClickListener {

    //--------------------------------
    // 定数
    //--------------------------------
    private final int TIME_INTERVAL = 100;      // 100ms

    // 記録状態
    public static final int RECORD_PLAY = 0;      // 記録中
    public static final int RECORD_PAUSE = 1;     // 記録一時停止中
    public static final int RECORD_STOP = 2;      // 記録終了
    
    // レコードアニメーション制御
    private final int RECORDING_ANIM_START = 0;
    private final int RECORDING_ANIM_RESUME = 1;
    private final int RECORDING_ANIM_PAUSE = 2;
    private final int RECORDING_ANIM_STOP = 3;

    //--------------------------------
    // フィールド変数
    //--------------------------------
    private RecordTable mRecord;
    private ArrayList<StampMemoTable> mStampMemos;
    private ArrayList<UserMemoTable> mUserMemos;
    private ArrayList<UserCategoryTable> mUserCategories;
    private final Handler mTimeHandler = new Handler(Looper.getMainLooper());
    private Runnable mTimeRunnable;
    private ObjectAnimator mRecordingAnimator;
    private long mCountUpMsec;
    private TextView mtx_recordTime;
    private TextView mtv_delayTime;
    private int mRecordPlayState;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_record, container, false);

        // 記録画面初期設定
        init(root);

        // メモとカテゴリ情報を取得
        boolean isGet = getUserData();
        if (isGet) {
            // メモを一覧表示
            setMemoList(root);
        } else {
            // なければ、DBから取得
            getOnDB(root);
        }

        // 記録再生制御アイコンの設定
        setRecordControlIcon(root);
        // アイコン表示制御
        showRecordControlIcon(root, View.VISIBLE, View.GONE, View.GONE);
        // 記録時間／遅延時間設定
        setRecordStartTimeDelayTime(root);
        // 記録時間／遅延時間設定
        setRecordName(root);

        return root;
    }

    /*
     * 初期設定
     */
    private void init(View root) {

        // 記録時間(msec)
        mCountUpMsec = 0;
        // 記録状態
        mRecordPlayState = RECORD_STOP;
        // 記録メモリスト
        mStampMemos = new ArrayList<>();
        // 記録中のレコードアニメーション
        mRecordingAnimator = createRecordingAnimation(root);

        // 一定時間経過時の処理を用意
        mTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // 記録時間更新
                updateRecordTime();

                // 次の時間を指定
                mTimeHandler.postDelayed(this, TIME_INTERVAL);
            }
        };
    }

    /*
     * 記録開始初期処理
     */
    private void startRecordInit() {
        // カウントアップ開始
        mTimeHandler.post(mTimeRunnable);

        // 記録
        String recordTime = mtx_recordTime.getText().toString();
        String startDate = AppCommonData.getNowDate();
        mRecord = new RecordTable();
        mRecord.setStartRecordingTime(startDate);
        mRecord.setRecordingTime(recordTime);

        // 記録メモをクリア
        mStampMemos.clear();
    }

    /*
     * 記録名編集ダイアログの表示
     */
    private void showRecordNameEditDialog() {

        // 記録名
        TextView tv_recordName = mtx_recordTime.getRootView().findViewById(R.id.tv_recordName);
        String recordName = tv_recordName.getText().toString();

        // Dialogを開く
        RecordNameEditDialog dialog = RecordNameEditDialog.newInstance();
        dialog.setOnPositiveClickListener(new RecordNameEditDialog.PositiveClickListener() {
                @Override
                public void onPositiveClick(String recordName) {
                    // 記録名に反映
                    tv_recordName.setText(recordName);
                }
            }
        );
        dialog.setRecordName(recordName);
        dialog.show(getParentFragmentManager(), "SHOW");
    }

    /*
     * 記録時間更新処理
     */
    private void updateRecordTime() {
        // 記録中でなければ何もしない
        if (mRecordPlayState != RECORD_PLAY) {
            return;
        }

        // 時間を加算
        mCountUpMsec += TIME_INTERVAL;
        // 1s進んでいれば
        if ((mCountUpMsec % 1000) == 0) {
            // 記録時間を更新
            String hhssmm = formatHHMMSS(mCountUpMsec);
            mtx_recordTime.setText(hhssmm);
        }
    }

    /*
     * 記録再生制御アイコンの設定
     */
    private void setRecordControlIcon(View root) {

        ImageView iv_play = root.findViewById(R.id.iv_play);
        ImageView iv_pause = root.findViewById(R.id.iv_pause);
        ImageView iv_stop = root.findViewById(R.id.iv_stop);

        //-------------------
        // 記録開始アイコン
        //-------------------
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int animationCtrl = RECORDING_ANIM_RESUME;

                // 記録停止中なら記録開始
                if (mRecordPlayState == RECORD_STOP) {
                    startRecordInit();
                    animationCtrl = RECORDING_ANIM_START;
                }

                // 状態を記録中に更新
                mRecordPlayState = RECORD_PLAY;

                Log.i("記録", "開始ルート");

                // アイコン表示制御
                showRecordControlIcon(root, View.GONE, View.VISIBLE, View.VISIBLE);
                // レコードアニメーションの制御
                ctrlRecordingAnimation( animationCtrl );
            }
        });

        //-------------------
        // 記録一時停止アイコン
        //-------------------
        iv_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("記録", "一時停止ルート");

                // 時間カウント一時停止
                mRecordPlayState = RECORD_PAUSE;
                // アイコン表示制御
                showRecordControlIcon(root, View.VISIBLE, View.GONE, View.VISIBLE);
                // レコードアニメーションの制御
                ctrlRecordingAnimation( RECORDING_ANIM_PAUSE );
            }
        });

        //-------------------
        // 記録終了アイコン
        //-------------------
        iv_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 終了確認ダイアログの表示
                confirmStopRecord();
            }
        });
    }

    /*
     * 記録再生制御アイコンの表示・非表示
     *   @para1:記録開始アイコン表示値
     *   @para2:記録一時停止アイコン表示値
     *   @para3:記録停止アイコン表示値
     */
    private void showRecordControlIcon(View root, int playVisibility, int pauseVisibility, int stopVisibility) {

        //-----------------------
        // アニメーション
        //-----------------------
        Animation appearAnim = AnimationUtils.loadAnimation(root.getContext(), R.anim.appear_icon);
        Animation disappearAnim = AnimationUtils.loadAnimation(root.getContext(), R.anim.disappear_icon);

        //-----------------------
        // アイコン表示制御
        //-----------------------
        ImageView iv_play = root.findViewById(R.id.iv_play);
        ImageView iv_pause = root.findViewById(R.id.iv_pause);
        ImageView iv_stop = root.findViewById(R.id.iv_stop);

        // 現在の表示状態
        int currentPlay = iv_play.getVisibility();
        int currentPause = iv_pause.getVisibility();
        int currentStop = iv_stop.getVisibility();

        // 現在の表示状態と指定表示が異なれば、変更
        if (currentPlay != playVisibility) {
            iv_play.setVisibility(playVisibility);

            // アニメーション開始
            if (playVisibility == View.VISIBLE) {
                iv_play.startAnimation(appearAnim);
            } else {
                iv_play.startAnimation(disappearAnim);
            }

        }

        if (currentPause != pauseVisibility) {
            iv_pause.setVisibility(pauseVisibility);

            // アニメーション開始
            if (pauseVisibility == View.VISIBLE) {
                iv_pause.startAnimation(appearAnim);
            } else {
                iv_pause.startAnimation(disappearAnim);
            }

        }

        if (currentStop != stopVisibility) {
            iv_stop.setVisibility(stopVisibility);

            // アニメーション開始
            if (stopVisibility == View.VISIBLE) {
                iv_stop.startAnimation(appearAnim);
            } else {
                iv_stop.startAnimation(disappearAnim);
            }
        }
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
    private void getOnDB(View root) {

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
                setMemoList(root);
            }
        });
        //非同期処理開始
        db.execute();
    }

    /*
     * メモの一覧表示設定
     */
    private void setMemoList(View root) {

        Activity activity = getActivity();

        //--------------------------
        // ViewPager2の設定
        //--------------------------
        // ページ毎（カテゴリ別）のメモリストを生成
        ArrayList<ArrayList<UserMemoTable>> memosByCategory = MemoPageAdapter.getMemosByCategoryList(mUserCategories, mUserMemos);

        // Pageアダプタを設定
        MemoPageAdapter memoPageAdapter = new MemoPageAdapter(memosByCategory);
        ViewPager2 vp2_memoList = root.findViewById(R.id.vp2_memoList);
        vp2_memoList.setAdapter(memoPageAdapter);

        // メモクリックリスナーの設定
        memoPageAdapter.setOnMemoClickListener(this);

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


    /*
     * 記録時間／遅延時間の設定
     */
    private void setRecordStartTimeDelayTime(View root) {

        mtx_recordTime = root.findViewById(R.id.tx_recordTime);
        mtv_delayTime = root.findViewById(R.id.tv_delayTime);

        // 記録時間
        mtx_recordTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 設定中の時分秒
                String hhmmss = mtx_recordTime.getText().toString();
                // 時間設定Dialogを開く
                TimePickerHHMMSSDialog dialog = TimePickerHHMMSSDialog.newInstance();
                dialog.setTime(hhmmss);
                dialog.setOnPositiveClickListener(new TimePickerHHMMSSDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveClick(String hhmmssStr) {
                        // 記録時間変更処理
                        changeRecordTime(hhmmssStr);
                    }
                });
                dialog.show(getParentFragmentManager(), "SHOW");
            }
        });

        // 遅延時間
        mtv_delayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 設定中の時分秒
                String mmss = mtv_delayTime.getText().toString();
                // 時間設定Dialogを開く
                TimePickerMMSSDialog dialog = TimePickerMMSSDialog.newInstance();
                dialog.setTime(mmss);
                dialog.setOnPositiveClickListener(new TimePickerMMSSDialog.PositiveClickListener() {
                                                      @Override
                                                      public void onPositiveClick(String mmssStr) {
                                                          // 入力された時分秒をビューに反映
                                                          mtv_delayTime.setText(mmssStr);
                                                      }
                                                  }
                );
                dialog.show(getParentFragmentManager(), "SHOW");
            }
        });
    }

    /*
     * 記録名のレイアウト設定
     */
    private void setRecordName(View root) {

        // 記録名
        TextView tv_recordName = root.findViewById(R.id.tv_recordName);
        tv_recordName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 編集ダイアログの表示
                showRecordNameEditDialog();
            }
        });
    }

    /*
     * 記録時間の変更処理
     */
    private void changeRecordTime(String newhhmmssStr) {

        //-----------------------
        // 一番進んでいる記録時間の保持
        //-----------------------
        // 変更直前の記録時間
        String preRecordTime = mtx_recordTime.getText().toString();
        // 進んでいる方の時分秒を取得
        String mostTimeAdvanced = getAdvancedTime(preRecordTime, newhhmmssStr);
        // 現時点の記録時間として保持する
        mRecord.setRecordingTime(mostTimeAdvanced);

        //-----------------------
        // 記録時間変更
        //-----------------------
        // 入力された時分秒をビューに反映
        mtx_recordTime.setText(newhhmmssStr);
        // 記録開始時間を変更
        mCountUpMsec = getmsecFromHHMMSS(newhhmmssStr);
    }

    /*
     * 指定された「hh:mm:ss」フォーマットの文字列で、時間が進んでいる方を返す
     *   例) 「00:10:00」と「00:10:01」の比較
     *       →　「00:10:01」を返す
     */
    private String getAdvancedTime(String hhmmss1, String hhmmss2) {
        // 比較処理
        if (hhmmss1.compareTo(hhmmss2) < 0) {
            return hhmmss2;
        } else {
            return hhmmss1;
        }
    }

    /*
     * 記録終了の確認
     */
    private void confirmStopRecord() {

        // 各種文言
        String title = getString(R.string.dialog_title_stop_record);
        String content = getString(R.string.dialog_content_stop_record);
        String positive = getString(R.string.dialog_positive_confirm_stop_confirm);
        String negative = getString(android.R.string.cancel);

        // 確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 記録終了処理へ
                        stopRecord();
                    }
                })
                .setNegativeButton(negative, null)
                .show();
    }

    /*
     * 記録終了処理
     */
    private void stopRecord() {

        // 記録終了
        mRecordPlayState = RECORD_STOP;
        mTimeHandler.removeCallbacks(mTimeRunnable);

        // 記録アイコンの表示を記録開始用に変更
        View root = mtx_recordTime.getRootView();
        showRecordControlIcon(root, View.VISIBLE, View.GONE, View.GONE);

        // 記録を保存
        saveRecord(mtx_recordTime.getText().toString());

        // 記録時間を初期化
        mCountUpMsec = 0;
        String hhssmm = formatHHMMSS(0);
        mtx_recordTime.setText(hhssmm);

        // レコードアニメーションの制御
        ctrlRecordingAnimation( RECORDING_ANIM_STOP );
    }

    /*
     * 記録保存
     *   @para1：記録終了時点の記録時間（hh:mm:ss）
     */
    private void saveRecord(String recordStopTime) {

        //------------------
        // 記録情報の設定
        //------------------
        // 記録名
        TextView tv_recordName = mtx_recordTime.getRootView().findViewById(R.id.tv_recordName);
        String recordName = tv_recordName.getText().toString();
        mRecord.setName(recordName);

        // 記録時間
        // 進んでいる方の時分秒を取得
        String setRecordTime = mRecord.getRecordingTime();
        String mostTimeAdvanced = getAdvancedTime(setRecordTime, recordStopTime);
        // 最終的な記録時間として設定
        mRecord.setRecordingTime(mostTimeAdvanced);

        // 記録終了時間
        String endTime = AppCommonData.getNowDate();
        mRecord.setEndRecordingTime(endTime);

        //------------------
        // DB保存
        //------------------
        AsyncCreateRecord db = new AsyncCreateRecord(getActivity(), mRecord, mStampMemos, new AsyncCreateRecord.OnFinishListener() {
            @Override
            public void onFinish() {
                Toast.makeText(getActivity(), R.string.toast_complete_save_record, Toast.LENGTH_SHORT).show();
            }
        });
        // 非同期処理開始
        db.execute();
    }

    /*
     * 指定時間を「hh:mm:ss」フォーマットに変換する
     */
    private String formatHHMMSS(long msec) {

        // 単位を変換；msec → sec
        long second = msec / 1000;
        long minute = second / 60;

        // 時分秒変換
        long hh = minute / 60;
        long mm = minute;
        long ss = second % 60;

        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    /*
     * 「hh:mm:ss」文字列の時間を、msecに変換する
     */
    private long getmsecFromHHMMSS(String hhmmssStr) {

        // 時分秒文字列を以下の形で分割
        // hh:mm:ss → 「hh」、「mm」、「ss」
        String[] hhmmss = hhmmssStr.split(AppCommonData.TIME_FORMAT_DELIMITER);
        int hh = Integer.parseInt(hhmmss[0]);
        int mm = Integer.parseInt(hhmmss[1]);
        int ss = Integer.parseInt(hhmmss[2]);

        // ミリ秒変換
        long msec = (hh * 60l * 60l) + (mm * 60l) + ss;
        msec *= 1000;

        return msec;
    }

    /*
     * 「mm:ss」文字列の時間を、msecに変換する
     */
    private long getmsecFromMMSS(String mmssStr) {

        // 時分秒文字列を以下の形で分割
        // hh:mm:ss → 「hh」、「mm」、「ss」
        String[] hhmmss = mmssStr.split(AppCommonData.TIME_FORMAT_DELIMITER);
        int mm = Integer.parseInt(hhmmss[0]);
        int ss = Integer.parseInt(hhmmss[1]);

        // ミリ秒変換
        long msec = (mm * 60l) + ss;
        msec *= 1000;

        return msec;
    }


    /*
     * メモの記録時間を取得
     */
    private long getStampPlayTime() {

        //-------------------------
        // メモの記録時間の取得
        //-------------------------
        String recordTimeStr = mtx_recordTime.getText().toString();
        String delayTimeStr = mtv_delayTime.getText().toString();

        // 記録時間から遅延時間を差し引く
        long recordTime = getmsecFromHHMMSS(recordTimeStr);
        long delayTime = getmsecFromMMSS(delayTimeStr);

        long playTime = recordTime - delayTime;
        if (playTime < 0) {
            // 時間は0より前になるなら、0に丸める
            playTime = 0;
        }

        return playTime;
    }

    /*
     * レコードアニメーションの作成
     */
    private ObjectAnimator createRecordingAnimation(View root) {

        // レコードイメージ
        ImageView iv_recordCircle = root.findViewById(R.id.iv_recordCircle);

        // Animatorの生成
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iv_recordCircle, "rotation", 0.0f, 360.0f);
        objectAnimator.setDuration(4000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatMode(ValueAnimator.RESTART);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

        return objectAnimator;
    }

    /*
     * レコードアニメーションの制御
     */
    private void ctrlRecordingAnimation( int ctrl ) {

        // 制御に応じたアニメーション
        switch ( ctrl ){
            case RECORDING_ANIM_START:
                mRecordingAnimator.start();
                break;

            case RECORDING_ANIM_RESUME:
                mRecordingAnimator.resume();
                break;

            case RECORDING_ANIM_PAUSE:
                mRecordingAnimator.pause();
                break;

            case RECORDING_ANIM_STOP:
                mRecordingAnimator.cancel();
                break;
        }
    }

    /*
     * 記録メモ追加アニメーションの開始
     */
    public void startAddMemoAnimation(int memoColor) {

        View root = mtv_delayTime.getRootView();
        View v_stampMemoImage = root.findViewById(R.id.v_stampMemoImage);

        Log.i("アニメーション", "開始");

        Animation appearAnim = AnimationUtils.loadAnimation(root.getContext(), R.anim.appear_icon);
        v_stampMemoImage.setBackgroundColor( memoColor );
        v_stampMemoImage.setVisibility(View.VISIBLE);
        v_stampMemoImage.startAnimation(appearAnim);
        appearAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation disappearAnim = AnimationUtils.loadAnimation(root.getContext(), R.anim.disappear_icon);
                disappearAnim.setStartOffset(100);
                v_stampMemoImage.startAnimation(disappearAnim);

                Log.i("アニメーション", "開始 消失");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


    /*
     * 記録データを共通データとして保持する
     */
    public void setRecordingDataAsCommonData(){
        // 記録中でなければ何もしない
        if( mRecordPlayState == RECORD_STOP ){
            return;
        }

        //------------------
        // 共通データに保存
        //------------------
        // 記録情報
        AppCommonData commonData = (AppCommonData) getActivity().getApplication();

        commonData.setRecord( mRecord );
        commonData.setRecordPlayState( mRecordPlayState );
        commonData.setStampMemos( mStampMemos );

        // 記録時間
        //★
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 記録データの共通データ化
        setRecordingDataAsCommonData();
    }

    /*
     * 本メソッドは、各メモクリック時処理として実装
     */
    @Override
    public void onMemoClick(UserMemoTable userMemo ) {

        // 記録を開始していないなら、メッセージ表示して終了
        if( mRecordPlayState == RECORD_STOP ){
            Toast.makeText(getActivity(), R.string.toast_waring_stamp_memo, Toast.LENGTH_SHORT).show();
            return;
        }

        //--------------------
        // 記録メモデータ
        //--------------------
        String memoName = userMemo.getName();
        int memoColor = userMemo.getColor();
        String delayTime = mtv_delayTime.getText().toString();
        String currentTime = AppCommonData.getNowDate();

        // メモ記録時間
        long playTimeMsec = getStampPlayTime();
        String playTime = formatHHMMSS( playTimeMsec );

        //--------------------
        // 記録メモ生成
        //--------------------
        StampMemoTable stampMemo = new StampMemoTable();
        stampMemo.setMemoName( memoName );
        stampMemo.setMemoColor( memoColor );
        stampMemo.setDelayTime( delayTime );
        stampMemo.setStampingPlayTime( playTime );
        stampMemo.setStampingSystemTime( currentTime );

        // 記録メモリストに追加
        mStampMemos.add( stampMemo );

        //--------------------
        // メモ追加アニメーション
        //--------------------
        startAddMemoAnimation( memoColor );
    }
}
