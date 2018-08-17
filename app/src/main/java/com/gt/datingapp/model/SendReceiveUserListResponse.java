
package com.gt.datingapp.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendReceiveUserListResponse {

    @SerializedName("data")
    @Expose
    private SendReceiveUserModel data;
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("message")
    @Expose
    private String message;

    public SendReceiveUserModel getData() {
        return data;
    }

    public void setData(SendReceiveUserModel data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
