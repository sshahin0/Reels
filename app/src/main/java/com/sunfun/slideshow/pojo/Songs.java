package com.sunfun.slideshow.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class Songs implements Parcelable {
    private String fn;
    private String dn;
    private boolean isPlaying = false;
    private boolean isDownloading = false;
    private String songPathInStorage;

    public String getSongPathInStorage() {
        return songPathInStorage;
    }

    public void setSongPathInStorage(String songPathInStorage) {
        this.songPathInStorage = songPathInStorage;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public String getFn ()
    {
        return fn;
    }

    public void setFn (String fn)
    {
        this.fn = fn;
    }

    public String getDn ()
    {
        return dn;
    }

    public void setDn (String dn)
    {
        this.dn = dn;
    }

    protected Songs(Parcel in) {
        fn = in.readString();
        dn = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fn);
        dest.writeString(dn);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Songs> CREATOR = new Parcelable.Creator<Songs>() {
        @Override
        public Songs createFromParcel(Parcel in) {
            return new Songs(in);
        }

        @Override
        public Songs[] newArray(int size) {
            return new Songs[size];
        }
    };
}