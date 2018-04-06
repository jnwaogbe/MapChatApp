package edu.temple.nfc;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Base64;

/**
 * Created by Jessica on 4/1/2018.
 */

public class KeysContract {
    // Constants
    public static final String LOG_TAG = "EncryptedNFC";
    public static final String PACKAGE_NAME = "edu.temple.nfc";

    public static final String CONTENT_PREFIX = "content://";

    public static final String DB_NAME = "CIS4515_DB";
    public static final int DB_VERSION = 1;

    public static final int DEFAULT_ENCODING = Base64.DEFAULT;
    public static final ENCRYPTION_SCHEME DEFAULT_ENCRYPTION_SCHEME = ENCRYPTION_SCHEME.RSA;
    public enum ENCRYPTION_SCHEME { RSA, SHA256 }

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String CHARSET_UTF8 = "UTF-8";

    // Contract

    public static final String AUTHORITY = (PACKAGE_NAME + ".KeysProvider");
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_PREFIX + AUTHORITY);


    public static final class KeyEntry implements BaseColumns {
        public static final String TABLE_NAME_PRIMARY = "keys";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(KeysContract.CONTENT_URI, TABLE_NAME_PRIMARY);

        public static final String ID = "_id";
        public static final String ALIAS = "alias";
        public static final String PUBLIC = "public";
        public static final String PRIVATE = "private";

        public static final String SORT_ORDER_DEFAULT = (ALIAS + " ASC");

        private static final String CONTENT_GROUP_NAME = ("/" + PACKAGE_NAME + "_" + TABLE_NAME_PRIMARY);
        public static final String CONTENT_LIST_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_GROUP_NAME);
        public static final String CONTENT_ITEM_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_GROUP_NAME);

        public static final String[] PROJECTION_ALL = { BaseColumns._ID, ALIAS, PUBLIC, PRIVATE };
    }

    public static KeyPair getKeyPair(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(KeyEntry.ID);
        int aliasIndex = cursor.getColumnIndex(KeyEntry.ALIAS);
        int publicIndex = cursor.getColumnIndex(KeyEntry.PUBLIC);
        int privateIndex = cursor.getColumnIndex(KeyEntry.PRIVATE);

        KeyPair keyPair = new KeyPair();
        keyPair.setID(cursor.getInt(idIndex));
        keyPair.setAlias(cursor.getString(aliasIndex));
        keyPair.setPublicKey(cursor.getString(publicIndex));
        keyPair.setPrivateKey(cursor.getString(privateIndex));

        return keyPair;
    }

    public static class KeyPair {
        private int ID;
        private String alias;
        private String publicKey;
        private String privateKey;

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
    }

    //public static final int DEFAULT_ENCODING = DEFAULT_ENCODING;
    //public static final ENCRYPTION_SCHEME DEFAULT_ENCRYPTION_SCHEME = DEFAULT_ENCRYPTION_SCHEME;
}
