package org.huangxk.recycle;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBase extends SQLiteOpenHelper {
    private static final String DB_NAME = "recycle_db";
    private static final int DB_VERSION = 1;
    private static final String KEY_PREFERENCE = "recycle_setting";
    private static final String KEY_FIRST_STORE_TIME = "key_first_store_time";

    public static final String KEY_STATIONID = "station_id";
    public static final int INVALID_STATIONID = -1;
    public static final long INVALID_STORE_TIME = -1;

    private static final String COL_TIMESTAMP = "create_time";
    private static final String COL_USERID = "user_id";
    private static final String COL_USERTYPE = "user_type";
    private static final String COL_USERNAME = "user_name";
    private static final String COL_ANIMALTYPE = "animal_type";
    private static final String COL_ANIMALLENGTH = "animal_length";
    private static final String COL_ANIMALWEIGHT = "animal_weight";
    private static final String COL_ANIMALSNAPSHOT = "animal_snapshot";
    private static final String COL_TRANSFERED = "has_transfered";

    public DataBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_STORE_TABLE_V1_0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void setStationId(int id) {
        SharedPreferences.Editor editor = sContext.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_STATIONID, id);
        editor.commit();
    }

    public int getStationId() {
        SharedPreferences sp = sContext.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE);
        return sp.getInt(KEY_STATIONID, INVALID_STATIONID);
    }

    public long getFirstStoreTime() {
        SharedPreferences sp = sContext.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE);
        return sp.getLong(KEY_FIRST_STORE_TIME, INVALID_STORE_TIME);
    }

    private void setFirstStoreTime(long value) {
        SharedPreferences.Editor editor = sContext.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putLong(KEY_FIRST_STORE_TIME, value);
        editor.commit();
    }

    public boolean saveStoreTask() {
        TaskData task = TaskData.getInstance();
        if (!task.getUserInfo().isValid() || !task.getAnimalInfo().isValid()) return false;
        SQLiteDatabase db = getWritableDatabase();
        String sql = String.format("INSERT INTO taskStore (%s, %s, %s, %s, %s, %s, %s) VALUES(?,?,?,?,?,?,?)",
                COL_USERID, COL_USERTYPE, COL_USERNAME, COL_ANIMALTYPE, COL_ANIMALLENGTH, COL_ANIMALWEIGHT, COL_ANIMALSNAPSHOT);
        db.execSQL(sql, new Object[] {
                task.getUserInfo().mCardNum,
                task.getUserInfo().mUserType,
                task.getUserInfo().mUserName,
                task.getAnimalInfo().mAnimalType,
                task.getAnimalInfo().mLengthMM,
                task.getAnimalInfo().mWeightGRAM,
                task.getAnimalInfo().jpegPicToByte()
        });

        if (getFirstStoreTime() == INVALID_STORE_TIME) {
            setFirstStoreTime(System.currentTimeMillis());
        }
        return true;
    }

    public boolean saveCollectTask() {
        TaskData task = TaskData.getInstance();
        if (!task.getUserInfo().isValid())  return false;
        SQLiteDatabase db = getWritableDatabase();
        String sql = String.format("INSERT INTO taskStore (%s, %s, %s) VALUES(?,?,?)",
                COL_USERID, COL_USERTYPE, COL_USERNAME);
        db.execSQL(sql, new Object[] {
                task.getUserInfo().mCardNum,
                task.getUserInfo().mUserType,
                task.getUserInfo().mUserName,
        });
        setFirstStoreTime(INVALID_STORE_TIME);
        return true;
    }

    public int countData(int flag) {    //0:all 1:transfered 2:untransfered
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT COUNT(" + COL_TRANSFERED + ") FROM taskStore";
        if (flag == 1) sql += " WHERE " + COL_TRANSFERED + " > 0";
        if (flag == 0) sql += " WHERE " + COL_TRANSFERED + " = 0";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        Log.d(DB_NAME, "count = " + cursor.getInt(0));

        return cursor.getInt(0);
    }

    private static final String CREATE_STORE_TABLE_V1_0 =
        "CREATE TABLE taskStore (\n" +
            COL_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
            COL_USERID + " CHAR(128) NOT NULL,\n" +
            COL_USERTYPE + " SMALLINT NOT NULL,\n" +
            COL_USERNAME + " CHAR(32) NOT NULL,\n" +
            COL_ANIMALTYPE + " SMALLINT DEFAULT NULL,\n" +
            COL_ANIMALLENGTH + " INT DEFAULT NULL,\n" +
            COL_ANIMALWEIGHT + " INT DEFAULT NULL,\n" +
            COL_ANIMALSNAPSHOT + " BLOB DEFAULT NULL,\n" +
            COL_TRANSFERED + " SMALLINT DEFAULT 0\n" +
        ");";

    private static DataBase sInstance = null;
    private static Context sContext;
    public static DataBase createInstance(Context context) {
        sContext = context;
        sInstance = new DataBase(sContext, DB_NAME, null, DB_VERSION);
        return sInstance;
    }

    public static DataBase getInstance() {
        return sInstance;
    }


}
