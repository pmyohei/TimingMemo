package com.memotool.timewatchmemo.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "RecordTable")
public class RecordTable {

    //----------------------------
    // カラム定義
    //----------------------------
    // プライマリーID
    @PrimaryKey(autoGenerate = true)
    private int pid;

    // 記録名
    @ColumnInfo(name = "name")
    private String name;

    // メモ開始時間（yyyy/mm/dd hh:mm:ss）
    @ColumnInfo(name = "startRecordingTime")
    private String startRecordingTime;

    // メモ終了時間（yyyy/mm/dd hh:mm:ss）
    @ColumnInfo(name = "endRecordingTime")
    private String endRecordingTime;

    // メモ記録時間（hh:mm:ss）
    @ColumnInfo(name = "recordingTime")
    private String recordingTime;



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

    public String getStartRecordingTime() {
        return startRecordingTime;
    }
    public void setStartRecordingTime(String startRecordingTime) {
        this.startRecordingTime = startRecordingTime;
    }

    public String getEndRecordingTime() {
        return endRecordingTime;
    }
    public void setEndRecordingTime(String endRecordingTime) {
        this.endRecordingTime = endRecordingTime;
    }

    public String getRecordingTime() {
        return recordingTime;
    }
    public void setRecordingTime(String recordingTime) {
        this.recordingTime = recordingTime;
    }
}
