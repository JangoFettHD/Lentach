package me.jangofetthd.lentach;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.List;
import java.util.Locale;

import me.jangofetthd.lentach.Services.MusicService;

/**
 * Created by JangoFettHD on 31.08.2016.
 */
public class AudioRecyclerViewAdapter extends RecyclerView.Adapter<AudioRecyclerViewAdapter.ViewHolder> {

    List<VKApiAudio> audios;
    Context context;

    public AudioRecyclerViewAdapter(List<VKApiAudio> audios, Context context) {
        this.audios = audios;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.player, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final VKApiAudio audio = audios.get(position);

        //holder.mPlay.setImageResource(R.drawable.play);
        holder.mTitle.setText(audio.title);
        holder.mArtist.setText(audio.artist);

        holder.mTime.setText(String.format(Locale.ENGLISH, "%d:%d", audio.duration / 60, audio.duration % 60));
        MainActivity activity = (MainActivity) context;
        /*holder.mPlay.setOnClickListener(view -> {
            try {
                if (activity.vkAudios.size() == 0 || activity.musicService.getPlayingSong().getId() != audio.getId()) {
                    activity.vkAudios.clear();
                    activity.vkAudios.add(audio);
                    activity.musicService.setSong(0);
                    activity.musicService.playSong();
                    holder.mPlay.setImageResource(R.drawable.pause);
                } else {
                    if (activity.musicService.isPlaying()) {
                        activity.musicService.pause();
                        holder.mPlay.setImageResource(R.drawable.play);
                    } else {
                        activity.musicService.resume();
                        holder.mPlay.setImageResource(R.drawable.pause);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });*/
        /*try {
            if (audio.getId() == activity.musicService.getPlayingSong().getId()) {
                holder.mPlay.setImageResource(R.drawable.pause);
            } else {
                holder.mPlay.setImageResource(R.drawable.play);
            }
        } catch (Exception e) {

        }*/
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTime;
        private TextView mTitle;
        private TextView mArtist;
        private ImageButton mPlay;

        public ViewHolder(View itemView) {
            super(itemView);
            mTime = (TextView) itemView.findViewById(R.id.mTime);
            mTitle = (TextView) itemView.findViewById(R.id.mTitle);
            mArtist = (TextView) itemView.findViewById(R.id.mArtist);
            mPlay = (ImageButton) itemView.findViewById(R.id.mPlay);
        }
    }
}
