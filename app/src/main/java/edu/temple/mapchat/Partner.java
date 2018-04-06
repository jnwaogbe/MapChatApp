package edu.temple.mapchat;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import edu.temple.mapchat.activities.MainActivity;

/**
 * Created by Jessica on 3/12/2018.
 */

public class Partner implements Comparable<Partner>, Parcelable {

    private String user;
    private double latitude, longitude;
    private double distanceFromUser;


    public Partner(String user, double latitude, double longitude) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        distanceFromUser = setDistanceFromUser(latitude, longitude);
    }

    public Partner(JSONObject userInfo) throws JSONException {
        this(userInfo.getString("user"), userInfo.getDouble("latitude"), userInfo.getDouble("longitude"));
    }

    protected Partner(Parcel in) {
        user = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        distanceFromUser = in.readDouble();
    }

    public static final Creator<Partner> CREATOR = new Creator<Partner>() {
        @Override
        public Partner createFromParcel(Parcel in) {
            return new Partner(in);
        }

        @Override
        public Partner[] newArray(int size) {
            return new Partner[size];
        }
    };

    public String getUserName() {
        return user;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public JSONObject getUserInfoAsJSON() {
        JSONObject userInfo = new JSONObject();

        try {
            userInfo.put("username", user);
            userInfo.put("latitude", latitude);
            userInfo.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userInfo;
    }

    public String toString() {
        return "username: " + user + "\nlatitude: " + latitude + "\nlongitude: " + longitude;
    }

    private double setDistanceFromUser(double latitude, double longitude) {
        Location locationMyUser = new Location("point A");
        Location locationThisUser = new Location("point B");

        double distance;

        locationMyUser.setLatitude(MainActivity.lat);
        locationMyUser.setLongitude(MainActivity.longi);

        locationThisUser.setLatitude(getLatitude());
        locationThisUser.setLongitude(getLongitude());

        distance = (int) locationMyUser.distanceTo(locationThisUser);
        return distance;
    }

    public double getDistanceFromUser() {
        return distanceFromUser;
    }

    @Override
    public int compareTo(Partner thisUser) {

        return (int) distanceFromUser - (int) thisUser.distanceFromUser;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

}
