package com.sunfun.slideshow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.sunfun.slideshow.R;
import com.sunfun.slideshow.pojo.Contents;
import com.sunfun.slideshow.utils.VideoUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class CategoryTopAdapter extends RecyclerView.Adapter<CategoryTopAdapter.MyViewHolder> {

    private final Context context;
    private LayoutInflater inflater;
    private ArrayList<Contents> list;
    private int width;
    private int currentPosition = 0;
    private OnItemClickListener onItemClickListener;

    public CategoryTopAdapter(Context context, ArrayList<Contents> arrayList) {
        super();
        inflater = LayoutInflater.from(context);
        list = arrayList;
        this.context = context;
    }


    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View view = inflater.inflate(R.layout.category_item_view, parent, false);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width= (int) (VideoUtils.getScreenWidth(context)/1.10f);
        params.setMargins(0,0,8,0);
        this.width = params.width;
        view.setLayoutParams(params);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        this.currentPosition = position;
        position = position % list.size();

        if(position == 0){
            holder.thumbImage.setImageResource(R.drawable.editors_choice_song);
        } else if(position == 1){
            holder.thumbImage.setImageResource(R.drawable.top_tracks_song);
        } else {
            holder.thumbImage.setImageResource(R.drawable.just_in_song);
        }

        holder.position = position;

    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public int position;
        ImageView thumbImage = itemView.findViewById(R.id.thumbImage);

        MyViewHolder(final View itemView) {
            super(itemView);
            thumbImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(position);
                }
            });
        }
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }


}