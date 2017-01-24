package com.owncloud.android.lib.common;

/**
 * Created by mdjanic on 24/01/2017.
 */

public class UserInfo {
    public String id;
    public Boolean enabled;
    public String displayName;
    public String email;
    public String phone;
    public String address;
    public String webpage;
    public String twitter;
    public Quota quota;

    public UserInfo() {
    }

    public UserInfo(String id, Boolean enabled, String displayName, String email, String phone, String address,
                    String webpage, String twitter, Quota quota) {
        this.id = id;
        this.enabled = enabled;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.webpage = webpage;
        this.twitter = twitter;
        this.quota = quota;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebpage() {
        return webpage;
    }

    public void setWebpage(String webpage) {
        this.webpage = webpage;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public Quota getQuota() {
        return quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
