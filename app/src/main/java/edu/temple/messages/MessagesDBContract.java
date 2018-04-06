package edu.temple.messages;

import android.provider.BaseColumns;

/**
 * Created by Jessica on 4/6/2018.
 */

public class MessagesDBContract {

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MessagesEntry.TABLE_NAME + " (" +
                    MessagesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_PARTNER + TEXT_TYPE + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_TO + TEXT_TYPE + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_FROM + TEXT_TYPE + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_MESSAGE + TEXT_TYPE +
                    ")";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + MessagesEntry.TABLE_NAME;

    public static abstract class MessagesEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_PARTNER = "partner";
        public static final String COLUMN_NAME_TO = "_to";
        public static final String COLUMN_NAME_FROM = "_from";
        public static final String COLUMN_NAME_MESSAGE = "message";
    }
}
