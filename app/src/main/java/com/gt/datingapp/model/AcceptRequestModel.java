package com.gt.datingapp.model;

import java.io.Serializable;

/**
 * Created by Hitesh on 25/10/17.
 */

public class AcceptRequestModel implements Serializable {

    String profile_photo;
    String user_id;
    String name;
    String request_id;
    String email;
    String phone;
    String accept_type;

    public String getAccept_type() {
        return accept_type;
    }

    public void setAccept_type(String accept_type) {
        this.accept_type = accept_type;
    }




    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }


}
