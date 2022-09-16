package com.example.timingmemo.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "UserMemoTable")
public class UserMemoTable {

    //----------------------------
    // カラム定義
    //----------------------------
    // プライマリーID
    @PrimaryKey(autoGenerate = true)
    private int pid;

    // プライマリーID（UserCategoryTable）
    @ColumnInfo(name = "categoryPid")
    private int categoryPid;

    // メモ名
    @ColumnInfo(name = "name")
    private String name;

    // メモ色
    @ColumnInfo(name = "color")
    private int color;

    //----------------------------
    // 定数
    //----------------------------
    public final static int NO_CATEGORY = -1;

    //----------------------------
    // メソッド
    //----------------------------
    public UserMemoTable(){
        // デフォルト：カテゴリなし
        categoryPid = NO_CATEGORY;
    }

    //----------------------------
    // getter/setter
    //----------------------------
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getCategoryPid() {
        return categoryPid;
    }
    public void setCategoryPid(int categoryPid) {
        this.categoryPid = categoryPid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
}
