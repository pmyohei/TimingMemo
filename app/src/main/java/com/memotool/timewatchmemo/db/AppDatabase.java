package com.memotool.timewatchmemo.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;


/*
 * Database定義
 */
@Database(
        version = 2,
        entities = {
                UserMemoTable.class,        // ユーザー登録メモテーブル
                UserCategoryTable.class,    // ユーザー登録カテゴリテーブル
                RecordTable.class,          // 記録テーブル
                StampMemoTable.class,       // 打刻メモテーブル
        },
        exportSchema = true
/*        autoMigrations = {
                @AutoMigration(from = 1, to = 2)
        }*/
)
public abstract class AppDatabase extends RoomDatabase {

    //DAO
    public abstract UserMemoTableDao        daoUserMemoTable();         // ユーザー登録メモテーブル
    public abstract UserCategoryTableDao    daoUserCategoryTable();     // ユーザー登録カテゴリテーブル
    public abstract RecordTableDao          daoRecordTable();           // 記録テーブル
    public abstract StampMemoTableDao       daoStampMemoTable();        // 打刻メモテーブル

}
