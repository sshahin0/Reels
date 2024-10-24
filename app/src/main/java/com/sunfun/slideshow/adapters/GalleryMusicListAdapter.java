package com.sunfun.slideshow.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sunfun.slideshow.MainActivity;
import com.sunfun.slideshow.R;
import com.sunfun.slideshow.fragment.GalleryListFragment;
import com.sunfun.slideshow.pojo.Contents;
import com.sunfun.slideshow.tools.SongDownloader;
import com.sunfun.slideshow.utils.DialogUtils;
import com.sunfun.slideshow.utils.PathUtils;
import com.sunfun.slideshow.utils.VideoInfo;
import com.sunfun.slideshow.view.dialog.DownloadFragment;
import com.sunfun.slideshow.viewmodel.GalleryListViewModel;
import com.wang.avi.AVLoadingIndicatorView;
import org.jetbrains.annotations.NotNull;
import java.io.File;


public class GalleryMusicListAdapter extends RecyclerView.Adapter<GalleryMusicListAdapter.MyViewHolder> {

    private final Context context;
    private final GalleryListViewModel galleryListViewModel;
    private final GalleryListFragment galleryListFragment;
    private boolean isDownloading = false;
    private LayoutInflater inflater;
    private Contents contents;
    private String mp3Path;
    public SimpleExoPlayer simpleExoPlayer;
    private int lastPlayPosition;

