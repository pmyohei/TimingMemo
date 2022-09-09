package com.example.timingmemo.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "UserCategoryTable")
public class UserCategoryTable {

    //----------------------------
    // カラム定義
    //----------------------------
    // 主キー
    @PrimaryKey(autoGenerate = true)
    private int pid;

    // カテゴリ名
    @ColumnInfo(name = "name")
    private String name;


    //----------------------------
    // 定数
    //----------------------------

    //----------------------------
    // getter/setter
    //----------------------------
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
