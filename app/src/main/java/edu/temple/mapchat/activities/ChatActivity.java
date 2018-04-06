package edu.temple.mapchat.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import edu.temple.mapchat.ChatMessage;
import edu.temple.mapchat.MessagesAdapter;
import edu.temple.mapchat.services.MessagingServerAPIHelper;
import edu.temple.mapchat.R;
import edu.temple.messages.MessagesDBContract;
import edu.temple.messages.MessagesDBHelper;
import edu.temple.nfc.KeysContract;
import edu.temple.nfc.RSA;

/**
 * Created by Jessica on 4/2/2018.
 */

public class ChatActivity extends Activity {
    private String user, partner, partnerPublicKey, userPrivateKey;
    private TextView partnerName;
    private RecyclerView chatRecyclerView;
    private ImageButton sendButton;
    private EditText text;
    private Button clearButton;

    private LinearLayoutManager myLayoutManager;
    private List<ChatMessage> myMessagesList = new ArrayList<>();
    private MessagesAdapter messagesAdapter = null;

    SQLiteDatabase db;
    MessagesDBHelper messagesDBHelper;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String messageString = "";
            String sender = "";

            try {
                JSONObject messageObject = new JSONObject(intent.getStringExtra("message"));
                sender = messageObject.getString("from");
                messageString = messageObject.getString("message");
                Log.i("ChatActivity", "Receiving message from " + sender + "." + messageString);
                addMessage(messageString, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //-- Initialize the views --//
        chatRecyclerView = findViewById(R.id.recyclerChat);
        sendButton = findViewById(R.id.btnSend);
        clearButton = findViewById(R.id.clearDB);
        text = findViewById(R.id.editWriteMessage);
        chatRecyclerView.setHasFixedSize(true);

        //-- Use a linear layout manager --//
        myLayoutManager = new LinearLayoutManager(this);
        myLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(myLayoutManager);

        Intent intentData = getIntent();
        partner = intentData.getStringExtra("Partner_Info");
        user = intentData.getStringExtra("Username_Info");
        partnerPublicKey = intentData.getStringExtra("Key_Info");

        partnerName = findViewById(R.id.chatUser);
        partnerName.setText(partner);

        messagesAdapter = new MessagesAdapter(myMessagesList, this);
        chatRecyclerView.setAdapter(messagesAdapter);

        messagesDBHelper = new MessagesDBHelper(this);

        populateChatView();

        ContentResolver cr = getContentResolver();

        String[] projection = new String[]{KeysContract.KeyEntry.ALIAS, KeysContract.KeyEntry.PRIVATE};
        String selection = KeysContract.KeyEntry.ALIAS + " = '" + user + "'";

        final Cursor cursor = cr.query(KeysContract.KeyEntry.CONTENT_URI, projection, selection, null, null);

        if (cursor != null  && cursor.moveToFirst()) {
            if (cursor.getCount() != 0) {
                String key = cursor.getString(cursor.getColumnIndex(KeysContract.KeyEntry.PRIVATE));
                userPrivateKey = key;
                Log.i("UserPrivateKey", key);
            }
        }
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendingMessage = text.getText().toString();

                try {
                    addMessage(sendingMessage, true);
                    
                    if (userPrivateKey != null) {
                        byte[] msg = RSA.encrypt(userPrivateKey, sendingMessage);
                        sendingMessage = RSA.encodeByteArray(msg);
                    }

                    MessagingServerAPIHelper.sendMessage(user, partner, sendingMessage, getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clearDBQuery = "DELETE FROM " + MessagesDBContract.MessagesEntry.TABLE_NAME;
                db.execSQL(clearDBQuery);
                myMessagesList.clear();
                messagesAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("CHAT_APP_MESSAGE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void saveMessages(String to, String from, String message) {
        db = messagesDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessagesDBContract.MessagesEntry.COLUMN_NAME_PARTNER, partner);
        values.put(MessagesDBContract.MessagesEntry.COLUMN_NAME_TO, to);
        values.put(MessagesDBContract.MessagesEntry.COLUMN_NAME_FROM, from);
        values.put(MessagesDBContract.MessagesEntry.COLUMN_NAME_MESSAGE, message);

        long newRowId;
        newRowId = db.insert(MessagesDBContract.MessagesEntry.TABLE_NAME, null, values);

        if (newRowId > 0) {
            Log.d("Messages saved ", newRowId + " - " + message);
            //populateChatView();
        }

    }


    private void addMessage(String message, boolean me) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        //myMessagesList.clear();
        String sender;
        String receiver;
        byte[] msg;
        if (me) {
            sender = user;
            receiver = partner;
        } else {
            sender = partner;
            receiver = user;
            msg = RSA.decodeString(message);
            message = RSA.decrypt(partnerPublicKey, msg);
        }

        ChatMessage newMsg = new ChatMessage(message, sender, receiver);
        myMessagesList.add(newMsg);
        saveMessages(sender, receiver, message);
        Log.i("ChatActivity", "Message being added.");
        messagesAdapter.notifyDataSetChanged();
    }

    private void populateChatView() {
        db = messagesDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MessagesDBContract.MessagesEntry.TABLE_NAME + " WHERE " +
                MessagesDBContract.MessagesEntry.COLUMN_NAME_PARTNER + " = " + "'" + partner + "'", null);

        Log.i("Cursor query", cursor.toString());

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    boolean isMe;
                    String from = cursor.getString(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_NAME_FROM));
                    String message = cursor.getString(cursor.getColumnIndex(MessagesDBContract.MessagesEntry.COLUMN_NAME_MESSAGE));

                    if (from != null) {
                        if (from.equals(user)) {
                            isMe = true;
                        } else {
                            isMe = false;
                        }

                        try {
                            addMessage(message, isMe);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (message != null) {
                        Log.i("Populating Chat View", message);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
    }
}
