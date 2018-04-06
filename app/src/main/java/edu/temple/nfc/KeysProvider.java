package edu.temple.nfc;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

/**
 * Created by Jessica on 4/1/2018.
 */

public class KeysProvider extends ContentProvider {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
        private static HashMap<String, String> KEY_PROJ_MAP;
        private SQLiteDatabase db;

        // --------------------------------------------------------------------------------------
        // --------------------------------------------------------------------------------------

        private static final int KEY_TABLE = 99999;
        private static final int KEY_LIST = 1;
        private static final int KEY_ID = 2;
        private static final UriMatcher URI_MATCHER;

        static {
            URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
            URI_MATCHER.addURI(KeysContract.AUTHORITY, null, KEY_TABLE);
            URI_MATCHER.addURI(KeysContract.AUTHORITY, KeysContract.KeyEntry.TABLE_NAME_PRIMARY, KEY_LIST);
            URI_MATCHER.addURI(KeysContract.AUTHORITY, KeysContract.KeyEntry.TABLE_NAME_PRIMARY + "/#", KEY_ID);
        }

        // --------------------------------------------------------------------------------------
        // --------------------------------------------------------------------------------------

        /**
         * Instantiate the provider data source from the associated DB helper
         *
         * @return boolean flag indicating whether data source initialization was successful
         */
        @Override
        public boolean onCreate() {
            Context context = getContext();
            KeysDBHelper dbHelper = new KeysDBHelper(context);
            db = dbHelper.getWritableDatabase();
            return ((db == null) ? false : true);
        }

        /**
         * Insert a new record into the DB "key" table with the associated values
         *
         * @param uri    - URI of the DB into which to insert the new key pair
         * @param values - properties of the new key pair to insert
         * @return updated URI of the new key pair record
         * @throws SQLException if DB insertion operation fails
         */
        @Override
        public Uri insert(Uri uri, ContentValues values) {
            // attempt to insert new record with new public / private key values
            long newKeyID;
            String username;
            try {
                username = values.getAsString(KeysContract.KeyEntry.ALIAS);
                newKeyID = db.insert(KeysContract.KeyEntry.TABLE_NAME_PRIMARY, "", generateKeyPair(values));
            } catch (NoSuchAlgorithmException ex) {
                throw new SQLException("ALGORITHM ERROR.  Could not generate public private keys for URI: " + uri);
            } catch (NoSuchProviderException ex) {
                throw new SQLException("PROVIDER ERROR.  Could not generate public private keys for URI: " + uri);
            }

            // check to see if record insertion completed successfully
            if (newKeyID > 0) {
                Uri newUri = ContentUris.withAppendedId(KeysContract.KeyEntry.CONTENT_URI, newKeyID);
                getContext().getContentResolver().notifyChange(newUri, null);
                return newUri;
            }

            // if we get here, it means the DB insertion failed ... throw exception!
            else throw new SQLException("DB record insertion into URI: " + uri + " failed!");
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(KeysContract.KeyEntry.TABLE_NAME_PRIMARY);

            switch (URI_MATCHER.match(uri)) {
                case KEY_LIST:
                    qb.setProjectionMap(KEY_PROJ_MAP);
                    Log.d(KeysContract.LOG_TAG, "Getting PPK list for query URI: " + uri);
                    break;
                case KEY_ID:
                    String id = uri.getPathSegments().get(1);
                    qb.appendWhere(String.format("%s=%s", KeysContract.KeyEntry._ID, id));
                    Log.d(KeysContract.LOG_TAG, "Getting single PPK with ID: " + id + ", for query URI: " + uri);
                    break;
                default:
                    Log.d(KeysContract.LOG_TAG, "Could not match URI: " + uri);
                    return null;
            }

            if (sortOrder == null || sortOrder == "")
                sortOrder = KeysContract.KeyEntry.SORT_ORDER_DEFAULT;

            Log.d(KeysContract.LOG_TAG, "Preparing to perform DB query with: "
                    + "\n... projection: " + join(projection)
                    + "\n... selection: " + selection
                    + "\n... selection args: " + join(selectionArgs)
                    + "\n... sort order: " + sortOrder);

            Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            int count;
            switch (URI_MATCHER.match(uri)) {
                case KEY_LIST:
                    count = db.delete(KeysContract.KeyEntry.TABLE_NAME_PRIMARY, selection, selectionArgs);
                    break;
                case KEY_ID:
                    String id = uri.getPathSegments().get(1);
                    count = db.delete(KeysContract.KeyEntry.TABLE_NAME_PRIMARY, String.format("%s=%s", KeysContract.KeyEntry._ID, id)
                            + (!TextUtils.isEmpty(selection) ? String.format(" AND (%s)", selection) : ""), selectionArgs);
                    break;
                case KEY_TABLE:
                    KeysDBHelper.recreateKeyTable(db);
                    count = 0;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            int count;
            switch (URI_MATCHER.match(uri)) {
                case KEY_LIST:
                    count = db.update(KeysContract.KeyEntry.TABLE_NAME_PRIMARY, values, selection, selectionArgs);
                    break;
                case KEY_ID:
                    count = db.update(KeysContract.KeyEntry.TABLE_NAME_PRIMARY, values,
                            String.format("%s=%s", KeysContract.KeyEntry._ID, uri.getPathSegments().get(1))
                                    + (!TextUtils.isEmpty(selection) ? String.format(" AND (%s)", selection) : ""), selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }

        @Override
        public String getType(Uri uri) {
            switch (URI_MATCHER.match(uri)) {
                case KEY_LIST:
                    return KeysContract.KeyEntry.CONTENT_LIST_TYPE;
                case KEY_ID:
                    return KeysContract.KeyEntry.CONTENT_ITEM_TYPE;
                default:
                    return null;
            }
        }

        private ContentValues generateKeyPair(ContentValues values) throws NoSuchAlgorithmException, NoSuchProviderException {
            String publicKey, privateKey = "";

            if (!values.containsKey(KeysContract.KeyEntry.PUBLIC)) {
                KeyPair newKeyPair = RSA.generateKeyPair();
                publicKey = RSA.encodeByteArray(newKeyPair.getPublic().getEncoded());
                privateKey = RSA.encodeByteArray(newKeyPair.getPrivate().getEncoded());
            } else {
                publicKey = values.getAsString(KeysContract.KeyEntry.PUBLIC);
            }

            ContentValues newValues = new ContentValues();
            newValues.put(KeysContract.KeyEntry.ALIAS, values.getAsString(KeysContract.KeyEntry.ALIAS));
            newValues.put(KeysContract.KeyEntry.PUBLIC, publicKey);
            newValues.put(KeysContract.KeyEntry.PRIVATE, privateKey);
            return newValues;
        }

    private void saveKeyToSharedPreferences(String privateKey, String publicKey) {
        editor = sharedPreferences.edit();
        sharedPreferences = getContext().getSharedPreferences("Key_Info", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("PublicKey", publicKey).apply();
        editor.putString("PrivateKey", privateKey).apply();
    }

        private String join(String[] array) {
            if (array == null) return "null";
            String out = "";
            for (int i = 0; i < array.length; i++) {
                out += array[i];
                if (i < array.length - 1) out += ", ";
            }
            return out;
        }

    }
