package com.gt.datingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SpalshActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences;
    private String token;
    private static final int PERMISSION_REQUEST_CODE = 201;
    private int which = 0;
    private TextView txtAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(SpalshActivity.this);

        txtAppName = (TextView) findViewById(R.id.txtAppName);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "BROADW_1.TTF");
        txtAppName.setTypeface(typeface);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sharedpreferences.getBoolean("isLogin", false)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkPermission()) {
                            if (sharedpreferences.getString("user_status", "").equalsIgnoreCase("Notverified")) {
                                Intent intent = new Intent(getApplicationContext(), MyService.class);
                                startService(intent);
                                Intent i = new Intent(SpalshActivity.this, OtpScreen.class);
                                startActivity(i);
                                finish();
                            } else if (sharedpreferences.getString("user_status", "").equalsIgnoreCase("Active")) {
                                Intent intent = new Intent(getApplicationContext(), MyService.class);
                                startService(intent);
                                Intent i = new Intent(SpalshActivity.this, HomeActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            }
                        } else {
                            which = 1;
                            requestPermission();
                        }
                    } else {
                        if (sharedpreferences.getString("user_status", "").equalsIgnoreCase("Notverified")) {
                            Intent i = new Intent(SpalshActivity.this, OtpScreen.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();

                        } else if (sharedpreferences.getString("user_status", "").equalsIgnoreCase("Active")) {
                            Intent intent = new Intent(getApplicationContext(), MyService.class);
                            startService(intent);
                            Intent i = new Intent(SpalshActivity.this, HomeActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkPermission()) {
                            Intent i = new Intent(SpalshActivity.this, LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        } else {
                            which = 2;
                            requestPermission();
                        }
                    } else {
                        Intent i = new Intent(SpalshActivity.this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                }
            }
        }, 3 * 1000);

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(SpalshActivity.this, ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(SpalshActivity.this, ACCESS_FINE_LOCATION)) {
            Toast.makeText(SpalshActivity.this, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(SpalshActivity.this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(SpalshActivity.this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getApplicationContext(), MyService.class);
                    startService(intent);
                    CallActivity();
                } else {
                }
                break;
        }
    }

    private void CallActivity() {
        if (which == 1) {
            if (sharedpreferences.getString("user_status", "").equalsIgnoreCase("Notverified")) {
                Intent i = new Intent(SpalshActivity.this, OtpScreen.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            } else if (sharedpreferences.getString("user_status", "").equalsIgnoreCase("Active")) {
                Intent i = new Intent(SpalshActivity.this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        } else if (which == 2) {
            Intent i = new Intent(SpalshActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
    }


}
