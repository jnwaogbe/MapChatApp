package edu.temple.mapchat.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import edu.temple.mapchat.R;
import edu.temple.nfc.KeysContract;
import edu.temple.nfc.KeysSpinnerAdapter;
import edu.temple.nfc.NDEFManager;
import edu.temple.nfc.RSA;

/**
 * Created by Jessica on 4/2/2018.
 */

public class NFCSenderActivity extends AppCompatActivity {
    public static final Uri CONTENT_URI = KeysContract.KeyEntry.CONTENT_URI;
    private Spinner keySpinner;
    private NDEFManager ndefManager;
    private Intent ndefIntent;
    private TextView partnerName;
    private String thisUser;

    private Button generateButton;
    private Button clearButton;

    private ContentResolver contentResolver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsa);

        keySpinner = findViewById(R.id.keySpinner);
        generateButton = findViewById(R.id.generateButton);
        clearButton = findViewById(R.id.clearButton);

        thisUser = getIntent().getStringExtra("Username_Info");
        contentResolver = getContentResolver();

        String[] projection = KeysContract.KeyEntry.PROJECTION_ALL;
        final Cursor cursor = contentResolver.query(CONTENT_URI, projection, null, null, null);
        refreshSpinnerView(contentResolver);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] projection = new String[]{KeysContract.KeyEntry.ALIAS};
                String selection = KeysContract.KeyEntry.ALIAS + " = '" + thisUser + "'";

                final Cursor cursor = contentResolver.query(CONTENT_URI, projection, selection, null, null);

                if (cursor != null) {
                    if (cursor.getCount() == 0) {
                        addToContentProvider(contentResolver, thisUser);
                        refreshSpinnerView(contentResolver);

                        Toast.makeText(getApplicationContext(), "New keys have been generated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NFCSenderActivity.this, "You already generated keys", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NFCSenderActivity.this, "Error: null.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentResolver.delete(CONTENT_URI, null, null);
                refreshSpinnerView(contentResolver);
                Toast.makeText(getApplicationContext(), "All keys have been deleted!", Toast.LENGTH_SHORT).show();
            }
        });

        ndefManager = new NDEFManager(NFCSenderActivity.this, KeysContract.MIME_TEXT_PLAIN, KeysContract.CHARSET_UTF8);
        if (ndefManager.initAdapter()) {
            partnerName = findViewById(R.id.partnerName);
            final Button sendButton = findViewById(R.id.send);

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KeysContract.KeyPair ownKey = getSelectedKey();

                    if (ownKey != null) {
                        String pubKeyString = ownKey.getPublicKey();
                        String pubKeyPEM = RSA.convertStringToPEM(pubKeyString);
                        ndefManager.pushNDEFMessage(thisUser, pubKeyPEM);
                    }
                }
            });
        } else {
            finish();
        }
    }

    private void refreshSpinnerView(ContentResolver cr) {
        String[] projection = KeysContract.KeyEntry.PROJECTION_ALL;
        Cursor cursor = cr.query(CONTENT_URI, projection, null, null, null);
        keySpinner.setAdapter(new KeysSpinnerAdapter(this, cursor));
    }

    protected KeysContract.KeyPair getSelectedKey() {
        keySpinner = findViewById(R.id.keySpinner);
        return (KeysContract.KeyPair) keySpinner.getSelectedItem();
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        partnerName = findViewById(R.id.partnerName);
        ndefManager.handleNDEFIntent(intent, partnerName, contentResolver);
        refreshSpinnerView(contentResolver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ndefManager.initForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ndefManager.stopForegroundDispatch();
    }

    public static void addToContentProvider(ContentResolver contentResolver, String data) {
        ContentValues values = new ContentValues();
        values.put(KeysContract.KeyEntry.ALIAS, data);
        contentResolver.insert(CONTENT_URI, values);
    }
}
