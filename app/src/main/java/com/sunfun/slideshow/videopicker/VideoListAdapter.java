package com.sunfun.slideshow.videopicker;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sunfun.slideshow.MainActivity;
import com.sunfun.slideshow.R;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


public class VideoListAdapter extends CursorRecyclerViewAdapter<VideoListAdapter.MyViewHolder> {

    private boolean isClicked;
    private LayoutInflater inflater;
    private LruCache<String, Bitmap> memoryCache;
    private String TAG = VideoListAdapter.class.getName();
    private int previousSelectedVideoId;


    VideoListAdapter(Context context, Cursor videoCursor, int id) {
        super(context, videoCursor);
        inflater = LayoutInflater.from(context);
        isClicked = false;
        previousSelectedVideoId = id;
        initCache();
    }

    private void initCache() {
        final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.single_video_item, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final Cursor cursor) {

        OnBitmapAddedListener listener = time -> {
            holder.durationText.setText(time);
            holder.durationText.setVisibility(View.VISIBLE);
        };

        long millis = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));

        String time = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        if(time.indexOf("00:") == 0){ time = time.replaceFirst("00:",""); }

        int videoId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));
        loadBitmap(holder.thumbImage.getContext(), videoId, holder.thumbImage, listener, time);
        holder.setSelectedPath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)));

        holder.id = videoId;
        if( previousSelectedVideoId!=-1 && videoId == previousSelectedVideoId){
            holder.border.setVisibility(View.VISIBLE);
        }else {
            holder.border.setVisibility(View.INVISIBLE);
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private String selectedPath;
        private int id;
        ImageView thumbImage = itemView.findViewById(R.id.thumbImage);
        TextView durationText = itemView.findViewById(R.id.durationText);
        FrameLayout border = itemView.findViewById(R.id.border);

        MyViewHolder(final View itemView) {
            super(itemView);
            thumbImage.setOnClickListener(v -> {
                if(isClicked) return;
                isClicked = true;
                previousSelectedVideoId = id;
                notifyDataSetChanged();
                Intent intent = new Intent(itemView.getContext(), MainActivity.class);
                intent.putExtra("path", selectedPath);
                intent.putExtra("video_id", id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                itemView.getContext().startActivity(intent);
            });
        }

        void setSelectedPath(String selectedPath){
            this.selectedPath = selectedPath;
        }
    }


    private void loadBitmap(Context context, int resId, ImageView imageView, OnBitmapAddedListener listener, String time) {
        final String imageKey = String.valueOf(resId);
        final Bitmap bitmap = getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            listener.onBitmapAdded(time);
        } else {
            LoadImage loadImage = new LoadImage(resId, context.getContentResolver(), imageView, listener, time);
            loadImage.execute();
        }
    }


    private class LoadImage extends AsyncTask<String, Bitmap, Bitmap> {

        OnBitmapAddedListener listener;
        ContentResolver contentResolver;
        ImageView imageView;
        int resId;
        String time;

        LoadImage(int resId, ContentResolver contentResolver, ImageView imageView, OnBitmapAddedListener listener, String time) {
            this.contentResolver = contentResolver;
            this.imageView = imageView;
            this.resId = resId;
            this.listener = listener;
            this.time = time;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            return MediaStore.Video.Thumbnails.getThumbnail(
                    contentResolver,
                    resId,
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageView.setImageBitmap(bitmap);

            if(bitmap != null){
                addBitmapToMemoryCache(String.valueOf(resId), bitmap);
            }

            listener.onBitmapAdded(time);

        }
    }


    interface OnBitmapAddedListener {
        void onBitmapAdded(String time);
    }
}