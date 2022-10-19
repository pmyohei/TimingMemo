package com.example.timingmemo.ui.record;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.example.timingmemo.ui.history.StampMemoListAdapter;
import com.example.timingmemo.ui.memo.MemoListAdapter;
import com.example.timingmemo.ui.memo.MemoPageAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
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
    private long mRecordStartSystemTime;
    private long mRecordPauseSystemTime;
    private TextView mtv_recordTime;
    private TextView mtv_delayTime;
    private int mRecordPlayState;
    private boolean mIsRenewRecordStartTime;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_record, container, false);

        //-----------------------------
        // 初期処理
        //-----------------------------
        init(root);

        //-----------------------------
        // リスナー設定
        //-----------------------------
        // 記録（レコード）アイコンの設定
        setRecordIconListerner(root);
        // 記録再生制御アイコンの設定
        setRecordControlIconListerner(root);
        // 記録時間／遅延時間設定
        setRecordStartTimeDelayTimeListerner();
        // 記録名設定
        setRecordNameListerner(root);

        //-----------------------------
        // 記録中の情報を取得（ある場合）
        //-----------------------------
        // 記録中情報を取得
        boolean isRecording = getRecordingDataFromCommonData();
        if (isRecording) {
            //--------------------
            // 記録中の再開処理
            //--------------------
            initRecordingData();
            // 記録再開
            resumeRecordFromInterruption();
        } else {
            //--------------------
            // 記録開始前の初期処理
            //--------------------
            initRecordData();
        }

        // 記録制御アイコンの表示非表示
        showRecordControlIcon(root);

        //-----------------------------
        // ユーザー登録メモの取得／表示
        //-----------------------------
        // メモとカテゴリ情報を取得
        boolean isGet = getUserData();
        if (isGet) {
            // メモを一覧表示
            setMemoList(root);
        } else {
            // なければ、DBから取得
            getOnDB(root);
        }

        //-----------------------------
        // 記録中メモ参照用ボトムシート設定
        //-----------------------------
        setReferenceStampingMemo(root);

        return root;
    }

    /*
     * 初期処理
     */
    private void init(View root) {

        //--------------------------------
        // Viewの保持
        //--------------------------------
        mtv_recordTime = root.findViewById(R.id.tx_recordTime);
        mtv_delayTime = root.findViewById(R.id.tv_delayTime);

        //--------------------------------
        // 必要処理の生成
        //--------------------------------
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
     * 記録処理の初期処理
     */
    private void initRecordData() {
        // 記録
        mRecord = new RecordTable();
        // 記録メモリスト
        mStampMemos = new ArrayList<>();
        // 記録開始時間の再設定なし
        mIsRenewRecordStartTime = false;
    }

    /*
     * 記録中の場合の初期設定処理
     */
    private void initRecordingData() {

        AppCommonData commonData = (AppCommonData) getActivity().getApplication();

        //--------------------
        // 記録時間
        //--------------------
        // 状態がpauseの場合のみ（開始中であれば時間経過のタイミングで更新されるため）
        if (mRecordPlayState == RECORD_PAUSE) {
            // 記録時間を更新
            String recordTimeStr = commonData.getRecordTime();
            mtv_recordTime.setText(recordTimeStr);
        }

        //--------------------
        // 遅延時間
        //--------------------
        String delayTime = commonData.getDelayTime();
        mtv_delayTime.setText(delayTime);

        //--------------------
        // 記録名
        //--------------------
        String recordName = commonData.getRecord().getName();
        TextView tv_recordName = mtv_delayTime.getRootView().findViewById(R.id.tv_recordName);
        tv_recordName.setText(recordName);
    }

    /*
     * 記録開始処理
     *   @para1：記録開始の契機が記録開始アイコン押下かどうか
     */
    private void startRecord() {

        // 記録開始システム時間の初期設定
        initRecordStartSystemTime(true);

        // 時間周期コール開始
        mTimeHandler.post(mTimeRunnable);

        //-------------------
        // 記録データ新規生成
        //-------------------
        TextView tv_recordName = mtv_recordTime.getRootView().findViewById(R.id.tv_recordName);
        String recordName = tv_recordName.getText().toString();
        String recordTime = mtv_recordTime.getText().toString();
        String startDate = AppCommonData.getNowDate();

        mRecord.setStartRecordingTime(startDate);
        mRecord.setRecordingTime(recordTime);
        mRecord.setName(recordName);
    }

    /*
     * 記録再開処理
     * 　　pauseからの再開処理（画面遷移からの再開ではない）
     */
    private void resumeRecordFromPause() {

        // 停止中に記録開始時間の再設定がある場合
        if (mIsRenewRecordStartTime) {
            // 指定開始時間でシステム時間を設定
            String startRecordTime = mtv_recordTime.getText().toString();
            setRecordStartSystemTimeFromText(startRecordTime);

            return;
        }

        // 一時停止中の時間を記録開始時間に反映
        reflectPauseTimeInRecordStartSystemTime();
    }

    /*
     * 記録再開処理
     * 　　画面復帰からの再開処理
     */
    private void resumeRecordFromInterruption() {
        // 記録開始システム時間の初期設定
        initRecordStartSystemTime(false);

        // 時間周期コール開始
        mTimeHandler.post(mTimeRunnable);

        // レコードアニメーション
        // ※どんな記録状態でも開始はさせておく（pauseからの再開時のresume()が働くようにするため）
        ctrlRecordingAnimation(RECORDING_ANIM_START);

        if (mRecordPlayState == RECORD_PAUSE) {
            // 中断前がPause状態なら、ここですぐにアニメーションを停止
            ctrlRecordingAnimation(RECORDING_ANIM_PAUSE);
        }
    }

    /*
     * 記録開始システム時間の初期設定
     *   @para1：記録開始の契機が記録開始アイコン押下かどうか
     */
    private void initRecordStartSystemTime(boolean isClickStartIcon) {

        if (isClickStartIcon) {
            // 記録開始アイコン押下の場合、記録開始テキストの開始時間から開始させるように設定
            String startRecordTime = mtv_recordTime.getText().toString();
            setRecordStartSystemTimeFromText(startRecordTime);

        } else {
            // 画面遷移から中断されていた記録が再開する場合、退避していた記録開始時間を設定
            AppCommonData commonData = (AppCommonData) getActivity().getApplication();
            mRecordStartSystemTime = commonData.getRecordStartSystemTime();
        }
    }

    /*
     * 記録名編集ダイアログの表示
     */
    private void showRecordNameEditDialog() {

        // 記録名
        TextView tv_recordName = mtv_recordTime.getRootView().findViewById(R.id.tv_recordName);
        String recordName = tv_recordName.getText().toString();

        // Dialogを開く
        RecordNameEditDialog dialog = RecordNameEditDialog.newInstance();
        dialog.setOnPositiveClickListener(new RecordNameEditDialog.PositiveClickListener() {
                                              @Override
                                              public void onPositiveClick(String recordName) {
                                                  // 記録名に反映
                                                  tv_recordName.setText(recordName);
                                                  // 記録に設定
                                                  mRecord.setName(recordName);
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

        long advancedTime = System.currentTimeMillis() - mRecordStartSystemTime;

        // 記録時間を更新
        String hhssmm = formatHHMMSS(advancedTime);
        mtv_recordTime.setText(hhssmm);
    }

    /*
     * 停止時間分を記録開始時間に反映
     *    記録開始システム時間に対してpause時間を反映し、カウント時間を調整する
     */
    private void reflectPauseTimeInRecordStartSystemTime() {
        // 記録開始時間に対して、停止分の時間を加算
        mRecordStartSystemTime += (System.currentTimeMillis() - mRecordPauseSystemTime);
    }

    /*
     * 指定時間を記録開始時間にするための記録開始システム時間の設定
     */
    private void setRecordStartSystemTimeFromText(String hhmmssStr) {

        // 開始時間文字列をmsecに変換
        long startTimeMsec = getmsecFromHHMMSS(hhmmssStr);
        // 現在時刻
        long currentTimeMsec = System.currentTimeMillis();

        // 指定時刻からカウントを開始するために、現在時刻から指定時間を減算した値を記録開始時間とする
        // (記録開始時間と現在時間の差を、指定時間msecとする)
        mRecordStartSystemTime = currentTimeMsec - startTimeMsec;
    }

    /*
     * 記録(レコード)アイコンの設定
     */
    private void setRecordIconListerner(View root) {

        ImageView iv_recordCircle = root.findViewById(R.id.iv_recordCircle);
        iv_recordCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ボトムシートオープン
                ConstraintLayout cl_stampingMemoBottomSheet = root.findViewById(R.id.cl_stampingMemoBottomSheet);
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(cl_stampingMemoBottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    /*
     * 記録再生制御アイコンの設定
     */
    private void setRecordControlIconListerner(View root) {

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

                if (mRecordPlayState == RECORD_STOP) {
                    // 記録停止中の場合、記録開始
                    startRecord();
                    animationCtrl = RECORDING_ANIM_START;

                } else if (mRecordPlayState == RECORD_PAUSE) {
                    // 記録一時停止中の場合、再開処理
                    resumeRecordFromPause();
                }

                // 状態を記録中に更新
                mRecordPlayState = RECORD_PLAY;
                // 記録開始時間の再設定リセット
                mIsRenewRecordStartTime = false;

                // アイコン表示制御
                showRecordControlIcon(root);
                // レコードアニメーションの制御
                ctrlRecordingAnimation(animationCtrl);
            }
        });

        //-------------------
        // 記録一時停止アイコン
        //-------------------
        iv_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 時間カウント一時停止
                mRecordPlayState = RECORD_PAUSE;
                // 一時停止時点のシステム時間を保持
                mRecordPauseSystemTime = System.currentTimeMillis();
                // アイコン表示制御
                showRecordControlIcon(root);
                // レコードアニメーションの制御
                ctrlRecordingAnimation(RECORDING_ANIM_PAUSE);
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
    private void showRecordControlIcon(View root) {

        //-----------------------
        // アニメーション
        //-----------------------
        Animation appearAnim = AnimationUtils.loadAnimation(root.getContext(), R.anim.appear_icon);
        Animation disappearAnim = AnimationUtils.loadAnimation(root.getContext(), R.anim.disappear_icon);

        //-----------------------
        // アイコン表示情報
        //-----------------------
        ImageView iv_play = root.findViewById(R.id.iv_play);
        ImageView iv_pause = root.findViewById(R.id.iv_pause);
        ImageView iv_stop = root.findViewById(R.id.iv_stop);

        // 現在の表示状態
        int currentPlay = iv_play.getVisibility();
        int currentPause = iv_pause.getVisibility();
        int currentStop = iv_stop.getVisibility();

        // 現在の記録状態に合わせた表示値
        int[] changeVisibility = getRecordControlIconVisivility();

        //-------------------------------------------
        // アイコンの表示変更
        //   現在の表示状態と指定表示が異なれば、変更
        //-------------------------------------------
        // 開始アイコン
        if (currentPlay != changeVisibility[0]) {
            iv_play.setVisibility(changeVisibility[0]);

            // アニメーション開始
            if (changeVisibility[0] == View.VISIBLE) {
                iv_play.startAnimation(appearAnim);
            } else {
                iv_play.startAnimation(disappearAnim);
            }
        }

        // 一時停止アイコン
        if (currentPause != changeVisibility[1]) {
            iv_pause.setVisibility(changeVisibility[1]);

            // アニメーション開始
            if (changeVisibility[1] == View.VISIBLE) {
                iv_pause.startAnimation(appearAnim);
            } else {
                iv_pause.startAnimation(disappearAnim);
            }
        }

        // 停止アイコン
        if (currentStop != changeVisibility[2]) {
            iv_stop.setVisibility(changeVisibility[2]);

            // アニメーション開始
            if (changeVisibility[2] == View.VISIBLE) {
                iv_stop.startAnimation(appearAnim);
            } else {
                iv_stop.startAnimation(disappearAnim);
            }
        }
    }

    /*
     * 現在の記録状態に応じた記録再生制御アイコンの表示・非表示 制御値の取得
     *   [0]:記録開始アイコン表示値
     *   [1]:記録一時停止アイコン表示値
     *   [2]:記録停止アイコン表示値
     */
    private int[] getRecordControlIconVisivility() {

        int[] visivility = new int[3];

        // 記録状態に応じたアイコンの表示状態
        switch (mRecordPlayState) {
            case RECORD_PLAY:
                visivility[0] = View.GONE;
                visivility[1] = View.VISIBLE;
                visivility[2] = View.VISIBLE;
                break;

            case RECORD_PAUSE:
                visivility[0] = View.VISIBLE;
                visivility[1] = View.GONE;
                visivility[2] = View.VISIBLE;
                break;

            case RECORD_STOP:
                visivility[0] = View.VISIBLE;
                visivility[1] = View.GONE;
                visivility[2] = View.GONE;
                break;
        }

        return visivility;
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
    private void setRecordStartTimeDelayTimeListerner() {

        // 記録時間
        mtv_recordTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 設定中の時分秒
                String hhmmss = mtv_recordTime.getText().toString();
                // 時間設定Dialogを開く
                TimePickerHHMMSSDialog dialog = TimePickerHHMMSSDialog.newInstance();
                dialog.setTime(hhmmss);
                dialog.setOnPositiveClickListener(new TimePickerHHMMSSDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveClick(String hhmmssStr) {
                        // 記録時間変更処理
                        changeRecordStartTime(hhmmssStr);
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
    private void setRecordNameListerner(View root) {

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
     * 記録中メモ参照用ボトムシート情報設定
     */
    private void setReferenceStampingMemo(View root) {

        //-----------------------------
        // 記録中メモをリスト表示設定
        //-----------------------------
        RecyclerView rv_stampingMemoList = root.findViewById(R.id.rv_stampingMemoList);
        final StampMemoListAdapter adapter = new StampMemoListAdapter(mStampMemos);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        rv_stampingMemoList.setAdapter(adapter);
        rv_stampingMemoList.setLayoutManager(linearLayoutManager);

        //-----------------------------
        // リストアイテムスワイプ削除設定
        //-----------------------------
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                //-------------------------
                // スワイプされたメモを削除
                //-------------------------
                final int removePos = viewHolder.getAdapterPosition();
                final StampMemoTable removedStampMemo = mStampMemos.get(removePos);
                mStampMemos.remove(removePos);
                adapter.notifyItemRemoved(removePos);

                //-------------------------
                // UNDO確認
                //-------------------------
                showSnackBarUndoRemoveStampMemo(adapter, removePos, removedStampMemo);
            }

            /*
             * 最終的な処理終了時にコールされる
             */
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                //※superをしないと、onSwipで削除したとき、同じデータが非表示になる
                super.clearView(recyclerView, viewHolder);
            }
        });

        // 記録中メモリストにアタッチ
        helper.attachToRecyclerView(rv_stampingMemoList);
    }

    /*
     * 記録開始時間の変更
     */
    private void changeRecordStartTime(String newhhmmssStr) {

        //-----------------------
        // 一番進んでいる記録時間の保持
        //-----------------------
        // 変更直前の記録時間
        String preRecordTime = mtv_recordTime.getText().toString();
        // 進んでいる方の時分秒を取得
        String mostTimeAdvanced = getAdvancedTime(preRecordTime, newhhmmssStr);
        // 現時点の記録時間として保持する
        mRecord.setRecordingTime(mostTimeAdvanced);

        //-----------------------
        // 記録開始システム時間を変更
        //-----------------------
        setRecordStartSystemTimeFromText(newhhmmssStr);
        mIsRenewRecordStartTime = true;

        //-----------------------
        // 記録時間変更
        //-----------------------
        // 入力された時分秒をビューに反映
        mtv_recordTime.setText(newhhmmssStr);
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
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
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

        //----------------------
        // 保存／同期処理
        //----------------------
        // 記録を保存
        String stoppedRecordTime = mtv_recordTime.getText().toString();
        saveRecord(stoppedRecordTime);

        // 共通データ側を記録停止にする
        AppCommonData commonData = (AppCommonData) getActivity().getApplication();
        commonData.setRecordPlayState(RECORD_STOP);

        //----------------------
        // 記録終了時のクリア処理
        //----------------------
        mRecordPlayState = RECORD_STOP;
        mTimeHandler.removeCallbacks(mTimeRunnable);
        // 記録時間を初期化
        String hhssmm = formatHHMMSS(0);
        mtv_recordTime.setText(hhssmm);

        // 記録メモをクリア
        final int stampedMemoNum = mStampMemos.size();
        mStampMemos.clear();

        // アダプタへクリア通知
        RecyclerView rv_stampingMemoList = mtv_delayTime.getRootView().findViewById(R.id.rv_stampingMemoList);
        StampMemoListAdapter adapter = (StampMemoListAdapter)rv_stampingMemoList.getAdapter();
        adapter.notifyItemRangeRemoved( 0, stampedMemoNum );

        //----------------------
        // 制御系を初期状態に
        //----------------------
        // 記録アイコンの表示を記録開始用に変更
        View root = mtv_recordTime.getRootView();
        showRecordControlIcon(root);

        // レコードアニメーションを停止
        ctrlRecordingAnimation(RECORDING_ANIM_STOP);
    }

    /*
     * 記録保存
     *   @para1：記録終了時点の記録時間（hh:mm:ss）
     */
    private void saveRecord(String recordStopTime) {

        View root = mtv_recordTime.getRootView();

        //------------------
        // 記録情報の設定
        //------------------
        // 記録名
        TextView tv_recordName = root.findViewById(R.id.tv_recordName);
        String recordName = tv_recordName.getText().toString();
        mRecord.setName(recordName);

        // 記録時間
        // 進んでいる方の時分秒を取得
        String keepingRecordTime = mRecord.getRecordingTime();
        String mostTimeAdvanced = getAdvancedTime(keepingRecordTime, recordStopTime);
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
                // 完了メッセージ
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
        long second = (long) Math.floor(msec / 1000f);
        long minute = second / 60;

        // 時分秒変換
        long hh = minute / 60;
        long mm = minute % 60;
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
        String recordTimeStr = mtv_recordTime.getText().toString();
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
//        objectAnimator.setDuration(4000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatMode(ValueAnimator.RESTART);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

        return objectAnimator;
    }

    /*
     * レコードアニメーションの制御
     */
    private void ctrlRecordingAnimation(int ctrl) {

        // 制御に応じたアニメーション
        switch (ctrl) {
            case RECORDING_ANIM_START:
                // 開始タイミングでRepeatCountを設定
                mRecordingAnimator.setDuration(4000);
                mRecordingAnimator.start();
                break;

            case RECORDING_ANIM_RESUME:
                mRecordingAnimator.resume();
                break;

            case RECORDING_ANIM_PAUSE:
                mRecordingAnimator.pause();
                break;

            case RECORDING_ANIM_STOP:

                // Durationを０にすることでアニメーションを停止しているため、pauseの状態にあるなら再開させる
                if (mRecordingAnimator.isPaused()) {
                    mRecordingAnimator.resume();
                }

                // Durationを０にすることでアニメーションを停止
                // ※cancel()ではレコードイメージがその時点で止まることになる
                mRecordingAnimator.setDuration(0);

                break;
        }
    }

    /*
     * 記録メモ追加アニメーションの開始
     */
    private void startAddMemoAnimation(int memoColor) {

/*        View root = mtv_delayTime.getRootView();
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
        });*/
    }


    /*
     * 共通データから記録中の記録情報を取得
     *   true :取得あり
     *   false:取得なし
     */
    private boolean getRecordingDataFromCommonData() {

        AppCommonData commonData = (AppCommonData) getActivity().getApplication();

        // 記録状態が停止中なら、取得なし
        mRecordPlayState = commonData.getRecordPlayState();
        if (mRecordPlayState == RECORD_STOP) {
            return false;
        }

        // 記録中データの取得
        mRecord = commonData.getRecord();
        mStampMemos = commonData.getStampMemos();
        mRecordStartSystemTime = commonData.getRecordStartSystemTime();
        mRecordPauseSystemTime = commonData.getRecordPauseSystemTime();
        mIsRenewRecordStartTime = commonData.isRenewRecordStartTime();

        return true;
    }

    /*
     * 記録データを共通データとして保持させる
     */
    private void setRecordingDataAsCommonData() {
        // 記録中でなければ何もしない
        if (mRecordPlayState == RECORD_STOP) {
            return;
        }

        //------------------
        // 共通データに保存
        //------------------
        // 記録時間
        String recordTime = mtv_recordTime.getText().toString();

        // 遅延時間
        String delayTime = mtv_delayTime.getText().toString();

        AppCommonData commonData = (AppCommonData) getActivity().getApplication();
        commonData.tmpSaveRecordData(mRecord, mRecordPlayState, mStampMemos, mRecordStartSystemTime, mRecordPauseSystemTime, recordTime, delayTime, mIsRenewRecordStartTime);
    }

    /*
     * スナックバーの表示
     * 　記録メモ取り消し
     */
    private void showSnackBarUndoStampMemo(final int addPos, String memoName, String playTime) {

        View cl_root = mtv_delayTime.getRootView().findViewById(R.id.cl_root);
        Resources resources = getResources();

        // 記録時間とメモ名をメッセージとして表示
        String message = playTime + "\n" + memoName;

        // UNDO確認メッセージを表示
        Snackbar.make(cl_root, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo_stamp_memo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // UNDOアクション時は、追加されたメモを記録メモリストから削除する
                        mStampMemos.remove(addPos);

                        // アダプタへ追加通知
                        RecyclerView rv_stampingMemoList = mtv_delayTime.getRootView().findViewById(R.id.rv_stampingMemoList);
                        StampMemoListAdapter adapter = (StampMemoListAdapter)rv_stampingMemoList.getAdapter();
                        adapter.notifyItemRemoved( addPos );
                    }
                })
                // レイアウト
                .setBackgroundTint(resources.getColor(R.color.sub))
                .setTextColor(resources.getColor(R.color.main))
                .setActionTextColor(resources.getColor(R.color.accent2))

                .show();
    }

    /*
     * スナックバーの表示
     * 　記録メモの削除取り消し
     */
    private void showSnackBarUndoRemoveStampMemo(StampMemoListAdapter adapter, int removePos, StampMemoTable removedStampMemo) {

        View cl_root = mtv_delayTime.getRootView().findViewById(R.id.cl_root);
        Resources resources = getResources();

        // UNDO確認メッセージを表示
        Snackbar.make(cl_root, R.string.snackbar_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo_remove, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // UNDOアクション時は、削除したメモを元に戻す
                        mStampMemos.add(removePos, removedStampMemo);
                        adapter.notifyItemInserted(removePos);
                    }
                })
                // レイアウト
                .setBackgroundTint(resources.getColor(R.color.sub))
                .setTextColor(resources.getColor(R.color.main))
                .setActionTextColor(resources.getColor(R.color.accent2))

                .show();
    }

    /*
     * 記録メモリストに対して、指定された「打刻時の経過時間」の挿入位置を取得
     *   例）para：「00:20:00」
     *   [0]：「00:10:00」
     *   [1]：「00:30:00」
     *   → この場合、「1」を返す
     */
    public static int getInsertPosition(ArrayList<StampMemoTable> stampMemos, String targetPlayTime) {

        //-------------------------
        // リスト内挿入位置検索
        //-------------------------
        int position = 0;
        for (StampMemoTable stampMemo : stampMemos) {
            // リスト内の時間が、対象の時間よりも後の場合
            String searchPlayTime = stampMemo.getStampingPlayTime();
            if (searchPlayTime.compareTo(targetPlayTime) > 0) {
                return position;
            }
            position++;
        }

        //---------------------------
        // 見つからなければ、終端位置を返す
        //---------------------------
        return position;
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

        // 記録メモリストの記録時間順になる位置へ追加
        int addIndex = getInsertPosition( mStampMemos, playTime );
        mStampMemos.add( addIndex, stampMemo );

        //------------------------
        // 記録中メモのアダプタを更新
        //------------------------
        // アダプタへ追加通知
        RecyclerView rv_stampingMemoList = mtv_delayTime.getRootView().findViewById(R.id.rv_stampingMemoList);
        StampMemoListAdapter adapter = (StampMemoListAdapter)rv_stampingMemoList.getAdapter();
        adapter.notifyItemInserted( addIndex );

        // スクロール位置を追加したアイテムに設定
        rv_stampingMemoList.scrollToPosition(addIndex);

        //--------------------------------
        // メモ追加アニメーション
        //   ※現時点では、snacknar表示のみ
        //--------------------------------
//        startAddMemoAnimation( memoColor );
        /*String message = getString(R.string.toast_stamp_memo) + "\n" + memoName + "\n" + playTime;
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();*/
        showSnackBarUndoStampMemo( addIndex, memoName, playTime );
    }
}
