package com.owncloud.android.lib.common;

/**
 * Created by mdjanic on 24/01/2017.
 */

public class Quota {
    public long free;
    public long used;
    public long total;
    public long quota;
    public double relative;

    public Quota() {
    }

    public Quota(long free, long used, long total, double relative, long quota) {

        this.free = free;
        this.used = used;
        this.total = total;
        this.quota = quota;
        this.relative = relative;
    }

    public long getFree() {
        return free;
    }

    public void setFree(long free) {
        this.free = free;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getQuota() {
        return quota;
    }

    public void setQuota(long quota) {
        this.quota = quota;
    }

    public double getRelative() {
        return relative;
    }

    public void setRelative(double relative) {
        this.relative = relative;
    }
}
