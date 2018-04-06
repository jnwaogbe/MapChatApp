package edu.temple.mapchat.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.temple.mapchat.Partner;
import edu.temple.mapchat.R;
import edu.temple.mapchat.activities.ChatActivity;
import edu.temple.messages.MessagesDBContract;
import edu.temple.nfc.KeysContract;

/**
 * Created by Jessica on 3/12/2018.
 */

public class PartnerListFragment extends Fragment {
    public static final Uri CONTENT_URI = KeysContract.KeyEntry.CONTENT_URI;

    private ArrayList<Partner> partnerList;
    private List<String> usernamesList;
    private ListView partners;
    private View v;
    private PartnerListAdapter arrayAdapter;
    private String username;
    private boolean usernameSelected = false;

    public PartnerListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        username = bundle.getString("Username");
        partnerList = bundle.getParcelableArrayList("PartnersList");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_partnerlist, container, false);


        partners = v.findViewById(R.id.listView);
        Collections.sort(partnerList);
        arrayAdapter = new PartnerListAdapter(getActivity(), partnerList);
        partners.setAdapter(arrayAdapter);



            partners.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String name = partnerList.get(position).getUserName();
                    ContentResolver cr = getActivity().getContentResolver();

                    String[] projection = new String[]{KeysContract.KeyEntry.ALIAS, KeysContract.KeyEntry.PUBLIC};
                    String selection = KeysContract.KeyEntry.ALIAS + " = '" + name + "'";

                    final Cursor cursor = cr.query(CONTENT_URI, projection, selection, null, null);
                    Log.i("Username from PList", name);

                    if (cursor != null  && cursor.moveToFirst()) {
                        if (cursor.getCount() != 0) {
                            String publicKey = cursor.getString(cursor.getColumnIndex(KeysContract.KeyEntry.PUBLIC));
                            Intent myIntent = new Intent(view.getContext(), ChatActivity.class);
                            myIntent.putExtra("Partner_Info", name);
                            myIntent.putExtra("Username_Info", username);
                            myIntent.putExtra("Key_Info", publicKey);
                            startActivityForResult(myIntent, 0);
                       } else {
                            Toast.makeText(getActivity(), "Error: Must exchange keys with this person.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error: Must exchange keys with " + name, Toast.LENGTH_SHORT).show();
                    }
                }

            });


        return v;
    }

    public void updatePartners(ArrayList<Partner> partnerList) {
        this.partnerList = partnerList;
        Log.i("FromPartnerFrag", partnerList.toString());
        arrayAdapter.notifyDataSetChanged();

    }

    /*
    private void checkIfPartner(ContentResolver cr) {
        Cursor cursor = cr.rawQuery("SELECT * FROM " + MessagesDBContract.MessagesEntry.TABLE_NAME + " WHERE " +
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
                        addMessage(message, isMe);
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
    */

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    class PartnerListAdapter extends ArrayAdapter<Partner> {

        public PartnerListAdapter(Context context, List<Partner> partners) {
            super(context, R.layout.item_row, partners);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.item_row, parent, false);
            }

            Partner partner = getItem(position);

            if (partner != null) {
                TextView user = (TextView) v.findViewById(R.id.username);
                TextView locationInfo = (TextView) v.findViewById(R.id.locationInfo);

                if (user != null && username != null) {
                    user.setText(partner.getUserName());
                    Log.i("Username Selected", "Username Selected: " + username);
                }

                if (locationInfo != null) {
                    //locationInfo.setText("Latitude: " + String.valueOf(partner.getLatitude()) + "\nLongitude: " + String.valueOf(partner.getLongitude()));
                    locationInfo.setText(String.valueOf(partner.getDistanceFromUser()) + " meters.");

                }
            }

            return v;
        }
    }
}
