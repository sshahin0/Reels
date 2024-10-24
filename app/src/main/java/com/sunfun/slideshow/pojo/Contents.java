package com.sunfun.slideshow.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class Contents implements Parcelable {
    private ArrayList<Songs> songs;

    private String name;

    public ArrayList<Songs> getSongs () {
        return songs;
    }

    public void setSongs (ArrayList<Songs> songs)
    {
        this.songs = songs;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public Contents(){ }

    protected Contents(Parcel in) {
        if (in.readByte() == 0x01) {
            songs = new ArrayList<Songs>();
            in.readList(songs, Songs.class.getClassLoader());
        } else {
            songs = null;
        }
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (songs == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(songs);
        }
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Contents> CREATOR = new Parcelable.Creator<Contents>() {
        @Override
        public Contents createFromParcel(Parcel in) {
            return new Contents(in);
        }

        @Override
        public Contents[] newArray(int size) {
            return new Contents[size];
        }
    };



}