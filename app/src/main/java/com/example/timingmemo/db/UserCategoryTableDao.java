package com.example.timingmemo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserCategoryTableDao {

    @Query("SELECT * FROM userCategoryTable")
    List<UserCategoryTable> getAll();

    @Insert
    void insert(UserCategoryTable taskTable);

    @Query("DELETE FROM userCategoryTable")
    void deleteAll();

    @Delete
    void delete(UserCategoryTable taskTable);
}
