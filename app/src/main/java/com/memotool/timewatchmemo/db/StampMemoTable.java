package com.memotool.timewatchmemo.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "StampMemoTable",
        foreignKeys = { @ForeignKey(
                entity = RecordTable.class,
                parentColumns = "pid",
                childColumns  = "recordPid",
                onDelete = ForeignKey.CASCADE),},
        indices     = { @Index(
                value = {"recordPid"})}
)
public class StampMemoTable implements Comparable<StampMemoTable> {

    //----------------------------
    // カラム定義
    //----------------------------
    // プライマリーID
    @PrimaryKey(autoGenerate = true)
    private int pid;

    // プライマリーID（RecordTable）
    @ColumnInfo(name = "recordPid")
    private int recordPid;

    // メモ名
    @ColumnInfo(name = "memoName")
    private String memoName;

    // メモ色
    @ColumnInfo(name = "memoColor")
    private int memoColor;

    // 打刻時の経過時間（hh:mm:ss）
    @ColumnInfo(name = "stampingPlayTime")
    private String stampingPlayTime;

    // 打刻時の実際の時間（yyyy/mm/dd hh:mm）
    @ColumnInfo(name = "stampingSystemTime")
    private String stampingSystemTime;

    // 遅延時間（mm:ss）
    @ColumnInfo(name = "delayTime")
    private String delayTime;


    //----------------------------
    // getter/setter
    //----------------------------
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getRecordPid() {
        return recordPid;
    }
    public void setRecordPid(int recordPid) {
        this.recordPid = recordPid;
    }

    public String getMemoName() {
        return memoName;
    }
    public void setMemoName(String memoName) {
        this.memoName = memoName;
    }

    public int getMemoColor() {
        return memoColor;
    }
    public void setMemoColor(int memoColor) {
        this.memoColor = memoColor;
    }

    public String getStampingPlayTime() {
        return stampingPlayTime;
    }
    public void setStampingPlayTime(String stampingPlayTime) {
        this.stampingPlayTime = stampingPlayTime;
    }

    public String getStampingSystemTime() {
        return stampingSystemTime;
    }
    public void setStampingSystemTime(String stampingSystemTime) {
        this.stampingSystemTime = stampingSystemTime;
    }

    public String getDelayTime() {
        return delayTime;
    }
    public void setDelayTime(String delayTime) {
        this.delayTime = delayTime;
    }

    //----------------------------
    // インターフェース実装
    //----------------------------
    /*
     * 「打刻時の経過時間」でのソート用
     */
    @Override
    public int compareTo( StampMemoTable stampMemo ){
        // 「打刻時の経過時間」の比較結果を返す
        return this.stampingPlayTime.compareTo( stampMemo.getStampingPlayTime() );
    }


}
