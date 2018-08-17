package com.gt.datingapp.fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gt.datingapp.R;
import com.gt.datingapp.adapter.NowUserListAdapter;
import com.gt.datingapp.api.ApiClient;
import com.gt.datingapp.api.ApiInterface;
import com.gt.datingapp.model.SendReceiveUserListResponse;
import com.gt.datingapp.model.SendReceiveUserModel;
import com.gt.datingapp.model.UserModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitesh on 02/04/18.
 */

public class NowFragment extends Fragment {

    private View rootview;
    private SharedPreferences sharedpreferences;
    private ProgressDialog pd;
    private LinearLayout lay_nodata;
    private RecyclerView userList;
    private TextView txt_nodata;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.activity_main, container, false);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        pd = new ProgressDialog(getActivity());
        pd.setIndeterminate(true);
        pd.setMessage("Loading...");

        InitView();

        getNowUserList();

        return rootview;
    }

    private void InitView() {
        userList = (RecyclerView) rootview.findViewById(R.id.userList);
        userList.setLayoutManager(new LinearLayoutManager(getActivity()));
        lay_nodata = (LinearLayout) rootview.findViewById(R.id.lay_nodata);
        txt_nodata = (TextView) rootview.findViewById(R.id.txt_nodata);
    }

    private void getNowUserList() {

        pd.show();
        Map<String, String> map = new HashMap<>();
        map.put("user_id", sharedpreferences.getString("user_id", ""));
        map.put("accept_type", "now");

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<SendReceiveUserListResponse> sendReceiveUserListResponseCall = apiService.getNowListResponse(map);

        sendReceiveUserListResponseCall.enqueue(new Callback<SendReceiveUserListResponse>() {
            @Override
            public void onResponse(Call<SendReceiveUserListResponse> call, Response<SendReceiveUserListResponse> response) {
                pd.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getSuccess()) {
                        if (response.body().getData() != null) {
                            SendReceiveUserModel sendReceiveUserModel = response.body().getData();
                            if (sendReceiveUserModel != null) {
                                List<UserModel> userModelList = sendReceiveUserModel.getUsers();
                                if (userModelList.size() > 0) {
                                    userList.setVisibility(View.VISIBLE);
                                    NowUserListAdapter userListAdapter = new NowUserListAdapter(getActivity(), userModelList);
                                    userList.setAdapter(userListAdapter);
                                    lay_nodata.setVisibility(View.GONE);
                                } else {
                                    userList.setVisibility(View.GONE);
                                    lay_nodata.setVisibility(View.VISIBLE);
                                    txt_nodata.setText("No user");
                                }
                            } else {
                                userList.setVisibility(View.GONE);
                                lay_nodata.setVisibility(View.VISIBLE);
                                txt_nodata.setText("No user");
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SendReceiveUserListResponse> call, Throwable t) {
                pd.dismiss();
                Log.d("tag", "Error:" + t.toString());
            }
        });

    }

}
