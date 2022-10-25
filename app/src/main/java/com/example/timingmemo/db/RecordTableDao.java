package com.example.timingmemo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecordTableDao {

    @Query("SELECT * FROM recordTable")
    List<RecordTable> getAll();

    // 記録が新しい順で全ての記録を取得
    @Query("SELECT * FROM recordTable ORDER BY startRecordingTime DESC")
    List<RecordTable> getAllNewOrder();

    @Query("SELECT * FROM recordTable WHERE pid=(:pid) ")
    RecordTable get( int pid );

    @Query("UPDATE recordTable SET name=(:recordName) WHERE pid=(:pid) ")
    void updateRecordName( int pid, String recordName );

    @Insert
    long insert(RecordTable recordTable);

    @Delete
    void delete(RecordTable recordTable);

    @Query("DELETE FROM recordTable WHERE pid=(:pid)")
    void delete( int pid );

    @Query("DELETE FROM recordTable")
    void deleteAll();
}
