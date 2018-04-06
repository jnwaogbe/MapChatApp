package edu.temple.mapchat.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.temple.mapchat.Partner;

/**
 * Created by Jessica on 4/2/2018.
 */

public class MessagingServerAPIHelper {

    private static final String APIPostURL = "https://kamorris.com/lab/";
    private static final String register_user = "fcm_register.php";
    private static final String send_message = "send_message.php";

    private static ArrayList<Partner> partnerList;

    private static Context context;

    private static boolean makeAPICall(final String username, final String fcmToken) throws Exception {

        RequestQueue requestQueue = MyRequestSingleton.getInstance(context).getRequestQueue();
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIPostURL + register_user,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("MSAH-Reg-Name", username);
                        Toast.makeText(context, "MSAH Register: " + response.toString(), Toast.LENGTH_SHORT).show();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", error.getMessage());
                        Toast.makeText(context, "MSAH Register: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("user", username);
                params.put("token", fcmToken);

                return params;
            }

        };

        requestQueue.add(postRequest);
        return true;
    }

    private static boolean makeAPICall(final String username, final String fcmToken, final String message) throws Exception {

        RequestQueue requestQueue = MyRequestSingleton.getInstance(context).getRequestQueue();
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIPostURL + send_message,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("MSAH-Msg-Name", username);
                        Toast.makeText(context, "MSAH: " + response.toString(), Toast.LENGTH_SHORT).show();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MSAH-Error", error.getMessage());
                        Toast.makeText(context, "MSAH: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("user", username);
                params.put("partneruser", fcmToken);
                params.put("message", message);

                return params;
            }

        };

        requestQueue.add(postRequest);
        return true;
    }


    public static void register(final String username, final String fcmToken, final Context context) throws Exception {
        MessagingServerAPIHelper.context = context;
        makeAPICall(username, fcmToken);
    }


    public static void sendMessage(final String username, final String partner, final String message, final Context context) throws Exception {
        MessagingServerAPIHelper.context = context;
        makeAPICall(username, partner, message);
    }

}
