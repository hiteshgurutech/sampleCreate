package com.gt.datingapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gt.datingapp.api.ApiClient;
import com.gt.datingapp.api.ApiInterface;
import com.gt.datingapp.constant.Constant;
import com.gt.datingapp.fragments.HomeFragement;
import com.gt.datingapp.fragments.NowFragment;
import com.gt.datingapp.fragments.ReceiveFragment;
import com.gt.datingapp.fragments.SendFragment;
import com.gt.datingapp.model.AcceptRequestModel;
import com.gt.datingapp.model.NotificationModel;
import com.gt.datingapp.model.RequestSendResponse;
import com.gt.datingapp.model.SendRequestModel;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView btn_all, btn_send, btn_received, tvUsername, btn_now;
    private String list_type = "Near By";
    NotificationModel notificationModel;
    SendRequestModel sendRequestModel;
    AcceptRequestModel acceptRequestModel;


    BroadcastReceiver mReceiverHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final NotificationModel model = (NotificationModel) intent.getSerializableExtra("notification");

            if (intent.getAction().equals(Constant.ACTION_REQUEST_RECEIVE)) {
                Log.d("tag", "Example activity inside action request");
                if (model.getAcceptRequestModel().getAccept_type().equalsIgnoreCase("cell")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle(model.getAcceptRequestModel().getName()).
                            setMessage("Please check your Cell tab").create();
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (list_type.equalsIgnoreCase("Near By")) {
                            } else {
                                list_type = "Near By";
                                buttonSelection();
                                HomeFragement homeFragement = new HomeFragement();
                                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                                t.replace(R.id.content_frame, homeFragement);
                                t.commit();
                            }
                        }
                    });
                    builder.show();
                } else if (model.getAcceptRequestModel().getAccept_type().equalsIgnoreCase("email")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle(model.getAcceptRequestModel().getName()).
                            setMessage("Please check your Email tab").create();
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (list_type.equalsIgnoreCase("Near By")) {
                            } else {
                                list_type = "Near By";
                                buttonSelection();
                                HomeFragement homeFragement = new HomeFragement();
                                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                                t.replace(R.id.content_frame, homeFragement);
                                t.commit();
                            }
                        }
                    });
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle(model.getAcceptRequestModel().getName()).
                            setMessage("Please check your Now tab").create();
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (list_type.equalsIgnoreCase("Near By")) {
                            } else {
                                list_type = "Near By";
                                buttonSelection();
                                HomeFragement homeFragement = new HomeFragement();
                                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                                t.replace(R.id.content_frame, homeFragement);
                                t.commit();
                            }
                        }
                    });
                    builder.show();
                }
            } else if (intent.getAction().equals(Constant.ACTION_REQUEST_SEND)) {
                Log.d("tag", "Example activity invitation action request");
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage(model.getData())
                        .setPositiveButton("Call me later", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                notificationModel = model;
                                CallApi("cell", notificationModel);
                            }
                        })
                        .setNegativeButton("Email me later", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                notificationModel = model;
                                CallApi("email", notificationModel);
                            }
                        }).setNeutralButton("Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        notificationModel = model;
                        CallApi("now", notificationModel);
                    }
                }).create().show();
            }
        }

    };
    private ProgressDialog pd;
    private SharedPreferences sharedpreferences;

    private void CallApi(String type, NotificationModel notificationModel) {

        pd.show();
        Map<String, String> map = new HashMap<>();
        map.put("request_id", notificationModel.getSendRequestModel().getUser_id());
        map.put("user_id", sharedpreferences.getString("user_id", ""));
        map.put("accept_type", type);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<RequestSendResponse> requestSendResponseCall = apiService.getAcceptResponse(map);

        requestSendResponseCall.enqueue(new Callback<RequestSendResponse>() {
            @Override
            public void onResponse(Call<RequestSendResponse> call, Response<RequestSendResponse> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        Toast.makeText(HomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RequestSendResponse> call, Throwable t) {
                pd.dismiss();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sharedpreferences.edit().clear().apply();
            MyService myService = new MyService();
            myService.stopSelf();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        return super.onOptionsItemSelected(item);
    }

    private void handlerNotification() {

        //   Log.d("tag", "notification type ::" + notificationModel.getType());
        if (notificationModel.getType().equals("Send")) {
            Intent intent = new Intent(HomeActivity.this, SendRequestActivity.class);
            intent.putExtra("notification", notificationModel);
            startActivity(intent);
            finish();
        } else {
            Log.d("tag", "notification type whrn accept requesy::" + notificationModel.getType());
            Log.d("tag", "notification accept type ::" + notificationModel.getAcceptRequestModel().getAccept_type());

//            if (notificationModel.getAcceptRequestModel().getAccept_type().equalsIgnoreCase("cell")) {
//                if (list_type.equalsIgnoreCase("Send")) {
//                } else {
//                    list_type = "Send";
//                    buttonSelection();
//                    SendFragment sendFragment = new SendFragment();
//                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
//                    t.replace(R.id.content_frame, sendFragment);
//                    t.commit();
//                }
//            } else if (notificationModel.getAcceptRequestModel().getAccept_type().equalsIgnoreCase("email")) {
//                if (list_type.equalsIgnoreCase("Receive")) {
//                } else {
//                    list_type = "Receive";
//                    buttonSelection();
//                    ReceiveFragment receiveFragment = new ReceiveFragment();
//                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
//                    t.replace(R.id.content_frame, receiveFragment);
//                    t.commit();
//                }
//            } else {
//                if (list_type.equalsIgnoreCase("Now")) {
//                } else {
//                    list_type = "Now";
//                    buttonSelection();
//                    NowFragment nowFragment = new NowFragment();
//                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
//                    t.replace(R.id.content_frame, nowFragment);
//                    t.commit();
//                }
//            }
            if (list_type.equalsIgnoreCase("Near By")) {
            } else {
                list_type = "Near By";
                buttonSelection();
                HomeFragement homeFragement = new HomeFragement();
                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                t.replace(R.id.content_frame, homeFragement);
                t.commit();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        tvUsername = (TextView) toolbar.findViewById(R.id.tvUsername);
        if (actionBar != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        InitView();

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);

        tvUsername.setText(sharedpreferences.getString("username", ""));

        pd = new ProgressDialog(HomeActivity.this);
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_REQUEST_SEND);
        intentFilter.addAction(Constant.ACTION_REQUEST_RECEIVE);
        registerReceiver(mReceiverHandler, intentFilter);
        notificationModel = (NotificationModel) getIntent().getSerializableExtra("notification");

        if (notificationModel != null) {
            handlerNotification();
        }

        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list_type.equalsIgnoreCase("Near By")) {
                } else {
                    list_type = "Near By";
                    buttonSelection();
                    HomeFragement homeFragement = new HomeFragement();
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.replace(R.id.content_frame, homeFragement);
                    t.commit();
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list_type.equalsIgnoreCase("Send")) {
                } else {
                    list_type = "Send";
                    buttonSelection();
                    SendFragment sendFragment = new SendFragment();
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.replace(R.id.content_frame, sendFragment);
                    t.commit();
                }
            }
        });

        btn_received.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list_type.equalsIgnoreCase("Receive")) {
                } else {
                    list_type = "Receive";
                    buttonSelection();
                    ReceiveFragment receiveFragment = new ReceiveFragment();
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.replace(R.id.content_frame, receiveFragment);
                    t.commit();
                }
            }
        });

        btn_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list_type.equalsIgnoreCase("Now")) {
                } else {
                    list_type = "Now";
                    buttonSelection();
                    NowFragment nowFragment = new NowFragment();
                    FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    t.replace(R.id.content_frame, nowFragment);
                    t.commit();
                }
            }
        });

    }

    private void InitView() {

        btn_all = (TextView) findViewById(R.id.btn_all);
        btn_send = (TextView) findViewById(R.id.btn_send);
        btn_received = (TextView) findViewById(R.id.btn_received);
        btn_now = (TextView) findViewById(R.id.btn_now);

        HomeFragement homeFragement = new HomeFragement();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.content_frame, homeFragement);
        t.commit();

        buttonSelection();
    }


    private void buttonSelection() {

        if (list_type.equalsIgnoreCase("Near By")) {
            btn_all.setTextColor(Color.parseColor("#FFFFFF"));
            btn_all.setBackgroundColor(Color.parseColor("#4d5056"));

            btn_send.setTextColor(Color.parseColor("#000000"));
            btn_send.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_send.setBackgroundResource(R.drawable.layout_corner_black);

            btn_received.setTextColor(Color.parseColor("#000000"));
            btn_received.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_received.setBackgroundResource(R.drawable.layout_corner_black);

            btn_now.setTextColor(Color.parseColor("#000000"));
            btn_now.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_now.setBackgroundResource(R.drawable.layout_corner_black);

        } else if (list_type.equalsIgnoreCase("Send")) {
            btn_send.setTextColor(Color.parseColor("#FFFFFF"));
            btn_send.setBackgroundColor(Color.parseColor("#4d5056"));

            btn_all.setTextColor(Color.parseColor("#000000"));
            btn_all.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_all.setBackgroundResource(R.drawable.layout_corner_black);

            btn_received.setTextColor(Color.parseColor("#000000"));
            btn_received.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_received.setBackgroundResource(R.drawable.layout_corner_black);

            btn_now.setTextColor(Color.parseColor("#000000"));
            btn_now.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_now.setBackgroundResource(R.drawable.layout_corner_black);

        } else if (list_type.equalsIgnoreCase("Receive")) {
            btn_received.setTextColor(Color.parseColor("#FFFFFF"));
            btn_received.setBackgroundColor(Color.parseColor("#4d5056"));

            btn_send.setTextColor(Color.parseColor("#000000"));
            btn_send.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_send.setBackgroundResource(R.drawable.layout_corner_black);

            btn_all.setTextColor(Color.parseColor("#000000"));
            btn_all.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_all.setBackgroundResource(R.drawable.layout_corner_black);

            btn_now.setTextColor(Color.parseColor("#000000"));
            btn_now.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_now.setBackgroundResource(R.drawable.layout_corner_black);

        } else if (list_type.equalsIgnoreCase("Now")) {
            btn_now.setTextColor(Color.parseColor("#FFFFFF"));
            btn_now.setBackgroundColor(Color.parseColor("#4d5056"));

            btn_send.setTextColor(Color.parseColor("#000000"));
            btn_send.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_send.setBackgroundResource(R.drawable.layout_corner_black);

            btn_all.setTextColor(Color.parseColor("#000000"));
            btn_all.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_all.setBackgroundResource(R.drawable.layout_corner_black);

            btn_received.setTextColor(Color.parseColor("#000000"));
            btn_received.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btn_received.setBackgroundResource(R.drawable.layout_corner_black);
        }

    }

    @Override
    public void onBackPressed() {
        Log.d("tag", "click into back");
        HomeActivity.this.finish();
    }

}
