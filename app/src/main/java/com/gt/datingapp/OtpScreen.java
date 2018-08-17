package com.gt.datingapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.gt.datingapp.api.ApiClient;
import com.gt.datingapp.api.ApiInterface;
import com.gt.datingapp.model.GetVerificationCode;
import com.gt.datingapp.model.RegisterModel;
import com.gt.datingapp.model.RegisterResponse;
import com.gt.datingapp.model.SendVerificationCode;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpScreen extends AppCompatActivity {


    private AppCompatButton btnSendOtp, btnReSendOtp, btnChangeEmail;
    private AppCompatEditText edtOtp;
    private ProgressDialog pd;
    private SharedPreferences sharedpreferences;
    private TextView tvUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpscreen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        InitView();

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(OtpScreen.this);

        pd = new ProgressDialog(OtpScreen.this);
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");

        tvUserEmail.setText(getString(R.string.label_otp_text) + " " + sharedpreferences.getString("email", ""));

        getOtpCodeApi();

        btnReSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOtpCodeApi();
            }
        });

        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    sendOtpApi();
                }
            }
        });

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChangeEmailPopup(OtpScreen.this).show();
            }
        });

    }

    private void InitView() {
        edtOtp = (AppCompatEditText) findViewById(R.id.edt_otp);
        btnSendOtp = (AppCompatButton) findViewById(R.id.btnSendOtp);
        btnReSendOtp = (AppCompatButton) findViewById(R.id.btnReSendOtp);
        btnChangeEmail = (AppCompatButton) findViewById(R.id.btnChangeEmail);
        tvUserEmail = (TextView) findViewById(R.id.tvUserEmail);
    }

    private void getOtpCodeApi() {
        pd.show();

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<GetVerificationCode> getVerificationCodeCall = apiService.getVerificationcode(sharedpreferences.getString("user_id", ""));

        getVerificationCodeCall.enqueue(new Callback<GetVerificationCode>() {
            @Override
            public void onResponse(Call<GetVerificationCode> call, Response<GetVerificationCode> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        Toast.makeText(OtpScreen.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OtpScreen.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<GetVerificationCode> call, Throwable t) {
                pd.dismiss();
                Log.d("tag", "Error:" + t.toString());
            }
        });
    }

    private boolean isValid() {
        if (edtOtp.getText().length() == 0) {
            Toast.makeText(OtpScreen.this, "Please enter verification code", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendOtpApi() {
        pd.show();

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Map<String, String> map = new HashMap<>();
        map.put("user_id", sharedpreferences.getString("user_id", ""));
        map.put("verification_code", edtOtp.getText().toString().trim());

        Call<SendVerificationCode> sendVerificationCodeCall = apiService.sendVerificationcode(map);

        sendVerificationCodeCall.enqueue(new Callback<SendVerificationCode>() {
            @Override
            public void onResponse(Call<SendVerificationCode> call, Response<SendVerificationCode> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        Toast.makeText(OtpScreen.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MyService.class);
                        startService(intent);
                        RegisterModel registerModel = response.body().getData();
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean("isLogin", true);
                        editor.putString("user_id", registerModel.getUserId());
                        editor.putString("user_status", registerModel.getUser_status());
                        editor.putString("name", registerModel.getName());
                        editor.putString("phone", registerModel.getPhone());
                        editor.putString("email", registerModel.getEmail());
                        editor.putString("profile_photo", registerModel.getProfilePhoto());
                        editor.putString("latitude", registerModel.getLatitude());
                        editor.putString("longitude", registerModel.getLongitude());
                        editor.apply();
                        Intent intent1 = new Intent(OtpScreen.this, HomeActivity.class);
                        startActivity(intent1);
                        finish();
                    } else {
                        Toast.makeText(OtpScreen.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<SendVerificationCode> call, Throwable t) {
                pd.dismiss();
                Log.d("tag", "Error:" + t.toString());
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    public class ChangeEmailPopup extends Dialog {


        private AppCompatEditText editEmail;
        private Context mContext;

        public ChangeEmailPopup(@NonNull Context context) {
            super(context);
            this.mContext = context;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_change_email);
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getWindow().getAttributes().windowAnimations = R.style.SlideAnimation;
            AppCompatButton btnCancel = (AppCompatButton) findViewById(R.id.btnCancel);
            AppCompatButton btnSubmit = (AppCompatButton) findViewById(R.id.btnSubmit);
            editEmail = (AppCompatEditText) findViewById(R.id.editEmail);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (editEmail.length() > 0) {
                        if (Patterns.EMAIL_ADDRESS.matcher(editEmail.getText().toString()).matches()) {
                            callchageEmailApi();
                        } else {
                            Toast.makeText(getContext(), "Please emter valid Email-Id", Toast.LENGTH_SHORT).show();
                        }
                    } else if (editEmail.getText().toString().equals("")) {
                        Toast.makeText(getContext(), "Please enter Email-Id", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        private void callchageEmailApi() {

            pd = new ProgressDialog(mContext);
            pd.setIndeterminate(true);
            pd.setMessage("Loading...");
            pd.show();

            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

            Call<RegisterResponse> forgotPasswordResponseCall = apiService.changeEmail(editEmail.getText().toString(), sharedpreferences.getString("user_id", ""));

            forgotPasswordResponseCall.enqueue(new Callback<RegisterResponse>() {
                @Override
                public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                    pd.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body().getSuccess()) {
                            dismiss();
                            Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            getOtpCodeApi();
                        } else {
                            Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<RegisterResponse> call, Throwable t) {
                    pd.dismiss();
                    Log.d("tag", "Error: " + t.toString());
                }
            });

        }

        private void getOtpCodeApi() {
            pd.show();

            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

            Call<GetVerificationCode> getVerificationCodeCall = apiService.getVerificationcode(sharedpreferences.getString("user_id", ""));

            getVerificationCodeCall.enqueue(new Callback<GetVerificationCode>() {
                @Override
                public void onResponse(Call<GetVerificationCode> call, Response<GetVerificationCode> response) {
                    pd.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body().getSuccess()) {
                            Toast.makeText(OtpScreen.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("email", response.body().getData().getEmail());
                            editor.apply();
                            tvUserEmail.setText(getString(R.string.label_otp_text) + " " + sharedpreferences.getString("email", ""));
                        } else {
                            Toast.makeText(OtpScreen.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<GetVerificationCode> call, Throwable t) {
                    pd.dismiss();
                    Log.d("tag", "Error:" + t.toString());
                }
            });

        }

    }


}
