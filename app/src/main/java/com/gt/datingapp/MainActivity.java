package com.gt.datingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.gt.datingapp.adapter.UserListAdapter;
import com.gt.datingapp.api.ApiClient;
import com.gt.datingapp.api.ApiInterface;
import com.gt.datingapp.constant.Constant;
import com.gt.datingapp.model.AcceptRequestModel;
import com.gt.datingapp.model.NotificationModel;
import com.gt.datingapp.model.RequestSendResponse;
import com.gt.datingapp.model.SendRequestModel;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback,
        PermissionUtils.PermissionResultCallback, UserListAdapter.AdapterImagedapter {


    public static double latitude = 0.0;
    // Google client to interact with Google API
    public static double longitude = 0.0;

    // list of permissions
    ArrayList<String> permissions = new ArrayList<>();
    PermissionUtils permissionUtils;
    boolean isPermissionGranted;
    AlertDialog alert;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences sharedpreferences;

    MainActivity activity;
    private Handler locationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(bundle.getDouble("lat"), bundle.getDouble("lng"), activity, new GeocoderHandler());

            super.handleMessage(msg);
        }
    };
    private boolean isLocationAvailable = false;
    private AppLocationService locationService;
    // Google client to interact with Google API
    private static final int LOCATION_REQUEST = 101;

    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private ProgressDialog pd;
    private RecyclerView userList;
    private LinearLayout lay_nodata;
    private TextView txt_nodata;


    NotificationModel notificationModel;
    SendRequestModel sendRequestModel;
    AcceptRequestModel acceptRequestModel;

    BroadcastReceiver mReceiverHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final NotificationModel model = (NotificationModel) intent.getSerializableExtra("notification");

            if (intent.getAction().equals(Constant.ACTION_REQUEST_RECEIVE)) {
                Log.d("tag", "Example activity inside action request");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(model.getData())
                        .setPositiveButton("Show", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                notificationModel = model;
                                handlerNotification();
                            }
                        })
                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            } else if (intent.getAction().equals(Constant.ACTION_REQUEST_SEND)) {

                Log.d("tag", "Example activity invitation action request");
                //final InviteNotificationModel inviteNotificationModel1 = (InviteNotificationModel) intent.getSerializableExtra("inviteModel");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(model.getData())
                        .setPositiveButton("Show", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                notificationModel = model;
                                //     inviteNotificationModel = inviteNotificationModel1;
                                handlerNotification();
                            }
                        })
                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            }
        }

    };

    private void handlerNotification() {

        Log.d("tag", "notification type ::" + notificationModel.getType());
        if (notificationModel.getType().equals("Send")) {
            Intent intent = new Intent(MainActivity.this, SendRequestActivity.class);
            intent.putExtra("notification", notificationModel);
            startActivity(intent);
        } else if (notificationModel.getType().equals("Accept")) {
            Intent intent = new Intent(MainActivity.this, AcceptRequestActivity.class);
            intent.putExtra("notification", notificationModel);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        pd = new ProgressDialog(MainActivity.this);
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        InitView();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_REQUEST_SEND);
        intentFilter.addAction(Constant.ACTION_REQUEST_RECEIVE);
        registerReceiver(mReceiverHandler, intentFilter);
        notificationModel = (NotificationModel) getIntent().getSerializableExtra("notification");

        if (notificationModel != null) {
            handlerNotification();
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                handler.postDelayed(this, 15 * 1000);
                getLocation();
                if (isValid()) {
                    getUserlistApi();
                } else {
                    latitude = Float.parseFloat(sharedpreferences.getString("latitude", ""));
                    longitude = Float.parseFloat(sharedpreferences.getString("longitude", ""));
                    getUserlistApi();
                }
            }
        }, 15 * 1000);
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
                                UserListAdapter userListAdapter = new UserListAdapter(MainActivity.this, userListModel.getUsers(), MainActivity.this);
                                userList.setAdapter(userListAdapter);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserlistResponse> call, Throwable t) {
                // pd.dismiss();
            }
        });

    }

    private void InitView() {

        permissionUtils = new PermissionUtils(MainActivity.this);

        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionUtils.check_permission(permissions, "Need GPS permission for getting your location", 1);
        // check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }
        getLocation();

        userList = (RecyclerView) findViewById(R.id.userList);
        userList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        lay_nodata = (LinearLayout) findViewById(R.id.lay_nodata);
        txt_nodata = (TextView) findViewById(R.id.txt_nodata);

        if (isValid()) {
            getUserlistApi();
        } else {
            latitude = Float.parseFloat(sharedpreferences.getString("latitude", ""));
            longitude = Float.parseFloat(sharedpreferences.getString("longitude", ""));
            getUserlistApi();
        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);

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

    @Override
    public void onImageClick(int position, User user) {

        intersetedApi(user);
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
                        Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
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


    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void getLocation() {

        if (isPermissionGranted) {
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
                editor.apply();
                Log.d("tag", "Latitude:" + latitude);
            } else {
                // Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_SHORT).show();
            }
            locationService = new AppLocationService(activity, locationHandler);
            if (locationService.isLocationServiceEnabled(activity)) {
                locationService.getLocation();
            } else {
                //  buildAlertMessageNoGps();
            }

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

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        onBackPressed();
                    }
                });
        alert = builder.create();
        alert.show();

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
