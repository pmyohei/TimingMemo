package com.example.timingmemo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserMemoTableDao {

    @Query("SELECT * FROM userMemoTable")
    List<UserMemoTable> getAll();

    @Query("SELECT * FROM userMemoTable WHERE pid=(:pid)")
    UserMemoTable getUserMemo( int pid );

    @Update
    void update(UserMemoTable userMemo);

    @Insert
    void insert(UserMemoTable taskTable);

    @Query("DELETE FROM userMemoTable")
    void deleteAll();

    @Query("DELETE FROM userMemoTable WHERE pid=(:pid)")
    void delete( int pid );

    @Delete
    void delete(UserMemoTable taskTable);
}
