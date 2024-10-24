package com.sunfun.slideshow.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.sunfun.slideshow.pojo.VideoCropData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Map;

public class MusicGalleryViewModel extends ViewModel {

    private MutableLiveData<VideoCropData> videoCropDataMutableLiveData;
    private MutableLiveData<String> result;

    public MutableLiveData<VideoCropData> parseData(String musicJson) {
        if (videoCropDataMutableLiveData == null) { videoCropDataMutableLiveData = new MutableLiveData<>(); }
        Gson gson = new Gson();
        VideoCropData videoCropData = gson.fromJson(musicJson, VideoCropData.class);
        videoCropDataMutableLiveData.setValue(videoCropData);
        return videoCropDataMutableLiveData;
    }

    public MutableLiveData<String> updateMusicJson() {
        result = new MutableLiveData<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("data");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Map<String, String> value = (Map<String, String>) dataSnapshot.getValue();
                if(value!=null) {
                    String data = "{ \"data\" : " + new JSONObject(value) + "}";
                    result.setValue(data);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("simuldb", "Failed to read value.", error.toException());
            }
        });

        return result;
    }
}
