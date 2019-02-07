package com.studio.millionares.barberbooker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

public class AllSaloonsBasicDetailsDatabase extends SQLiteOpenHelper {

    private final static String TAG = "allSaloonsBasicDetailsDatabase";

    private final static String DATABASE_NAME = "AllSaloonsBasicDetailsDatabase";
    private final static int DATABASE_VERSION = 1;

    private final static String SALONS_TABLE = "Salons";
    private final static String SALONS_TABLE_ID = "Id";
    private final static String SALONS_TABLE_SALON_NAME = "SalonName";
    private final static String SALONS_TABLE_LATITUDE = "LatitudeCd";
    private final static String SALONS_TABLE_LONGITUDE = "LongitudeCd";
    private final static String SALONS_TABLE_IMAGE = "ProfileImage";
    private final static String SALONS_TABLE_CITY = "City";
    private final static String SALONS_TABLE_ADDRESS = "Address";

    public AllSaloonsBasicDetailsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSalonsTable = "CREATE TABLE " + SALONS_TABLE +
                " (" + SALONS_TABLE_ID + " INTEGER PRIMARY KEY, " +
                SALONS_TABLE_SALON_NAME + " TEXT, " +
                SALONS_TABLE_IMAGE + " BLOB, " +
                SALONS_TABLE_CITY + " TEXT, " +
                SALONS_TABLE_ADDRESS + " TEXT, " +
                SALONS_TABLE_LATITUDE + " TEXT, " +
                SALONS_TABLE_LONGITUDE + " TEXT)";

        db.execSQL(createSalonsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insertSalonData(HashMap<String, String> rowValues){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        for(String key : rowValues.keySet()){
            contentValues.put(key, rowValues.get(key));
        }

        long result = db.insert(SALONS_TABLE, null, contentValues);
        boolean isAdded = result == -1 ? false : true;

        return isAdded;
    }

    public boolean updateSalonData(HashMap<String, String> rowValues){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        for(String key : rowValues.keySet()){
            contentValues.put(key, rowValues.get(key));
        }

        long result = db.replace(SALONS_TABLE, null, contentValues);
        boolean isReplaced = result == -1 ? false : true;

        return isReplaced;
    }

    public Cursor getAllSalonData(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + SALONS_TABLE, null);
        return result;
    }
}
