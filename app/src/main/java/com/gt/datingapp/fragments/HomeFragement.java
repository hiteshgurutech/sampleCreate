package com.gt.datingapp.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.gt.datingapp.HomeActivity;
import com.gt.datingapp.R;
import com.gt.datingapp.adapter.UserListAdapter;
import com.gt.datingapp.api.ApiClient;
import com.gt.datingapp.api.ApiInterface;
import com.gt.datingapp.model.RequestSendResponse;
import com.gt.datingapp.model.User;
import com.gt.datingapp.model.UserListModel;
import com.gt.datingapp.model.UserlistResponse;
import com.gt.datingapp.permissionModule.PermissionUtils;
import com.gt.datingapp.widget.AppLocationService;
import com.gt.datingapp.widget.LocationAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragement extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback,
        PermissionUtils.PermissionResultCallback, UserListAdapter.AdapterImagedapter {

    View rootview;
    private ProgressDialog pd;
    private SharedPreferences sharedpreferences;

    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private LinearLayout lay_nodata;
    private TextView txt_nodata;
    private RecyclerView userList;

    public static double latitude = 0.0;
    public static double longitude = 0.0;

    // list of permissions
    ArrayList<String> permissions = new ArrayList<>();
    PermissionUtils permissionUtils;
    boolean isPermissionGranted;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AppLocationService locationService;
    private boolean isLocationAvailable = false;
    private HomeActivity activity;
    private Handler locationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(bundle.getDouble("lat"), bundle.getDouble("lng"), getActivity(), new GeocoderHandler());

            super.handleMessage(msg);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (HomeActivity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.activity_main, container, false);

        pd = new ProgressDialog(getActivity());
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


        initView();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 15 * 1000);

                if (isValid()) {
                    getLocation();
                    if (sharedpreferences.getBoolean("isLogin", false)) {
                        getUserlistApi();
                    }
                } else {
                    getLocation();
                    latitude = Float.parseFloat(sharedpreferences.getString("latitude", ""));
                    longitude = Float.parseFloat(sharedpreferences.getString("longitude", ""));
                    if (sharedpreferences.getBoolean("isLogin", false)) {
                        getUserlistApi();
                    }
                }
            }
        }, 15 * 1000);

        return rootview;
    }

    private void initView() {

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }
        getLocation();

        userList = (RecyclerView) rootview.findViewById(R.id.userList);
        userList.setLayoutManager(new LinearLayoutManager(getActivity()));
        lay_nodata = (LinearLayout) rootview.findViewById(R.id.lay_nodata);
        txt_nodata = (TextView) rootview.findViewById(R.id.txt_nodata);

        if (isValid()) {
            getUserlistApi();
        } else {
            if (sharedpreferences.getString("latitude", "").equalsIgnoreCase("") && sharedpreferences.getString("longitude", "").equalsIgnoreCase("")) {
            } else {
                latitude = Float.parseFloat(sharedpreferences.getString("latitude", ""));
                longitude = Float.parseFloat(sharedpreferences.getString("longitude", ""));
                getUserlistApi();
            }
        }
    }

    private boolean isValid() {
        if (latitude == 0.0) {
            return false;
        }
        if (longitude == 0.0) {
            return false;
        }
        return true;
    }

    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getActivity());

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(getActivity(), resultCode,
                        PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(getActivity(), "This device is not supported.", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(HomeFragement.this)
                .addOnConnectionFailedListener(HomeFragement.this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location requests here
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

    }

    private void getLocation() {
//        if (isPermissionGranted) {
        try {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("latitude", "" + latitude);
            editor.putString("longitude", "" + longitude);
            editor.commit();
            //  Log.d("tag", "Latitude:" + latitude);
        } else {
            // Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_SHORT).show();
        }
//            locationService = new AppLocationService(getActivity(), locationHandler);
//            if (locationService.isLocationServiceEnabled(getActivity())) {
//                locationService.getLocation();
//            } else {
//                //  buildAlertMessageNoGps();
//            }
        //       }

    }

    @Override
    public void onImageClick(int position, User user) {

//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Hitesh").
//                setMessage("Please talk to me directly now")
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                })
//                .create().show();

        intersetedApi(user);
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            String country, countryCode;

            Bundle bundle = message.getData();
            locationAddress = bundle.getString("address");
            country = bundle.getString("country");
            countryCode = bundle.getString("countryCode");

            //Toast.makeText(activity, "Country:"+country+",Country Code:"+countryCode, Toast.LENGTH_SHORT).show();
            if (!isLocationAvailable) {
                isLocationAvailable = true;
            }
            locationAddress = null;
        }

    }

    private void intersetedApi(User user) {
        pd.show();
        Map<String, String> map = new HashMap<>();
        map.put("request_id", user.getUserId());
        map.put("user_id", sharedpreferences.getString("user_id", ""));

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        final Call<RequestSendResponse> requestSendResponseCall = apiService.getRequestSend(map);

        requestSendResponseCall.enqueue(new Callback<RequestSendResponse>() {
            @Override
            public void onResponse(Call<RequestSendResponse> call, Response<RequestSendResponse> response) {

                pd.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RequestSendResponse> call, Throwable t) {
                pd.dismiss();
                Log.d("tag", "Error:" + t.toString());
            }
        });

    }

    private void getUserlistApi() {
        Map<String, String> map = new HashMap<>();
        map.put("latitude", "" + latitude);
        map.put("longitude", "" + longitude);
        map.put("user_id", sharedpreferences.getString("user_id", ""));

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<UserlistResponse> userlistResponseCall = apiService.getUserListResponse(map);
        userlistResponseCall.enqueue(new Callback<UserlistResponse>() {
            @Override
            public void onResponse(Call<UserlistResponse> call, Response<UserlistResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        if (response.body().getData() != null) {
                            UserListModel userListModel = response.body().getData();
                            if (userListModel.getUsers().size() > 0) {
                                lay_nodata.setVisibility(View.GONE);
                                userList.setVisibility(View.VISIBLE);
                                UserListAdapter userListAdapter = new UserListAdapter(getActivity(), userListModel.getUsers(), HomeFragement.this);
                                userList.setAdapter(userListAdapter);
                            } else {
                                lay_nodata.setVisibility(View.VISIBLE);
                                userList.setVisibility(View.GONE);
                                txt_nodata.setText("No user");
                            }
                        } else {
                            lay_nodata.setVisibility(View.VISIBLE);
                            userList.setVisibility(View.GONE);
                            txt_nodata.setText("No user");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserlistResponse> call, Throwable t) {
                Log.d("tag", "Error" + t.toString());
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        getLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;

                    default:
                        break;
                }
                break;
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i("tag", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void PermissionGranted(int request_code) {
        Log.i("PERMISSION", "GRANTED");
        isPermissionGranted = true;
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY", "GRANTED");
    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION", "DENIED");
    }

    @Override
    public void NeverAskAgain(int request_code) {
        Log.i("PERMISSION", "NEVER ASK AGAIN");
    }

}
