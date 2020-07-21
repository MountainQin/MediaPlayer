package com.baima.mediaplayer;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.litepal.LitePal;
import org.litepal.LitePalBase;
import org.litepal.crud.LitePalSupport;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initDatabaseVersion();
    }

    private void initDatabaseVersion() {
        SQLiteDatabase database = LitePal.getDatabase();
        DBOpenHelper dbOpenHelper = new DBOpenHelper(this);
        SQLiteDatabase readableDatabase = dbOpenHelper.getReadableDatabase();

        database.close();
        readableDatabase.close();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
