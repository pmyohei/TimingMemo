package com.example.timingmemo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserMemoTableDao {

    @Query("SELECT * FROM userMemoTable")
    List<UserMemoTable> getAll();

    @Insert
    void insert(UserMemoTable taskTable);

    @Delete
    void delete(UserMemoTable taskTable);
}
