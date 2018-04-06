package edu.temple.mapchat.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.temple.mapchat.Partner;
import edu.temple.mapchat.services.MyRequestSingleton;

/**
 * Created by Jessica on 3/12/2018.
 */

public class ServerAPIHelper {

    private static final String APIGetURL = "https://kamorris.com/lab/get_locations.php";
    private static final String APIPostURL = "https://kamorris.com/lab/register_location.php";
    private static ArrayList<Partner> partnerList;
    private static final String TAG = "SAH";

    public static ArrayList<Partner> downloadPartnerData(final Context context) {
        RequestQueue requestQueue = MyRequestSingleton.getInstance(context).getRequestQueue();

        partnerList = new ArrayList<>();
        JsonArrayRequest jsonArrayGetRequest = new JsonArrayRequest(APIGetURL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                        Log.i(TAG + " on Response", response.toString());

                        Intent intent = new Intent("BROADCAST_REQUEST").putExtra("Request", response.toString());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.getMessage());
                    }
                }
        );

        requestQueue.add(jsonArrayGetRequest);
        return partnerList;
    }


    public static boolean register(final String username, final String latitude, final String longitude, final Context context) throws Exception {

        RequestQueue requestQueue = MyRequestSingleton.getInstance(context).getRequestQueue();
        StringRequest postRequest = new StringRequest(Request.Method.POST, APIPostURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("SAH-Reg-Name", username);
                        Toast.makeText(context, "SAH: " + response.toString(), Toast.LENGTH_SHORT).show();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("SAH-Reg-Error", error.getMessage());
                        Toast.makeText(context, "SAH: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user", username);
                params.put("latitude", latitude);
                params.put("longitude", longitude);

                return params;
            }

        };
        Log.i("SAHToString", postRequest.toString());
        requestQueue.add(postRequest);
        return true;
    }


}
