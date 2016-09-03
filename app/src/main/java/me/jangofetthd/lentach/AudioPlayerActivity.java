package me.jangofetthd.lentach;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.Locale;

import me.jangofetthd.lentach.Services.MusicService;

/**
 * Created by JangoFettHD on 03.09.2016.
 */
public class AudioPlayerActivity  extends AppCompatActivity
        /*implements NavigationView.OnNavigationItemSelectedListener*/  {

    NavigationView navigationView;
    ImageView apAlbumImage;
    SeekBar apSeekBar;
    TextView apTitle, apArtist, apCurrentTime, apDuration;
    ImageButton apPlay, apPrev, apNext;

    MusicService service;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audioplayer);

        service = MainActivity.instance.musicService;

        /*navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        apAlbumImage = (ImageView) findViewById(R.id.apImage);
        apSeekBar = (SeekBar) findViewById(R.id.apSeekBar);
        apTitle = (TextView) findViewById(R.id.apTitle);
        apArtist = (TextView) findViewById(R.id.apArtist);
        apDuration = (TextView) findViewById(R.id.apTime);
        apCurrentTime = (TextView) findViewById(R.id.apTimeCurrent);
        apPlay = (ImageButton) findViewById(R.id.apPlay);
        apPrev = (ImageButton) findViewById(R.id.apPrev);
        apNext = (ImageButton) findViewById(R.id.apNext);

        apPlay.setOnClickListener(view -> {
            if (service.isPlaying()) {
                service.pause();
                setPlayStatus(false);
            } else {
                service.resume();
                setPlayStatus(true);
            }
        });

        VKApiAudio song = service.getPlayingSong();
        if (song.getId() != 0){
            setInterface(song);
        }

        apDuration.setText(String.format(Locale.ENGLISH, "%d:%d", song.duration/60, song.duration%60));

        apSeekBar.setOnTouchListener((view, motionEvent) -> {
            service.setProgress(apSeekBar.getProgress(), true);
            return false;
        });

        service.setOnSchelduedUpdatesHandler((songPosition, songDuration) -> {
            apCurrentTime.setText(String.format(Locale.ENGLISH, "%d:%d", songPosition/60, songPosition%60));
            int pos = (int)((float)songPosition/(float)songDuration*100);
            apSeekBar.setProgress(pos);
        });
    }

    private void setInterface(VKApiAudio song) {
        apTitle.setText(song.title);
        apArtist.setText(song.artist);
        setPlayStatus(service.isPlaying());
    }

    private void setPlayStatus(boolean playing) {
        if (playing) {
            apPlay.setImageResource(R.drawable.pause);
        } else {
            apPlay.setImageResource(R.drawable.play);
        }
    }

    /*
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
    */
}
