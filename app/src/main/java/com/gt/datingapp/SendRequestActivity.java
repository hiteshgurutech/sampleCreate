package com.gt.datingapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gt.datingapp.api.ApiClient;
import com.gt.datingapp.api.ApiInterface;
import com.gt.datingapp.constant.Constant;
import com.gt.datingapp.model.NotificationModel;
import com.gt.datingapp.model.RequestSendResponse;
import com.gt.datingapp.model.SendRequestModel;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendRequestActivity extends AppCompatActivity {

    private ImageView userImage;
    private TextView txtUserName, txtUserGender, btnAccept, btnReject, txtUseEmail, txtUserPhone, btnNow;
    private LinearLayout layUserInfo, layButton;
    private NotificationModel notificationModel;
    SendRequestModel sendRequestModel;

    BroadcastReceiver mReceiverHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationModel model = (NotificationModel) intent.getSerializableExtra("notification");
            if (intent.getAction().equalsIgnoreCase(Constant.ACTION_MESSAGE_RECEIVE)) {
                notificationModel = model;
            }
        }
    };

    private SharedPreferences sharedPreferences;

    private boolean isAccept = false;
    private ProgressDialog pd;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);

        InitView();

        pd = new ProgressDialog(SendRequestActivity.this);
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            notificationModel = (NotificationModel) intent.getSerializableExtra("notification");
            sendRequestModel = notificationModel.getSendRequestModel();
            txtUserName.setText(sendRequestModel.getName());
        }

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SendRequestActivity.this);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAcceptApi("cell");
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAcceptApi("email");
            }
        });

        btnNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAcceptApi("now");
            }
        });
    }

    private void requestAcceptApi(String type) {

        pd.show();
        Map<String, String> map = new HashMap<>();
        map.put("request_id", sendRequestModel.getUser_id());
        map.put("user_id", sharedPreferences.getString("user_id", ""));
        map.put("accept_type", type);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<RequestSendResponse> requestSendResponseCall = apiService.getAcceptResponse(map);

        requestSendResponseCall.enqueue(new Callback<RequestSendResponse>() {
            @Override
            public void onResponse(Call<RequestSendResponse> call, Response<RequestSendResponse> response) {

                pd.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        Toast.makeText(SendRequestActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SendRequestActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SendRequestActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SendRequestActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<RequestSendResponse> call, Throwable t) {
                pd.dismiss();
            }
        });

    }

    private void InitView() {

        userImage = (ImageView) findViewById(R.id.userImage);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        btnAccept = (TextView) findViewById(R.id.btnAccept);
        btnReject = (TextView) findViewById(R.id.btnReject);
        btnNow = (TextView) findViewById(R.id.btnNow);
        layButton = (LinearLayout) findViewById(R.id.layButton);

        userImage.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(SendRequestActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SendRequestActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
