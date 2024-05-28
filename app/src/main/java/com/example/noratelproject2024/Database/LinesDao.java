package com.example.noratelproject2024.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.noratelproject2024.Models.Lines;

import java.util.List;
@Dao
public interface LinesDao {
    @Query("SELECT * FROM lines")
    List<Lines> getAll();

    @Delete
    void delete(Lines lines);

    // Upsert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Lines lines);

    @Query("SELECT * FROM lines WHERE userName = :userName")
    List<Lines> getAllFromUser(String userName);
}
