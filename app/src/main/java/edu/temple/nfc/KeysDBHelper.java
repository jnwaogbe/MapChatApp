package edu.temple.nfc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.Key;

/**
 * Created by Jessica on 4/1/2018.
 */

public class KeysDBHelper extends SQLiteOpenHelper {

    private static final String DB_CREATE_KEY_TABLE =
            "CREATE TABLE " + KeysContract.KeyEntry.TABLE_NAME_PRIMARY +
                    "(" + KeysContract.KeyEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KeysContract.KeyEntry.ALIAS + " TEXT NOT NULL, " +
                    KeysContract.KeyEntry.PUBLIC + " TEXT NOT NULL, " +
                    KeysContract.KeyEntry.PRIVATE + " TEXT NOT NULL);";

    private static final String DB_DELETE_KEY_TABLE =
            "DROP TABLE IF EXISTS " + KeysContract.KeyEntry.TABLE_NAME_PRIMARY;

    private static final String DB_CHECK_KEY_TABLE =
            "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '" + KeysContract.KeyEntry.TABLE_NAME_PRIMARY + "'";

    /**
     * Default constructor for DB helper
     * @param context - the context with which to associate the DB helper
     */
    public KeysDBHelper(Context context) {
        super(context, KeysContract.DB_NAME, null, KeysContract.DB_VERSION);
    }

    /**
     * Creates a new "key" table in the provided DB instance
     * @param database - the DB for which to create a "key" table
     */

    @Override
    public void onCreate(SQLiteDatabase database) {
        createKeyTable(database);
    }

    /**
     * Updates the "key" table in the associated DB instance
     * @param database - the DB for which to update the "key" table
     * @param oldVersion - the old version of the provided DB
     * @param newVersion - the new version of the provided DB
     */

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        recreateKeyTable(database);
    }

    private static void createKeyTable(SQLiteDatabase database) {
        database.execSQL(DB_CREATE_KEY_TABLE);
        Log.d(KeysContract.LOG_TAG, "Created key table with query: \n" + DB_CREATE_KEY_TABLE);
    }

    private static void deleteKeyTable(SQLiteDatabase database) {
        database.execSQL(DB_DELETE_KEY_TABLE);
        Log.d(KeysContract.LOG_TAG, "Deleted key table with query: \n" + DB_DELETE_KEY_TABLE);
    }

    public static void recreateKeyTable(SQLiteDatabase database) {
        deleteKeyTable(database);
        createKeyTable(database);
    }

}