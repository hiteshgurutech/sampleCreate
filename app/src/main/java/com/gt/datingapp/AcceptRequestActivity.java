package com.gt.datingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gt.datingapp.constant.Constant;
import com.gt.datingapp.model.AcceptRequestModel;
import com.gt.datingapp.model.NotificationModel;

public class AcceptRequestActivity extends AppCompatActivity {


    private ImageView userImage;
    private TextView txtUserName, txtUserGender, txtUseEmail, txtUserPhone, btnEmail, btnPhone;

    private NotificationModel notificationModel;
    AcceptRequestModel acceptRequestModel;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_request);

        InitView();

        Intent intent = getIntent();

        if (intent != null) {
            notificationModel = (NotificationModel) intent.getSerializableExtra("notification");

            acceptRequestModel = notificationModel.getAcceptRequestModel();

            //  Picasso.with(AcceptRequestActivity.this).load(Constant.IMAGE_URL + acceptRequestModel.getProfile_photo()).fit().into(userImage);
            txtUserName.setText(acceptRequestModel.getName());
            txtUseEmail.setText(acceptRequestModel.getEmail());
            if (acceptRequestModel.getPhone().equalsIgnoreCase("")) {
                txtUserPhone.setVisibility(View.GONE);
            } else {
                txtUserPhone.setVisibility(View.VISIBLE);
                txtUserPhone.setText(acceptRequestModel.getPhone());
            }
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void InitView() {
        userImage = (ImageView) findViewById(R.id.userImage);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtUseEmail = (TextView) findViewById(R.id.txtUseEmail);
        txtUserPhone = (TextView) findViewById(R.id.txtUserPhone);

        btnEmail = (TextView) findViewById(R.id.btnEmail);
        btnPhone = (TextView) findViewById(R.id.txtUserPhone);

        btnEmail.setVisibility(View.GONE);
        btnPhone.setVisibility(View.GONE);
        userImage.setVisibility(View.GONE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(AcceptRequestActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AcceptRequestActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
