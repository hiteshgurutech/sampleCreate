package com.gt.datingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.gt.datingapp.api.ApiClient;
import com.gt.datingapp.api.ApiInterface;
import com.gt.datingapp.model.RegisterModel;
import com.gt.datingapp.model.RegisterResponse;
import com.gt.datingapp.permissionModule.PermissionUtils;
import com.gt.datingapp.widget.AppLocationService;
import com.gt.datingapp.widget.ForgotPopup;
import com.gt.datingapp.widget.Internet;
import com.gt.datingapp.widget.LocationAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback,
        PermissionUtils.PermissionResultCallback {

    // Google client to interact with Google API
    private static final int LOCATION_REQUEST = 101;

    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
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
    private AppCompatEditText log_editEmail, log_editPassword;
    private TextInputLayout inputLayEmail, inputLayPassword;
    private AppCompatButton btnLogin, login_btnForgot, login_btnNoAccount;
    private String token;
    private ProgressDialog pd;
    private LoginActivity activity;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activity = this;

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        token = FirebaseInstanceId.getInstance().getToken();

        InitView();

        pd = new ProgressDialog(LoginActivity.this);
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new Internet(LoginActivity.this).isInternetOn()) {
                    if (isValid()) {
                        RegisterApi();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        login_btnNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        login_btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ForgotPopup(LoginActivity.this).show();
            }
        });

    }

    private void RegisterApi() {

        pd.show();

        Map<String, String> map = new HashMap<>();
        map.put("email", log_editEmail.getText().toString().trim());
        map.put("password", log_editPassword.getText().toString().trim());
        map.put("latitude", "" + latitude);
        map.put("longitude", "" + longitude);
        map.put("device_token", token);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<RegisterResponse> registerResponseCall = apiService.getLoginResponse(map);

        registerResponseCall.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        RegisterModel registerModel = response.body().getData();
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean("isLogin", true);
                        editor.putString("user_id", registerModel.getUserId());
                        editor.putString("user_status", registerModel.getUser_status());
                        editor.putString("username", registerModel.getName());
                        editor.putString("email", registerModel.getEmail());
                        editor.apply();
                        if (registerModel.getUser_status().equalsIgnoreCase("Notverified")) {
                            Intent intent = new Intent(LoginActivity.this, OtpScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else if (registerModel.getUser_status().equalsIgnoreCase("Active")) {
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                pd.dismiss();
                Log.d("tag", "Error:" + t.toString());
            }
        });

    }

    private boolean isValid() {

        if (log_editEmail.getText().length() == 0) {
            Toast.makeText(LoginActivity.this, "Please enter email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (log_editPassword.getText().length() == 0) {
            Toast.makeText(LoginActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(log_editEmail.getText().toString()).matches()) {
            Toast.makeText(LoginActivity.this, "Please enter valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (latitude == 0.0) {
            getLocation();
            Toast.makeText(LoginActivity.this, "Location is not found, Please try again!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (latitude == 0.0) {
            getLocation();
            Toast.makeText(LoginActivity.this, "Location is not found, Please try again!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void InitView() {

        permissionUtils = new PermissionUtils(LoginActivity.this);

        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionUtils.check_permission(permissions, "Need GPS permission for getting your location", 1);
        // check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        getLocation();
        //////////////////////////// findview by id

        log_editEmail = (AppCompatEditText) findViewById(R.id.log_editEmail);
        inputLayEmail = (TextInputLayout) findViewById(R.id.inputLayEmail);
        log_editPassword = (AppCompatEditText) findViewById(R.id.log_editPassword);
        inputLayPassword = (TextInputLayout) findViewById(R.id.inputLayPassword);
        btnLogin = (AppCompatButton) findViewById(R.id.btnLogin);
        login_btnForgot = (AppCompatButton) findViewById(R.id.login_btnForgot);
        login_btnNoAccount = (AppCompatButton) findViewById(R.id.login_btnNoAccount);

    }

    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLocation();
                        break;
                    case Activity.RESULT_CANCELED:
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
            } else {

            }
            locationService = new AppLocationService(activity, locationHandler);
            if (locationService.isLocationServiceEnabled(activity)) {
                locationService.getLocation();
            } else {
                // buildAlertMessageNoGps();
            }

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
                            status.startResolutionForResult(LoginActivity.this, REQUEST_CHECK_SETTINGS);

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

}
