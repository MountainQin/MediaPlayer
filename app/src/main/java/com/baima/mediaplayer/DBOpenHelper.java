package com.baima.mediaplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.litepal.LitePal;

public class DBOpenHelper extends SQLiteOpenHelper {

    public  DBOpenHelper(Context context){
        super(context, "sqlite.db",null,
                LitePal.getDatabase().getVersion());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