    public GalleryMusicListAdapter(Context context, Contents contents, GalleryListViewModel galleryListViewModel, GalleryListFragment galleryListFragment) {
        super();
        inflater = LayoutInflater.from(context);
        this.contents = contents;
        this.context = context;
        this.galleryListViewModel = galleryListViewModel;
        this.galleryListFragment = galleryListFragment;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View view = inflater.inflate(R.layout.gallery_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        if(position % 2 == 0){
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),R.color.lighter_grey));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),android.R.color.white));
        }

        holder.musicNameText.setText(contents.getSongs().get(position).getDn());
        holder.position = position;

        if(contents.getSongs().get(position).isPlaying()){
            holder.playBtn.setTag("1");
            holder.playBtn.setImageResource(R.drawable.ic_pause_black);
        } else {
            holder.playBtn.setTag("0");
            holder.playBtn.setImageResource(R.drawable.ic_play_black);
        }

        if(contents.getSongs().get(position).isDownloading()){
            holder.playBtn.setVisibility(View.INVISIBLE);
            holder.loadingView.setVisibility(View.VISIBLE);
            holder.loadingView.show();
        } else {
            holder.loadingView.setVisibility(View.INVISIBLE);
            holder.playBtn.setVisibility(View.VISIBLE);
            holder.loadingView.hide();
        }

    }

    @Override
    public int getItemCount() {
        return contents.getSongs().size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        int position;

        TextView musicNameText = itemView.findViewById(R.id.nameText);
        ImageView playBtn = itemView.findViewById(R.id.playBtn);
        AVLoadingIndicatorView loadingView = itemView.findViewById(R.id.loadingView);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        MyViewHolder(final View itemView) {
            super(itemView);
            musicNameText.setOnClickListener(view -> {
                if(isDownloading) return;
                else {
                    isDownloading = true;
                    startLongDownloadWithChecking(position);
                }
            });
            playBtn.setOnClickListener(view -> {
                if(playBtn.getTag().equals("0")){
                    prepareAudio(position);
                } else {
                    stopAudioPlay();
                }
            });
        }

        private void stopAudioPlay() {
            if(simpleExoPlayer == null) return;
            simpleExoPlayer.setPlayWhenReady(false);
            contents.getSongs().get(lastPlayPosition).setPlaying(false);
            notifyDataSetChanged();
        }

        private void showLoadingView(int pos) {
            contents.getSongs().get(pos).setDownloading(true);
            notifyDataSetChanged();
        }

        private void hideLoadingView(int pos) {
            contents.getSongs().get(pos).setDownloading(false);
            notifyDataSetChanged();
        }

        private void startPlayAudio(int pos) {
            if(simpleExoPlayer == null) simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context);
            setMediaSourceAndPrepare();
            simpleExoPlayer.setPlayWhenReady(true);
            simpleExoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if(playbackState == ExoPlayer.STATE_ENDED){
                        stopAudioPlay();
                    }
                }
            });
            contents.getSongs().get(lastPlayPosition).setPlaying(false);
            contents.getSongs().get(pos).setPlaying(true);
            lastPlayPosition = pos;
            notifyDataSetChanged();
        }

        private void setMediaSourceAndPrepare() {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "videoediting"));
            MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mp3Path));
            simpleExoPlayer.prepare(audioSource);
        }

        private void prepareAudio(int pos){
            mp3Path = PathUtils.generateRealPath(contents.getSongs().get(pos).getFn().replace(".mp3", "_demo"), ".mp3");
            if(!new File(mp3Path).exists()){
                startShortDownload(pos);
                return;
            }
            startPlayAudio(pos);
        }

        private void startShortDownload(int pos) {
            if(!((MainActivity)context).connectionAvailable){
                hideLoadingView(pos);
                new DialogUtils().createWarningDialog(context, "Error!", R.string.internet_error_msg, null, null, null);
                return;
            }

            String songTitle = contents.getSongs().get(pos).getFn().replace(".mp3", "_demo.mp3.zip");
            SongDownloader songDownloader = new SongDownloader(context, storageReference, songTitle, pos);
            galleryListViewModel.downloadMusic(songDownloader).observe(galleryListFragment, outSongDownloader -> {
                hideLoadingView(outSongDownloader.getPosition());
                isDownloading = false;
                if(outSongDownloader.getDownloadPath()!=null){
                    prepareAudio(outSongDownloader.getPosition());
                } else {
                    new DialogUtils().createWarningDialog(context, "Error!", R.string.something_went_wrong, null, null, null);
                }
            });
            showLoadingView(pos);
        }

        private void startLongDownloadWithChecking(int pos) {
            String songTitle = contents.getSongs().get(pos).getFn().replace(".mp3", "");
            File file = new File(PathUtils.generateRealPath(songTitle, ".mp3"));

            if(file.exists()){
                VideoInfo.getInstance().setExtraAudioPath(file.getAbsolutePath());
                galleryListFragment.finish("done");
                return;
            }

            if(!((MainActivity)context).connectionAvailable){
                new DialogUtils().createWarningDialog(context, "Error!", R.string.internet_error_msg, null, null, null);
                return;
            }


            songTitle = contents.getSongs().get(pos).getFn().replace(".mp3", ".mp3.zip");
            DownloadFragment downloadFragment = DownloadFragment.newInstance(contents.getSongs().get(pos).getDn());
            downloadFragment.setStyle( DialogFragment.STYLE_NORMAL, R.style.DownloadDialogTheme);
            downloadFragment.setCancelable(false);
            downloadFragment.show(((FragmentActivity)context).getSupportFragmentManager(), "fragment_download");
            SongDownloader songDownloader = new SongDownloader(context, storageReference, songTitle, pos);
            galleryListViewModel.downloadMusic(songDownloader).observe(galleryListFragment, outSongDownloader -> {
                downloadFragment.dismiss();
                if(outSongDownloader.getDownloadPath()!=null){
                    contents.getSongs().get(outSongDownloader.getPosition()).setSongPathInStorage(outSongDownloader.getDownloadPath());
                    VideoInfo.getInstance().setExtraAudioPath(outSongDownloader.getDownloadPath());
                    galleryListFragment.finish("done");
                } else {
                    new DialogUtils().createWarningDialog(context, "Error!", R.string.something_went_wrong, null, null, null);
                }
            });
            galleryListViewModel.getDownloadProgress().observe(galleryListFragment, downloadFragment::setProgressToSeekBar);
            downloadFragment.setOnCancelListener(songDownloader::cancelDownload);
        }
    }

}