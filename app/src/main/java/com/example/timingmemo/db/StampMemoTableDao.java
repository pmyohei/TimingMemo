package com.example.timingmemo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StampMemoTableDao {

    @Query("SELECT * FROM stampMemoTable")
    List<StampMemoTable> getAll();

    @Query("SELECT * FROM stampMemoTable WHERE recordPid=(:recordPid)")
    List<StampMemoTable> getStampMemos( int recordPid );

    @Insert
    void insert(StampMemoTable stampMemoTable);

    @Delete
    void delete(StampMemoTable stampMemoTable);

    @Query("DELETE FROM stampMemoTable")
    void deleteAll();
}
