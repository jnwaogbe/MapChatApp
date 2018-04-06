package edu.temple.mapchat.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import edu.temple.mapchat.Partner;
import edu.temple.mapchat.R;

/**
 * Created by Jessica on 3/14/2018.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap myMap;
    private MapView myMapView;
    private View v;
    private ArrayList<Partner> partnerList;

    public MapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_map, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myMapView = (MapView) v.findViewById(R.id.mapView);

        if (myMapView != null) {
            myMapView.onCreate(null);
            myMapView.onResume();
            myMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        partnerList = getArguments().getParcelableArrayList("PartnersList2");

        Log.i("MapFragment", partnerList.toString());

        double lat, longi;
        String user;

            for (Partner partner : partnerList) {
                lat = partner.getLatitude();
                longi = partner.getLongitude();
                user = partner.getUserName();
                googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, longi)).title(user).snippet(user));
                Log.i("MapFragment", user);

            }

        CameraPosition position = CameraPosition.builder().target(new LatLng(39.98, -75.16)).zoom(16).bearing(0).tilt(45).build();

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    public void updatePartners(ArrayList<Partner> partnerList) {
        this.partnerList = partnerList;
    }

    }
