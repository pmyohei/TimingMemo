package com.memotool.timewatchmemo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserCategoryTableDao {

    @Query("SELECT * FROM userCategoryTable")
    List<UserCategoryTable> getAll();

    @Query("SELECT * FROM userCategoryTable WHERE pid=(:pid)")
    UserCategoryTable getUserCategory( int pid );

    @Update
    int update(UserCategoryTable userCategory);

    @Insert
    long insert(UserCategoryTable taskTable);

    @Query("DELETE FROM userCategoryTable")
    void deleteAll();

    @Query("DELETE FROM userCategoryTable WHERE pid=(:pid)")
    void delete( int pid );

    @Delete
    void delete(UserCategoryTable taskTable);
}
