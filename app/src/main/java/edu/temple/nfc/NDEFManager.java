package edu.temple.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import edu.temple.mapchat.activities.NFCSenderActivity;

/**
 * Created by Jessica on 4/1/2018.
 */

public class NDEFManager {

    private Activity currentActivity;
    private String defaultDataType;
    private String defaultCharset;
    private NfcAdapter mAdapter;

    public NDEFManager(Activity activity, String dataType, String charset) {
        currentActivity = activity;
        defaultDataType = dataType;
        defaultCharset = charset;
    }

    public boolean initAdapter() {
        mAdapter = NfcAdapter.getDefaultAdapter(currentActivity);
        if (mAdapter == null) {
            Log.e(KeysContract.LOG_TAG, "Device does not support NFC!");
            return false;
        }

        if (!mAdapter.isEnabled()) {
            Log.e(KeysContract.LOG_TAG, "Device supports NFC, but feature is not enabled!");
            return false;
        }

        Log.i(KeysContract.LOG_TAG, "All set!  Preparing to receive NFC transmission...");
        return true;
    }

    public void handleNDEFIntent(Intent intent, TextView displayText, ContentResolver contentResolver) {
        // Outputting spacer message to log for easy locating
        Log.i(KeysContract.LOG_TAG, "\n... ... ... ... ...\n");

        String action = intent.getAction();
        Log.i(KeysContract.LOG_TAG, "Attempting to resolve NDEF intent for action: " + action
                + " with current activity: " + currentActivity.getClass().getSimpleName());

        if (isNdefIntent(intent)) {
            String pk = "", name = "", encryptedMessage = "";
            Parcelable[] receivedArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (receivedArray != null) {
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                for (NdefRecord record : receivedMessage.getRecords()) {
                    //if (record == receivedMessage.)
                    String payload = (new String(record.getPayload())).trim();
                    if (!payload.equals(currentActivity.getPackageName())) {
                        // double check the payload contents
                        Log.i(KeysContract.LOG_TAG, "Processing new payload record: " + payload);

                        // check to see if this is an acceptable PEM file
                        if (RSA.isPEM(payload)) {
                            try {
                                pk = RSA.convertPEMToKeyString(payload);
                                Log.i(KeysContract.LOG_TAG, "Successfully converted NFC public key!");
                            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        } else if (!isGooglePlayURL(payload)) {
                            name = payload;
                            displayText.setText(payload);
                        }
                    }
                }

                // only proceed with decryption if we have received both components
                if (!pk.isEmpty() && !name.isEmpty()) {
                    addToContentProvider(contentResolver, name, pk);
                    Log.i(KeysContract.LOG_TAG, "Successfully received partner name and public key!");
                }

            } else
                Log.e(KeysContract.LOG_TAG, "Could not read NDEF message ... empty parcel array.");
        } else Log.e(KeysContract.LOG_TAG, "Could not match intent action type: " + action);

        // Outputting spacer message to log for easy locating
        Log.i(KeysContract.LOG_TAG, "\n... ... ... ... ...\n");
    }

    public void pushNDEFMessage(String... messages) {
        // populate list of records with messages to transmit
        NdefRecord[] records = new NdefRecord[messages.length + 1];
        for (int i = 0; i < messages.length; i++) {
            records[i] = NdefRecord.createMime(defaultCharset, messages[i].trim().getBytes());
            Log.i(KeysContract.LOG_TAG, "Adding record to message:\n" + new String(records[i].getPayload()));
        }

        // Adding an application record provides a stronger certainty that our app will be the
        // one to handle the corresponding NDEF intent
        // https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#aar
        records[messages.length] = NdefRecord.createApplicationRecord(currentActivity.getPackageName());

        // create new NDEF message, and push at next opportunity
        Log.i(KeysContract.LOG_TAG, "Pushing new NDEF message for activity: " + currentActivity.getClass().getSimpleName());
        mAdapter.setNdefPushMessage(new NdefMessage(records), currentActivity);
    }

    public void initForegroundDispatch() {
        Intent intent = new Intent(currentActivity, currentActivity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(currentActivity, 0, intent, 0);

        // Same filter as in our manifest.
        IntentFilter[] filters = new IntentFilter[1];
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        try {
            filters[0].addDataType(defaultDataType);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(KeysContract.LOG_TAG, "Could not set up new intent filter with MIME type: "
                    + defaultDataType, e);
        }

        // Same tech list as in XML file
        String[][] techList = new String[][]{
                {android.nfc.tech.Ndef.class.getName()},
                {android.nfc.tech.NdefFormatable.class.getName()}};
        mAdapter.enableForegroundDispatch(currentActivity, pendingIntent, filters, techList);
    }

    public void stopForegroundDispatch() {
        mAdapter.disableForegroundDispatch(currentActivity);
    }

    private boolean isNdefIntent(Intent intent) {
        String action = intent.getAction();
        return (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) ||
                action.equals(NfcAdapter.ACTION_TECH_DISCOVERED));
    }

    private boolean isGooglePlayURL(String data) {
        String url = "play.google";
        if (data.startsWith(url)) {
            return true;
        }
        return false;
    }

    private void addToContentProvider(ContentResolver contentResolver, String partnerName, String data) {
        ContentValues values = new ContentValues();
        values.put(KeysContract.KeyEntry.ALIAS, partnerName);
        values.put(KeysContract.KeyEntry.PUBLIC, data);
        contentResolver.insert(KeysContract.CONTENT_URI, values);
    }
}
