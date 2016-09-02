package me.jangofetthd.lentach.Services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.vk.sdk.api.model.VKApiAudio;

import java.io.IOException;
import java.util.ArrayList;

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


    private TimeSynchronizationHandler timeHandler;

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
        if (timeHandler != null){
            timeHandler.OnTimePlayerSynchronizationHandler(player.getCurrentPosition()/1000, player.getDuration()/1000);
        }
        player.start();
    }

    public VKApiAudio getPlayingSong() {
        return songs.get(songPos);
    }

    public void setProgress(int progress) {
        if (player.isPlaying()) {
            player.seekTo(progress*1000);
        }
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

    public void setTimeSynchronizationHandler(TimeSynchronizationHandler timeHandler) {
        this.timeHandler = timeHandler;
    }

    //----------------------------

    interface TimeSynchronizationHandler {
        void OnTimePlayerSynchronizationHandler(int songTimePosition, int songDuration);
    }
}
