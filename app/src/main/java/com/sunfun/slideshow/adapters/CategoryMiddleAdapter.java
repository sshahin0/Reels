package com.sunfun.slideshow.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunfun.slideshow.R;
import com.sunfun.slideshow.pojo.Contents;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class CategoryMiddleAdapter extends RecyclerView.Adapter<CategoryMiddleAdapter.MyViewHolder> {

    private final Context context;
    private final TypedArray array;
    private final ArrayList<Contents> contents;
    private LayoutInflater inflater;
    private ArrayList<String> list;
    private int width;
    private CategoryTopAdapter.OnItemClickListener onItemClickListener;

    public CategoryMiddleAdapter(Context context, ArrayList<String> arrayList, ArrayList<Contents> contents) {
        super();
        inflater = LayoutInflater.from(context);
        list = arrayList;
        this.context = context;
        this.contents = contents;
        array = context.getResources().obtainTypedArray(R.array.icon_categories_best_of_week);
    }


    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View view = inflater.inflate(R.layout.category_item_view, parent, false);
       /* ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width= (int) (VideoUtils.getScreenWidth(context)/1.10f);
        params.setMargins(0,0,8,0);
        this.width = params.width;*
        view.setLayoutParams(params);*/
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.thumbImage.setImageResource(array.getResourceId(position, 0));
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        int position;
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

    public void setOnItemClickListener(CategoryTopAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }



}