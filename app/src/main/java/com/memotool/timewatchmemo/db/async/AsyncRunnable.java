package com.memotool.timewatchmemo.db.async;

import android.os.Handler;
import android.os.Looper;

public class AsyncRunnable implements Runnable {

    Handler handler = new Handler(Looper.getMainLooper());

    /*
     * バックグラウンド処理
     */
    @Override
    public void run() {

        //後処理
        handler.post(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
}
