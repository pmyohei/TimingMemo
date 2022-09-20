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

    @Insert
    long insert(RecordTable recordTable);

    @Delete
    void delete(RecordTable recordTable);

    @Query("DELETE FROM recordTable")
    void deleteAll();
}
