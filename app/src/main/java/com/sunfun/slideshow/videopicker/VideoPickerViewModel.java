package com.sunfun.slideshow.videopicker;

import android.app.Application;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class VideoPickerViewModel extends AndroidViewModel {

    private String TAG = VideoPickerViewModel.class.getName();
    public MutableLiveData<Cursor> videoCursorMutable = new MutableLiveData<>();
    public MutableLiveData<List<BucketInfo>> bucketsMutable = new MutableLiveData<>();
    private ArrayList<BucketInfo> buckets;

    public VideoPickerViewModel(@NonNull Application application) {
        super(application);
    }

    public void getVideos(final String selectedFolder, final String viewGalleryOption) {
        new Thread(() -> {
            Cursor cursor = initVideoCursor(selectedFolder, viewGalleryOption);
            OnVideoCursorLoad(cursor);
        }).start();
    }

    public void getBuckets(final String viewGalleryOption) {
        new Thread(() -> {
            Cursor cursor = initBucketCursor(viewGalleryOption);
            OnBucketCursorLoad(cursor);
        }).start();
    }


    private void OnVideoCursorLoad(Cursor cursor) {
        videoCursorMutable.postValue(cursor);
    }

    private void OnBucketCursorLoad(Cursor cursor) {
        buckets = new ArrayList<>();
        HashMap<String, Integer> hashMap = new HashMap<>();
        HashMap<String, Integer> idHashMap = new HashMap<>();
        addFullBuckets(cursor);

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
            Integer value = hashMap.get(bucketDisplayName);
            if (value == null) {

                value = 1;
                BucketInfo bucketInfo = new BucketInfo();

                bucketInfo.setBitmap(getBitmap(cursor));
                bucketInfo.setBucketName(bucketDisplayName);
                buckets.add(bucketInfo);

                idHashMap.put(bucketDisplayName, buckets.size()-1);

            } else {
                value = value + 1;
                int bucketId = idHashMap.get(bucketDisplayName);

                if(buckets.get(bucketId).getBitmap1() == null){
                    buckets.get(bucketId).setBitmap1(getBitmap(cursor));
                } else if(buckets.get(bucketId).getBitmap2() == null){
                    buckets.get(bucketId).setBitmap2(getBitmap(cursor));
                }

            }
            hashMap.put(bucketDisplayName, value);
        }

        for (int i = 1; i < buckets.size(); i++) {
            boolean isRemoved = false;
            if((hashMap.get(buckets.get(i).getBucketName()) + "").equals("1")){
                if(buckets.get(i).getBitmap() == null){
                    buckets.get(i).setVideoCount(hashMap.get(buckets.get(i).getBucketName()) + "");
                    hashMap.remove(buckets.get(i).getBucketName());
                    buckets.remove(i);
                    isRemoved = true;
                    i=i-1;
                }
            }
            if(!isRemoved) {
                buckets.get(i).setVideoCount(hashMap.get(buckets.get(i).getBucketName()) + "");
            }
        }

        bucketsMutable.postValue(buckets);
    }

    private Bitmap getBitmap(Cursor cursor) {
        return MediaStore.Video.Thumbnails.getThumbnail(
                getApplication().getContentResolver(),
                cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID)),
                MediaStore.Video.Thumbnails.MICRO_KIND,
                null);
    }

    private void addFullBuckets(Cursor cursor) {
        cursor.moveToFirst();
        try {
            BucketInfo fullBucket = new BucketInfo();
            fullBucket.setBitmap(getBitmap(cursor));

            cursor.moveToNext();
            fullBucket.setBitmap1(getBitmap(cursor));

            cursor.moveToNext();
            fullBucket.setBitmap2(getBitmap(cursor));

            fullBucket.setBucketName("All Videos");
            fullBucket.setVideoCount(cursor.getCount() + "");
            buckets.add(fullBucket);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            cursor.moveToFirst();
        }
    }

    private Cursor initBucketCursor(String viewGalleryOption) {
        Uri videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.VideoColumns._ID
        };

        //String  BUCKET_GROUP_BY = "1) GROUP BY 1,(2"; //This also give perfect result
        String BUCKET_GROUP_BY = "1) GROUP BY " + MediaStore.Video.VideoColumns.BUCKET_ID + ", (" + MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME;

        return getApplication().getContentResolver().query(
                videos,
                projection,
                null,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " " + viewGalleryOption // Sort order. (ASC)(DESC)
        );
    }

    private Cursor initVideoCursor(String selectedFolder, String viewGalleryOption) {
        // Get relevant columns for use later.
        String[] projection = {
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.TITLE,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA
        };

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        if (selectedFolder.equals("")) {
            String where = MediaStore.Video.VideoColumns.DURATION + " >= 5000";
            String selection = MediaStore.Video.VideoColumns._ID + " and " + where;


            return getApplication().getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    null,
                    MediaStore.Files.FileColumns.DATE_ADDED + " " + viewGalleryOption // Sort order. (ASC)(DESC)
            );

        } else {
            String where = MediaStore.Video.VideoColumns.DURATION + " >= 5000";
            String selection = MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME + " =? and " + where;
            String[] selectionArgs = new String[]{selectedFolder};

            return getApplication().getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    MediaStore.Files.FileColumns.DATE_ADDED + " " + viewGalleryOption // Sort order. (ASC)(DESC)
            );
        }
    }


}
