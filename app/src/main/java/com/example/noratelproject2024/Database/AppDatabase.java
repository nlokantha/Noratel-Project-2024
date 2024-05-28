package com.example.noratelproject2024.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.noratelproject2024.Models.Lines;

@Database(entities = {Lines.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LinesDao linesDao();
}
