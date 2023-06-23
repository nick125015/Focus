package com.example.focus

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(
    context: Context,
    name: String = database,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = v
) : SQLiteOpenHelper(context, name, factory, version) {
    companion object {
        private const val database = "myDataBase"
        private const val v = 2
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS dateTable(date text , describe text )")
        db.execSQL("CREATE TABLE IF NOT EXISTS myTable(time text , describe text )")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS dateTable")
        db.execSQL("DROP TABLE IF EXISTS myTable")
        onCreate(db)
    }
}