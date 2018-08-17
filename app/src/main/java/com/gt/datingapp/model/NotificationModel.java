package com.gt.datingapp.model;

import java.io.Serializable;

/**
 * Created by Hitesh on 25/10/17.
 */

public class NotificationModel implements Serializable {

    String data;
    String type;
    SendRequestModel sendRequestModel;
    AcceptRequestModel acceptRequestModel;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public SendRequestModel getSendRequestModel() {
        return sendRequestModel;
    }

    public void setSendRequestModel(SendRequestModel sendRequestModel) {
        this.sendRequestModel = sendRequestModel;
    }

    public AcceptRequestModel getAcceptRequestModel() {
        return acceptRequestModel;
    }

    public void setAcceptRequestModel(AcceptRequestModel acceptRequestModel) {
        this.acceptRequestModel = acceptRequestModel;
    }


}
