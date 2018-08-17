package com.gt.datingapp.api;


import com.gt.datingapp.model.ForgotPasswordResponse;
import com.gt.datingapp.model.GetVerificationCode;
import com.gt.datingapp.model.RegisterResponse;
import com.gt.datingapp.model.RequestSendResponse;
import com.gt.datingapp.model.SendReceiveUserListResponse;
import com.gt.datingapp.model.SendVerificationCode;
import com.gt.datingapp.model.UserlistResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("?api=register")
    Call<RegisterResponse> getRegisterResponse(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=login")
    Call<RegisterResponse> getLoginResponse(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=get_userlist")
    Call<UserlistResponse> getUserListResponse(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=send_request")
    Call<RequestSendResponse> getRequestSend(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=request_accepted")
    Call<RequestSendResponse> getAcceptResponse(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=reject_request")
    Call<RequestSendResponse> getRejectResponse(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=my_accepted_list")
    Call<SendReceiveUserListResponse> getSendListResponse(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=my_accepted_list")
    Call<SendReceiveUserListResponse> getReceivedListResponse(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=my_accepted_list")
    Call<SendReceiveUserListResponse> getNowListResponse(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("?api=change_email")
    Call<RegisterResponse> changeEmail(@Field("email") String email,@Field("user_id") String user_id);


    @FormUrlEncoded
    @POST("?api=forgot_password")
    Call<ForgotPasswordResponse> getForgotPassword(@Field("email") String email);

    @FormUrlEncoded
    @POST("?api=active_account")
    Call<GetVerificationCode> getVerificationcode(@Field("user_id") String email);

    @FormUrlEncoded
    @POST("?api=send_activation_code")
    Call<SendVerificationCode> sendVerificationcode(@FieldMap Map<String, String> map);


}
