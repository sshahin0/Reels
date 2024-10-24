package com.sunfun.slideshow.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunfun.slideshow.R;
import com.sunfun.slideshow.adapters.GalleryMusicListAdapter;
import com.sunfun.slideshow.pojo.Contents;
import com.sunfun.slideshow.utils.AnimationUtils;
import com.sunfun.slideshow.viewmodel.GalleryListViewModel;

public class GalleryListFragment extends Fragment {

    private static Contents contents;
    private RecyclerView musicListRecyclerView;
    private TextView titleText;
    private ImageButton backBtn;
    private GalleryMusicListAdapter adapter;
    private GalleryListViewModel galleryListViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findAllViews();
        initToolbar();
        initViewModel();
        initRecyclerView();
    }

    private void initViewModel() {
        galleryListViewModel = ViewModelProviders.of(this).get(GalleryListViewModel.class);
    }

    private void initToolbar() {
        titleText.setText(contents.getName());
        backBtn.setOnClickListener(view -> finish("back"));
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        musicListRecyclerView.setLayoutManager(layoutManager);
        musicListRecyclerView.setLayoutManager(layoutManager);
        musicListRecyclerView.setHasFixedSize(true);
        musicListRecyclerView.setNestedScrollingEnabled(false);
        new AnimationUtils(getContext()).setOverScrollAnimation(musicListRecyclerView);

        adapter = new GalleryMusicListAdapter(getContext(), contents, galleryListViewModel, this);
        musicListRecyclerView.setAdapter(adapter);
    }

    private void findAllViews() {
        musicListRecyclerView = getView().findViewById(R.id.musicListRecyclerView);
        titleText = getView().findViewById(R.id.titleText);
        backBtn = getView().findViewById(R.id.backBtn);
    }

    public static GalleryListFragment getInstance(Bundle bundle) {
        contents = bundle.getParcelable("contents");
        return new GalleryListFragment();
    }

    public void finish(String status){
        if(adapter.simpleExoPlayer!=null) adapter.simpleExoPlayer.release();
        getActivity().getSupportFragmentManager().popBackStack();
        Intent intent = new Intent();
        intent.putExtra("status", status);
        assert getTargetFragment() != null;
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                intent
        );
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onBackPressed() {
        finish("back");
    }
}
