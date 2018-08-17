package com.gt.datingapp.widget;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

public class AppLocationService extends Service implements LocationListener {

    protected LocationManager locationManager;
    Location location;
    boolean checkGPS = false;

    boolean checkNetwork = false;

    boolean canGetLocation = false;
    private static final long MIN_DISTANCE_FOR_UPDATE = 0;
    private static final long MIN_TIME_FOR_UPDATE = 2000 * 60 * 1;  // 1000 * 60 * 1
    private Handler mHandler;
    private Context mContext;
    private Location loc;

    public AppLocationService(Context context, Handler handler) {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        this.mContext = context;
        this.mHandler = handler;
    }

    public void getLocation() {
        try {
            // getting GPS status
            checkGPS = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            checkNetwork = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                //  Toast.makeText(mContext, "No Service Provider Available", Toast.LENGTH_SHORT).show();
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (checkNetwork) {
                    // Toast.makeText(mContext, "Network", Toast.LENGTH_SHORT).show();
                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_FOR_UPDATE,
                                MIN_DISTANCE_FOR_UPDATE, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            loc = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        if (loc != null) {
                            Message message = Message.obtain();
                            message.setTarget(mHandler);
                            message.what = 1;
                            Bundle bundle = new Bundle();
                            bundle.putDouble("lat", loc.getLatitude());
                            bundle.putDouble("lng", loc.getLongitude());
                            message.setData(bundle);
                            message.sendToTarget();
                        }
                    } catch (SecurityException e) {

                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (checkGPS) {
                // Toast.makeText(mContext,"GPS",Toast.LENGTH_SHORT).show();
                if (loc == null) {
                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_FOR_UPDATE,
                                MIN_DISTANCE_FOR_UPDATE, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            loc = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (loc != null) {
                                Message message = Message.obtain();
                                message.setTarget(mHandler);
                                message.what = 1;
                                Bundle bundle = new Bundle();
                                bundle.putDouble("lat", loc.getLatitude());
                                bundle.putDouble("lng", loc.getLongitude());
                                message.setData(bundle);
                                message.sendToTarget();
                            }
                        }
                    } catch (SecurityException e) {

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void requestUpdate() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    onLocationChanged(location);
                }

            }
        }
    }

    public boolean isLocationServiceEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("tag", "On location change:" + location.getLatitude() + " Longitude:" + location.getLongitude());
        Message message = Message.obtain();
        message.setTarget(mHandler);
        message.what = 1;
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", location.getLatitude());
        bundle.putDouble("lng", location.getLongitude());
        message.setData(bundle);
        message.sendToTarget();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(AppLocationService.this);
        }
    }
}