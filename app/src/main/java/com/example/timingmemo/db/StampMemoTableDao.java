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

    @Insert
    void insert(StampMemoTable taskTable);

    @Delete
    void delete(StampMemoTable taskTable);
}
