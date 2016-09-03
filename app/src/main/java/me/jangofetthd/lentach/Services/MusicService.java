package me.jangofetthd.lentach.Services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vk.sdk.api.model.VKApiAudio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by JangoFettHD on 02.09.2016.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    /**
     * Media player
     */
    private MediaPlayer player;
    /**
     * List of VK songs
     */
    private ArrayList<VKApiAudio> songs;
    /**
     * Current position
     */
    private int songPos;
    private final IBinder musicBind = new MusicBinder();
    private AudioChangedHandler audioChangedHandler;

    //---------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        songPos = 0;
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        resume();
    }

    //-----------------------------------

    public void playSong() {
        player.reset();
        VKApiAudio playSong = songs.get(songPos);
        try {
            player.setDataSource(playSong.url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
    }

    public void setSong(int songPosition) {
        this.songPos = songPosition;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void pause() {
        if (player.isPlaying())
            player.pause();
    }

    public void resume() {
        player.start();
    }

    public void next() {
        audioChangedHandler.OnAudioChangedHandler(songs.get(songPos), songs.get(++songPos));
        if (songPos >= songs.size())
            songPos = 0;
        playSong();
    }

    public void prev() {
        audioChangedHandler.OnAudioChangedHandler(songs.get(songPos), songs.get(--songPos));
        if (songPos < 0)
            songPos = songs.size() - 1;
        playSong();
    }

    public VKApiAudio getPlayingSong() {
        return songs.get(songPos);
    }

    public void setProgress(int progress, boolean percents) {
        if (player.isPlaying() && progress >= 0) {
            if (percents && progress <= 100)
                player.seekTo(player.getDuration() * progress / 100);
            else
                player.seekTo(progress * 1000);
        }
    }

    public int getAudioDuration() {
        if (player.isPlaying()) {
            return player.getDuration() / 1000;
        }
        return -1;
    }

    public int getAudioPosition() {
        if (player.isPlaying()) {
            return player.getCurrentPosition() / 1000;
        }
        return -1;
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<VKApiAudio> songs) {
        this.songs = songs;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void setAudioChangedHandler(AudioChangedHandler handler) {
        this.audioChangedHandler = handler;
    }

    //----------------------------

    public interface AudioChangedHandler {
        void OnAudioChangedHandler(VKApiAudio oldAudio, VKApiAudio newAudio);
    }
}
