package edu.temple.mapchat.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Jessica on 4/2/2018.
 */

public class MyInstanceIDService extends FirebaseInstanceIdService {

    private String username;

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        try {
            saveRegistration(refreshedToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("RegistrationService", "Refreshed token: " + refreshedToken);
    }

    private void saveRegistration(String token) throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("TOKEN", token).apply();
        Log.i("InstanceIDService", "Token saved.");
    }
}
