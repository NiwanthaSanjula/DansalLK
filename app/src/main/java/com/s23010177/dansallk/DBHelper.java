package com.s23010177.dansallk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "DansalaDB";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS dansala_events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, location TEXT, date TEXT, start_time TEXT, end_time TEXT, " +
                "description TEXT, latitude REAL, longitude REAL, images TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS dansala_events");
        onCreate(db);
    }

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=?", new String[]{username});
        if (cursor.getCount() > 0) {
            cursor.close();
            return false; // Username already exists
        }
        cursor.close();

        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", new String[]{username, password});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    // Insert Dansala with images
    public boolean insertDansala(String name, String location, String date, String startTime, String endTime,
                                 String description, double latitude, double longitude, List<String> images) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("location", location);
        values.put("date", date);
        values.put("start_time", startTime);
        values.put("end_time", endTime);
        values.put("description", description);
        values.put("latitude", latitude);
        values.put("longitude", longitude);

        // Convert images list to JSON string
        if (images != null && !images.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String img : images) {
                jsonArray.put(img);
            }
            values.put("images", jsonArray.toString());
        } else {
            values.put("images", "[]"); // empty JSON array if no images
        }

        long result = db.insert("dansala_events", null, values);
        return result != -1;
    }

    // Dansala data model
    public static class Dansala {
        public int id;
        public String name;
        public String location;
        public String date;
        public String startTime;
        public String endTime;
        public String description;
        public double latitude;
        public double longitude;
        public List<String> images = new ArrayList<>();
    }

    // Get all Dansalas
    public List<Dansala> getAllDansalas() {
        List<Dansala> dansalaList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM dansala_events", null);

        if (cursor.moveToFirst()) {
            do {
                Dansala d = new Dansala();
                d.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                d.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                d.location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                d.date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                d.startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                d.endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));
                d.description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                d.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                d.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));

                // Parse images JSON string
                String imagesJson = cursor.getString(cursor.getColumnIndexOrThrow("images"));
                if (imagesJson != null && !imagesJson.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(imagesJson);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            d.images.add(jsonArray.getString(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                dansalaList.add(d);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dansalaList;
    }

    // Get Dansala by ID
    public Dansala getDansalaById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM dansala_events WHERE id = ?", new String[]{String.valueOf(id)});
        Dansala d = null;

        if (cursor.moveToFirst()) {
            d = new Dansala();
            d.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            d.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            d.location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            d.date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            d.startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
            d.endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));
            d.description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            d.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
            d.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));

            // Parse images JSON string
            String imagesJson = cursor.getString(cursor.getColumnIndexOrThrow("images"));
            if (imagesJson != null && !imagesJson.isEmpty()) {
                try {
                    JSONArray jsonArray = new JSONArray(imagesJson);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        d.images.add(jsonArray.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        cursor.close();
        return d;
    }
}
