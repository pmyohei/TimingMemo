package com.example.timingmemo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StampMemoTableDao {

    @Query("SELECT * FROM stampMemoTable")
    List<StampMemoTable> getAll();

    @Query("SELECT * FROM stampMemoTable WHERE recordPid=(:recordPid)")
    List<StampMemoTable> getStampMemosLinkedRecord(int recordPid );

    @Query("SELECT * FROM stampMemoTable WHERE pid=(:pid)")
    StampMemoTable getStampMemo(int pid );

    @Insert
    long insert(StampMemoTable stampMemoTable);

    @Update
    void update(StampMemoTable stampMemoTable);

    @Delete
    void delete(StampMemoTable stampMemoTable);

    @Query("DELETE FROM stampMemoTable WHERE pid=(:pid)")
    void delete( int pid );

    @Query("DELETE FROM stampMemoTable")
    void deleteAll();
}
