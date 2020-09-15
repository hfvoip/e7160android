package com.jhearing.e7160sl.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Create by dongli
 * Create date 2020/8/5
 * desc：
 */
public class shopInfo implements Parcelable {

    private String title;
    private String content;
    private String address;

    public shopInfo() {
    }

    protected shopInfo(Parcel in) {
        title = in.readString();
        address = in.readString();
        content = in.readString();
    }

    public static final Creator<shopInfo> CREATOR = new Creator<shopInfo>() {
        @Override
        public shopInfo createFromParcel(Parcel in) {
            return new shopInfo(in);
        }

        @Override
        public shopInfo[] newArray(int size) {
            return new shopInfo[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(address);
        dest.writeString(content);
    }
}
