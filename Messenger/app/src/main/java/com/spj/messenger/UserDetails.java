package com.spj.messenger;

import android.widget.Toast;

/**
 * Created by Stenal P Jolly on 09-Mar-15.
 */
public class UserDetails {
    private String vUsername;
    private String vPhone;
    private String vEmailId;
    private String vRegID;
    private String vPhoto;

    public UserDetails(String vUsername,String vEmailId,String vPhone,String vRegID,String vPhoto){
        this.vRegID = vRegID;
        this.vEmailId = vEmailId;
        this.vPhone = vPhone;
        this.vUsername = vUsername.replaceAll("%20"," ");
        this.vPhoto = vPhoto;
    }

    public void setvPhoto(String vPhoto) {
        this.vPhoto = vPhoto;
    }

    public void setvRegID(String vRegID) {
        this.vRegID = vRegID;
    }

    public void setvEmailId(String vEmailId) {
        this.vEmailId = vEmailId;
    }

    public void setvPhone(String vPhone) {
        this.vPhone = vPhone;
    }

    public void setvUsername(String vUsername) {
        this.vUsername = vUsername;
    }

    public String getvRegID() {
        return vRegID;
    }

    public String getvEmailId() {
        return vEmailId;
    }

    public String getvPhone() {
        return vPhone;
    }

    public String getvPhoto() {
        return vPhoto;
    }

    public String getvUsername() {
        return vUsername;
    }
}

