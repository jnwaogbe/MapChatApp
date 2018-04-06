package edu.temple.mapchat.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import edu.temple.mapchat.Partner;
import edu.temple.mapchat.R;
import edu.temple.mapchat.services.ServerAPIHelper;
import edu.temple.mapchat.fragments.MapFragment;
import edu.temple.mapchat.fragments.PartnerListFragment;


public class PartnerListActivity extends AppCompatActivity implements LocationListener {
    private boolean twoPanes;
    private ArrayList<Partner> partnerList;
    private MapFragment mapFragment;
    private PartnerListFragment partnerListFragment;
    private boolean listVisible;
    private JSONArray response;
    private String thisUser;
    private double dlongitude, dlatitude;
    private String username, lat, longi;
    String latitude, longitude;
    private Location location;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                response = new JSONArray(intent.getStringExtra("Request"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = (JSONObject) response.get(i);
                        username = jsonObject.getString("username");
                        lat = jsonObject.getString("latitude");
                        longi = jsonObject.getString("longitude");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (lat != null && longi != null && username != null) {
                        try {
                            dlatitude = new Double(lat);
                        } catch (NumberFormatException e) {
                            dlatitude = 0.0;
                        }

                        try {
                            dlongitude = new Double(longi);
                        } catch (NumberFormatException e) {
                            dlongitude = 0.0;
                        }

                        Partner partner = new Partner(username, dlatitude, dlongitude);
                        partnerList.add(partner);
                    }
                }


                if (partnerList != null) {
                    Collections.sort(partnerList);

                    partnerListFragment.updatePartners(partnerList);
                    mapFragment.updatePartners(partnerList);
                }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partnerlist);

        //-- Location Stuff --//
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 100, this);

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        thisUser = getIntent().getStringExtra("Username_Info");
        Log.i("PLActivity ThisUser", thisUser);

        twoPanes = (findViewById(R.id.fragment2) != null);

        mapFragment = new MapFragment();
        partnerListFragment = new PartnerListFragment();

        partnerList = ServerAPIHelper.downloadPartnerData(PartnerListActivity.this);

        Log.i("PartnerListActivity", partnerList.toString());

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("PartnersList", partnerList);
        bundle.putString("Username", thisUser);

        partnerListFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, partnerListFragment).commit();

        //-- Check if large screen --//
        if (twoPanes) {
            Bundle bundle2 = new Bundle();
            bundle2.putParcelableArrayList("PartnersList2", partnerList);
            mapFragment.setArguments(bundle2);

            FragmentManager fragmentManager1 = getFragmentManager();
            FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
            fragmentTransaction1.add(R.id.fragment2, mapFragment).commit();

        } else {
            listVisible = true;

            //-- Switch from List View to Map View Fragments --//
            final Button switchView = findViewById(R.id.switchButton);

            switchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listVisible == true) {
                        switchToMapFragment();
                        switchView.setText("Switch to List View");
                    } else {
                        switchToListFragment();
                        switchView.setText("Switch to Map View");
                    }
                }
            });
        }
    }

    private void switchToMapFragment() {
        Bundle bundle2 = new Bundle();
        bundle2.putParcelableArrayList("PartnersList2", partnerList);
        bundle2.putString("Username2", thisUser);
        mapFragment.setArguments(bundle2);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, mapFragment)
                .addToBackStack(null)
                .commit();

        listVisible = false;
    }

    private void switchToListFragment() {
        FragmentManager fragmentManager = getFragmentManager();


        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }

        listVisible = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("BROADCAST_REQUEST"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            ServerAPIHelper.register(thisUser, latitude, longitude, this);
            //ServerAPIHelper.downloadPartnerData(PartnerListActivity.this);
            Toast.makeText(getApplicationContext(), "Location changed.", Toast.LENGTH_SHORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
