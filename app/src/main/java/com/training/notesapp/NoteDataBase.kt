package com.training.notesapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoteDataBase(context: Context) : SQLiteOpenHelper(context, Constant.DATA_BASE_NAME, null, Constant.DATA_BASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val sql = "CREATE TABLE ${Constant.TABLE_NAME} (" +
                " ${Constant.ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${Constant.TITLE} TEXT," +
                " ${Constant.CONTENT} TEXT)"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${Constant.TABLE_NAME}")
        onCreate(db)
    }
}