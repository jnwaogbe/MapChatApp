package edu.temple.mapchat.services;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Created by Jessica on 3/12/2018.
 */

public class MyRequestSingleton {

    private static MyRequestSingleton rq;
    private RequestQueue myRequestQueue;
    private static Context context;

    private MyRequestSingleton(Context context) {
        this.context = context;
        myRequestQueue = getRequestQueue();
    }

    public static synchronized MyRequestSingleton getInstance(Context context) {
        if (rq == null) {
            rq = new MyRequestSingleton(context);
        }

        return rq;
    }

    public RequestQueue getRequestQueue() {
        if (myRequestQueue == null) {
            myRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }

        return myRequestQueue;
    }
}
