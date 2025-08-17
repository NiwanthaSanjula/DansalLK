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
    private static final String DB_NAME = "DansalDB";
    private static final int DB_VERSION = 3; // increment version
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_USERS = "users";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "lat REAL NOT NULL, " +
                "lng REAL NOT NULL, " +
                "eventType TEXT NOT NULL, " +
                "category TEXT, " +
                "images TEXT, " +
                "eventDate TEXT, " +
                "startTime TEXT, " +
                "created_at INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            try { db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN eventDate TEXT"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN startTime TEXT"); } catch (Exception ignored) {}
        }
    }

    // ---------------- Events methods ----------------
    public long addEvent(String name, String description, double lat, double lng,
                         String eventType, String category, List<String> images,
                         String eventDate, String startTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", description);
        cv.put("lat", lat);
        cv.put("lng", lng);
        cv.put("eventType", eventType);
        cv.put("category", category);
        if (images != null && !images.isEmpty()) {
            JSONArray ja = new JSONArray();
            for (String s : images) ja.put(s);
            cv.put("images", ja.toString());
        } else {
            cv.putNull("images");
        }
        cv.put("eventDate", eventDate);
        cv.put("startTime", startTime);
        cv.put("created_at", System.currentTimeMillis());
        return db.insert(TABLE_EVENTS, null, cv);
    }

    public List<Event> getEventsByType(String eventType) {
        SQLiteDatabase db = getReadableDatabase();
        List<Event> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_EVENTS + " WHERE eventType = ? ORDER BY created_at DESC",
                new String[]{eventType});
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public List<Event> getDansalByCategory(String category) {
        SQLiteDatabase db = getReadableDatabase();
        List<Event> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_EVENTS + " WHERE eventType = ? AND (? IS NULL OR category = ?) ORDER BY created_at DESC",
                new String[]{"dansal", category, category});
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public List<Event> getDansalOtherCategories(List<String> predefinedCats) {
        SQLiteDatabase db = getReadableDatabase();
        List<Event> list = new ArrayList<>();

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < predefinedCats.size(); i++) {
            placeholders.append("?");
            if (i < predefinedCats.size() - 1) placeholders.append(",");
        }

        String sql = "SELECT * FROM " + TABLE_EVENTS +
                " WHERE eventType = ? AND (category NOT IN (" + placeholders + ") OR category IS NULL) " +
                "ORDER BY created_at DESC";

        String[] args = new String[predefinedCats.size() + 1];
        args[0] = "dansal";
        for (int i = 0; i < predefinedCats.size(); i++) args[i + 1] = predefinedCats.get(i);

        Cursor c = db.rawQuery(sql, args);
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    private Event fromCursor(Cursor c) {
        Event e = new Event();
        e.id = c.getInt(c.getColumnIndexOrThrow("id"));
        e.name = c.getString(c.getColumnIndexOrThrow("name"));
        e.description = c.getString(c.getColumnIndexOrThrow("description"));
        e.lat = c.getDouble(c.getColumnIndexOrThrow("lat"));
        e.lng = c.getDouble(c.getColumnIndexOrThrow("lng"));
        e.eventType = c.getString(c.getColumnIndexOrThrow("eventType"));
        e.category = c.getString(c.getColumnIndexOrThrow("category"));
        e.eventDate = c.getString(c.getColumnIndexOrThrow("eventDate"));
        e.startTime = c.getString(c.getColumnIndexOrThrow("startTime"));

        String imagesJson = c.getString(c.getColumnIndexOrThrow("images"));
        e.images = new ArrayList<>();
        if (imagesJson != null && !imagesJson.isEmpty()) {
            try {
                JSONArray arr = new JSONArray(imagesJson);
                for (int i = 0; i < arr.length(); i++) e.images.add(arr.getString(i));
            } catch (JSONException ignored) {}
        }
        e.createdAt = c.getLong(c.getColumnIndexOrThrow("created_at"));
        return e;
    }

    public Event getEventById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_EVENTS + " WHERE id = ?", new String[]{String.valueOf(id)});
        Event event = null;
        if (c.moveToFirst()) event = fromCursor(c);
        c.close();
        return event;
    }

    // ---------------- Users methods ----------------
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username = ? AND password = ?", new String[]{username, password});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public long addUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        return db.insert(TABLE_USERS, null, cv);
    }

    // ---------------- Event model ----------------
    public static class Event {
        public int id;
        public String name;
        public String description;
        public double lat, lng;
        public String location;
        public String startTime;
        public String endTime;
        public String eventType;
        public String category;
        public String eventDate;
        public List<String> images;
        public long createdAt;
    }
}
