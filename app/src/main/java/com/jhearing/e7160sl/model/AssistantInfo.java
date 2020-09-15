package com.jhearing.e7160sl.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Create by dongli
 * Create date 2020/8/5
 * desc：
 */
public class AssistantInfo implements Parcelable {

    private String title;
    private String content;

    public AssistantInfo() {
    }

    protected AssistantInfo(Parcel in) {
        title = in.readString();
        content = in.readString();
    }

    public static final Creator<AssistantInfo> CREATOR = new Creator<AssistantInfo>() {
        @Override
        public AssistantInfo createFromParcel(Parcel in) {
            return new AssistantInfo(in);
        }

        @Override
        public AssistantInfo[] newArray(int size) {
            return new AssistantInfo[size];
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(content);
    }
}
