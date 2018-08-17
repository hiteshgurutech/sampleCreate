package com.gt.datingapp.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.gt.datingapp.R;
import com.gt.datingapp.api.ApiClient;
import com.gt.datingapp.api.ApiInterface;
import com.gt.datingapp.model.ForgotPasswordResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Hitesh on 24/6/17.
 */

public class ForgotPopup extends Dialog {


    private AppCompatEditText editEmail;
    private Context mContext;
    private ProgressDialog pd;

    public ForgotPopup(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_forgot_password);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().getAttributes().windowAnimations = R.style.SlideAnimation;
        AppCompatButton btnCancel = (AppCompatButton) findViewById(R.id.forgot_btnCancel);
        AppCompatButton btnSubmit = (AppCompatButton) findViewById(R.id.forgot_btnSubmit);
        editEmail = (AppCompatEditText) findViewById(R.id.forgot_editEmail);

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
                        if (new Internet(mContext).isInternetOn()) {
                            callForgotApi();
                        } else {
                            Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Please enter valid email", Toast.LENGTH_SHORT).show();
                    }
                } else if (editEmail.getText().toString().trim().equals("")) {
                    Toast.makeText(getContext(), "Please enter email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void callForgotApi() {

        pd = new ProgressDialog(mContext);
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");
        pd.show();

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<ForgotPasswordResponse> forgotPasswordResponseCall = apiService.getForgotPassword(editEmail.getText().toString());

        forgotPasswordResponseCall.enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        dismiss();
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                pd.dismiss();
                Log.d("tag", "Error: " + t.toString());
            }
        });


    }

}
