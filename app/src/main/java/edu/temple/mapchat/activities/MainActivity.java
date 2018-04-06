package edu.temple.mapchat.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.location.Location;
import android.location.LocationManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.iid.FirebaseInstanceId;

import edu.temple.mapchat.services.MessagingServerAPIHelper;
import edu.temple.mapchat.R;
import edu.temple.mapchat.services.ServerAPIHelper;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static String username;
    private TextView gpsLat;
    private TextView gpsLong;
    private TextView welcomeText;
    private EditText usernameInput;
    private Location location;

    private Button registerButton;
    private Button partnersButton;
    private Button signoutButton;
    private Button keyPartnersButton;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static double lat, longi = 0;

    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("Login_Info", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        registerButton = findViewById(R.id.enterButton);
        partnersButton = findViewById(R.id.viewUsers);
        keyPartnersButton = findViewById(R.id.exchangedUsers);

        signoutButton = findViewById(R.id.signoutButton);

        signoutButton.setVisibility(View.INVISIBLE);

        gpsLat = findViewById(R.id.latitude);
        gpsLong = findViewById(R.id.longitude);
        usernameInput = findViewById(R.id.input);
        welcomeText = (TextView) findViewById(R.id.welcome);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 10, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 10, this);

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        String name = sharedPreferences.getString("Name", null);

        if (name != null) {
            username = name;
            hideButtons(username);

        } else {
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    username = usernameInput.getText().toString();
                    Log.d("Username", username);

                    if (username.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter a username.", Toast.LENGTH_SHORT).show();
                        return;

                    } else {
                        try {
                            editor.putString("Name", username);
                            editor.commit();
                            hideButtons(username);
                            postToServer(username, lat, longi);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            });
        }


        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.clear();
                editor.commit();
                showButtons();
            }
        });


        //-- Click to view List of Partners --//
        partnersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loggedIn == true && username != null) {

                    try {
                        postToServer(username, lat, longi);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.i("MA - PartnersButton", username);
                    Intent intent = new Intent(MainActivity.this, PartnerListActivity.class);
                    intent.putExtra("Username_Info", username);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "You must be logged in.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //-- Click to view List of Partners You Have Exchanged Keys w/ --//
        keyPartnersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loggedIn == true && username != null) {
                    Intent intent = new Intent(MainActivity.this, NFCSenderActivity.class);
                    intent.putExtra("Username_Info", username);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "You must be logged in.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            updateLocation(location);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void updateLocation(Location location)  {
        if (location != null) {
            lat = location.getLatitude();
            longi = location.getLongitude();

            gpsLat.setText(lat + "");
            gpsLong.setText(longi + "");
        }

        if (username != null && location != null) {
            try {
                postToServer(username, lat, longi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
            Log.d("MainActivity", "This is the latitude: " + lat + ". This is longitude: " + longi);

    }

    private void postToServer(String name, double latitude, double longitude) throws Exception {
        if (!name.isEmpty() && location != null) {
            MessagingServerAPIHelper.register(name, FirebaseInstanceId.getInstance().getToken(), MainActivity.this);
            ServerAPIHelper.register(name, String.valueOf(latitude), String.valueOf(longitude), MainActivity.this);
        }

    }

    private void hideButtons(String username) {
        welcomeText.setText("Hi, " + username);
        usernameInput.setVisibility(View.INVISIBLE);
        registerButton.setVisibility(View.INVISIBLE);
        signoutButton.setVisibility(View.VISIBLE);
        loggedIn = true;
    }

    private void showButtons() {
        usernameInput.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.VISIBLE);
        signoutButton.setVisibility(View.INVISIBLE);
        welcomeText.setText("Please enter your username.");
        loggedIn = false;
    }

    public static String getUsername() {
        if (!username.isEmpty()) {
            return username;
        } else {
            return null;
        }
    }
}
