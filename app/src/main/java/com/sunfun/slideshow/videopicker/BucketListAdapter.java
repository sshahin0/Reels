package com.sunfun.slideshow.videopicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunfun.slideshow.R;

import java.util.List;

public class BucketListAdapter extends RecyclerView.Adapter<BucketListAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    private List<BucketInfo> buckets;

    public BucketListAdapter(Context context, List<BucketInfo> buckets) {
        inflater = LayoutInflater.from(context);
        this.buckets = buckets;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.single_bucket_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        viewHolder.folderNameText.setText(buckets.get(i).getBucketName());
        viewHolder.videoCountText.setText(buckets.get(i).getVideoCount());
        viewHolder.itemImage.setImageBitmap(buckets.get(i).getBitmap());

        if (buckets.get(i).getBitmap1() != null) {
            viewHolder.itemImage1.setVisibility(View.VISIBLE);
            viewHolder.itemImage1.setImageBitmap(buckets.get(i).getBitmap1());
        } else {
            viewHolder.itemImage1.setVisibility(View.INVISIBLE);
        }

        if (buckets.get(i).getBitmap2() != null) {
            viewHolder.itemImage2.setVisibility(View.VISIBLE);
            viewHolder.itemImage2.setImageBitmap(buckets.get(i).getBitmap2());
        } else {
            viewHolder.itemImage2.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return buckets.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage = itemView.findViewById(R.id.itemImage);
        ImageView itemImage1 = itemView.findViewById(R.id.itemImage1);
        ImageView itemImage2 = itemView.findViewById(R.id.itemImage2);

        TextView folderNameText = itemView.findViewById(R.id.folderNameText);
        TextView videoCountText = itemView.findViewById(R.id.videoCountText);

        public MyViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(v -> ((VideoPickerActivity) itemView.getContext()).closeTopSheet(folderNameText.getText().toString()));
        }
    }
}
