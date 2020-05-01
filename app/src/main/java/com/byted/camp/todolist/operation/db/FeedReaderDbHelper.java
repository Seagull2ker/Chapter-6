package com.byted.camp.todolist.operation.db;

import static com.byted.camp.todolist.operation.db.FeedReaderContract.SQL_CREATE_ENTRIES;
import static com.byted.camp.todolist.operation.db.FeedReaderContract.SQL_DELETE_ENTRIES;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

/**
 * @author zhongshan
 * 2020-04-19.
 */
public class FeedReaderDbHelper extends SQLiteOpenHelper {//2. 实现一个SQLiteOpenHelper

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "FeedReader.db";


    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(@org.jetbrains.annotations.NotNull SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(@NotNull SQLiteDatabase db, int oldVersion, int newVersion) {//3. 实现数据库升级逻辑
        db.execSQL(SQL_DELETE_ENTRIES);//删掉
        onCreate(db);//调用onCreate
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("");
        }
    }
}

