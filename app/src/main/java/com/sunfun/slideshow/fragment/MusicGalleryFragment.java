
package com.sunfun.slideshow.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sunfun.slideshow.R;
import com.sunfun.slideshow.adapters.CategoryBottomAdapter;
import com.sunfun.slideshow.adapters.CategoryMiddleAdapter;
import com.sunfun.slideshow.adapters.CategoryTopAdapter;
import com.sunfun.slideshow.pojo.Contents;
import com.sunfun.slideshow.pojo.Songs;
import com.sunfun.slideshow.pojo.VideoCropData;
import com.sunfun.slideshow.utils.AnimationUtils;
import com.sunfun.slideshow.utils.CustomLinearLayoutManager;
import com.sunfun.slideshow.utils.Prefs;
import com.sunfun.slideshow.viewmodel.MusicGalleryViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class MusicGalleryFragment extends Fragment {

    private ScrollView scrollView;
    private AnimationUtils animationUtils;

    private ConstraintLayout titleBar;
    private ImageButton backBtn;
    private View divider;
    private TextView titleText;
    private int count;
    private RecyclerView categoryTopRecyclerView;
    private RecyclerView categoryMiddleRecyclerView;
    private RecyclerView categoryBottomRecyclerView;
    private MusicGalleryViewModel musicGalleryViewModel;
    private GalleryListFragment galleryListFragment;
    private int LIST_MUSIC_REQUEST;
    private VideoCropData videoCropData;

    public static MusicGalleryFragment getInstance() {
        MusicGalleryFragment fragment = new MusicGalleryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_gallery, container, false);
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        Animation anim = android.view.animation.AnimationUtils.loadAnimation(getActivity(), nextAnim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if(enter){
                    findAllViews();
                    initButtons();
                    initAnimation();
                    initViewModel();
                    updateMusicJson();
                }else {

                }
            }
        });
        return anim;
    }

    private void initViewModel() {
        musicGalleryViewModel = ViewModelProviders.of(this).get(MusicGalleryViewModel.class);
        final MutableLiveData<VideoCropData> liveData = musicGalleryViewModel.parseData(new Prefs(getContext()).getMusicJson());
        liveData.observe(this, new Observer<VideoCropData>() {
            @Override
            public void onChanged(VideoCropData videoCropData) {
                MusicGalleryFragment.this.videoCropData = videoCropData;
                initCategoryTopRecycler();
                initCategoryMiddleRecycler();
                initCategoryBottomRecycler();
                liveData.removeObserver(this);
            }
        });
    }

    private void initCategoryBottomRecycler() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        categoryBottomRecyclerView.setLayoutManager(new CustomLinearLayoutManager(getContext()));
        categoryBottomRecyclerView.setLayoutManager(layoutManager);
        categoryBottomRecyclerView.setHasFixedSize(true);
        categoryBottomRecyclerView.setNestedScrollingEnabled(false);
        final CategoryBottomAdapter categoryBottomAdapter = new CategoryBottomAdapter(getContext(), videoCropData.getData().getSongfeatures().getBottom_cat().getContents());
        categoryBottomRecyclerView.setAdapter(categoryBottomAdapter);

        categoryBottomAdapter.setOnItemClickListener(position -> {
            Bundle args = new Bundle();
            args.putParcelable("contents", videoCropData.getData().getSongfeatures().getBottom_cat().getContents().get(position));

            galleryListFragment = GalleryListFragment.getInstance(args);
            galleryListFragment.setTargetFragment(MusicGalleryFragment.this, LIST_MUSIC_REQUEST);

            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.bottom_up, android.R.anim.fade_out, android.R.anim.fade_out, R.anim.bottom_down)
                    .add(((MainFragment) getTargetFragment()).fragmentContainer.getId(), galleryListFragment)
                    .addToBackStack("gallery_list_fragment")
                    .commit();
        });

    }

    private void initCategoryMiddleRecycler() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        categoryMiddleRecyclerView.setLayoutManager(new CustomLinearLayoutManager(getContext()));
        categoryMiddleRecyclerView.setLayoutManager(layoutManager);
        categoryMiddleRecyclerView.setHasFixedSize(true);
        categoryMiddleRecyclerView.setNestedScrollingEnabled(false);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("trending");
        arrayList.add("featured");
        arrayList.add("popular");
        arrayList.add("best new");

        final CategoryMiddleAdapter categoryMiddleAdapter = new CategoryMiddleAdapter(getContext(), arrayList, videoCropData.getData().getSongfeatures().getBottom_cat().getContents());
        categoryMiddleRecyclerView.setAdapter(categoryMiddleAdapter);

        categoryMiddleAdapter.setOnItemClickListener(position -> {
            ArrayList<Songs> list = new ArrayList<>();
            int index = videoCropData.getData().getSongfeatures().getBottom_cat().getContents().size() - (2 * position + 1);
            list.addAll(videoCropData.getData().getSongfeatures().getBottom_cat().getContents().get(index).getSongs());
            list.addAll(videoCropData.getData().getSongfeatures().getBottom_cat().getContents().get(index - 1).getSongs());

            Contents contents = new Contents();
            contents.setSongs(list);
            contents.setName("");

            Bundle args = new Bundle();
            args.putParcelable("contents", contents);

            galleryListFragment = GalleryListFragment.getInstance(args);
            galleryListFragment.setTargetFragment(MusicGalleryFragment.this, LIST_MUSIC_REQUEST);

            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.bottom_up, android.R.anim.fade_out, android.R.anim.fade_out, R.anim.bottom_down)
                    .add(((MainFragment) getTargetFragment()).fragmentContainer.getId(), galleryListFragment)
                    .addToBackStack("gallery_list_fragment")
                    .commit();
        });
    }

    private void initCategoryTopRecycler() {
        final CustomLinearLayoutManager layoutManager = new CustomLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        final SnapHelper snapHelper = new PagerSnapHelper();
        categoryTopRecyclerView.setLayoutManager(new CustomLinearLayoutManager(getContext()));
        categoryTopRecyclerView.setLayoutManager(layoutManager);
        snapHelper.attachToRecyclerView(categoryTopRecyclerView);
        categoryTopRecyclerView.setHasFixedSize(true);
        final CategoryTopAdapter categoryAdapter = new CategoryTopAdapter(getContext(), videoCropData.getData().getSongfeatures().getTop_cat().getContents());
        categoryTopRecyclerView.setAdapter(categoryAdapter);
        categoryTopRecyclerView.getLayoutManager().scrollToPosition(Integer.MAX_VALUE / 2);

        categoryTopRecyclerView.post(() -> {
            View view = layoutManager.findViewByPosition(Integer.MAX_VALUE / 2);
            int[] snapDistance = snapHelper.calculateDistanceToFinalSnap(layoutManager, view);
            if (snapDistance[0] != 0 || snapDistance[1] != 0) {
                categoryTopRecyclerView.scrollBy(snapDistance[0], snapDistance[1]);
            }
        });

        categoryAdapter.setOnItemClickListener(position -> {
            Bundle args = new Bundle();
            args.putParcelable("contents", videoCropData.getData().getSongfeatures().getTop_cat().getContents().get(position));

            galleryListFragment = GalleryListFragment.getInstance(args);
            galleryListFragment.setTargetFragment(MusicGalleryFragment.this, LIST_MUSIC_REQUEST);

            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.bottom_up, android.R.anim.fade_out, android.R.anim.fade_out, R.anim.bottom_down)
                    .add(((MainFragment) getTargetFragment()).fragmentContainer.getId(), galleryListFragment)
                    .addToBackStack("gallery_list_fragment")
                    .commit();
        });


        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            public void run() {
                int currentPosition = ((CustomLinearLayoutManager) categoryTopRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                categoryTopRecyclerView.getLayoutManager().smoothScrollToPosition(categoryTopRecyclerView, null, currentPosition + 2);
                if (count++ < Integer.MAX_VALUE) {
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    private void initButtons() {
        backBtn.setOnClickListener(v -> finish("back"));
    }

    private void finish(String status) {
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

    private void initAnimation() {
        animationUtils = new AnimationUtils(getContext());
        animationUtils.setScrollViewAnimation(scrollView, titleBar, divider, titleText);
        animationUtils.setOverScrollAnimation(scrollView);
    }

    private void findAllViews() {
        scrollView = getView().findViewById(R.id.scrollView);
        titleBar = getView().findViewById(R.id.titleBar);
        backBtn = getView().findViewById(R.id.backBtn);
        divider = getView().findViewById(R.id.divider);
        titleText = getView().findViewById(R.id.titleText);
        categoryTopRecyclerView = getView().findViewById(R.id.categoryTopRecyclerView);
        categoryMiddleRecyclerView = getView().findViewById(R.id.categoryMiddleRecyclerView);
        categoryBottomRecyclerView = getView().findViewById(R.id.categoryBottomRecyclerView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity().getSupportFragmentManager().beginTransaction().remove(galleryListFragment);
        galleryListFragment = null;
        if(data.getStringExtra("status").equals("done")){
            finish("done");
        }
    }

    private void updateMusicJson() {
        musicGalleryViewModel.updateMusicJson().observe(this, data -> new Prefs(getContext()).setMusicJson(data));
    }

    public void onBackPressed() {
        if(galleryListFragment!=null) galleryListFragment.onBackPressed();
        else finish("back");
    }
}
