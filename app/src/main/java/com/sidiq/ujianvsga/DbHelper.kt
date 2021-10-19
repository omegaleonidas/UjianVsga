package com.sidiq.ujianvsga

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    companion object {
        const val   DATABASE_NAME = "pendaftaran.db"
        const val DATABASE_VERSION = 1
        const val TABLE_SQLite = "sqlite"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_NO_HP = "no_hp"
        const val COLUMN_ADDRESS = "lokasi"
        const val COLUMN_SEX = "jenisKelamin"
        const val COLUMN_PHOTO = "photo"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE ${TABLE_SQLite} ("+
                "${COLUMN_ID} INTEGER PRIMARY KEY," +
                "${COLUMN_NAME} TEXT," +
                "${COLUMN_ADDRESS} TEXT," +
                "${COLUMN_NO_HP} TEXT," +
                "${COLUMN_SEX} TEXT," +
                "${COLUMN_PHOTO} TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${TABLE_SQLite}")
    }

}