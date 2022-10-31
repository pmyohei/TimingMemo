package com.memotool.timewatchmemo.db;

import android.content.Context;

import androidx.room.Room;

public class AppDatabaseManager {
    private static AppDatabase instance = null;

    /*
     * インスタンス取得
     */
    public static AppDatabase getInstance(Context context) {
        if (instance != null) {
            // インスタンスを生成済みなら、それを返す
            return instance;
        }

        // Roomクラスからインスタンスを生成
        instance = Room.databaseBuilder(context, AppDatabase.class, "database-memo").build();
        return instance;
    }
}
