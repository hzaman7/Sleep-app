package com.example.sleepapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SoundsActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private int currentResId = R.raw.white_noise;
    private boolean isMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sounds);


        Button btnWhite = findViewById(R.id.btn_white_noise);
        Button btnOcean = findViewById(R.id.btn_ocean_waves);
        Button btnRain = findViewById(R.id.btn_rain);

        btnWhite.setOnClickListener(v -> selectTrack(R.raw.white_noise, getString(R.string.white_noise)));
        btnOcean.setOnClickListener(v -> selectTrack(R.raw.ocean_waves, getString(R.string.ocean_waves)));
        btnRain.setOnClickListener(v -> selectTrack(R.raw.rain, getString(R.string.rain)));


        ImageButton play = findViewById(R.id.btn_play);
        ImageButton pause = findViewById(R.id.btn_pause);
        ImageButton stop = findViewById(R.id.btn_stop);
        ImageButton mute = findViewById(R.id.btn_mute);
        ImageButton unmute = findViewById(R.id.btn_unmute);

        play.setOnClickListener(v -> {
            if (mediaPlayer == null) initPlayer();
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();

                mediaPlayer.setVolume(isMuted ? 0.0f : 1.0f, isMuted ? 0.0f : 1.0f);
                Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
            }
        });

        pause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
            }
        });

        stop.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
                isMuted = false; // Reset mute state on stop
                Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
            }
        });

        mute.setOnClickListener(v -> {
            if (mediaPlayer != null && !isMuted) {
                mediaPlayer.setVolume(0.0f, 0.0f);
                isMuted = true;
                Toast.makeText(this, "Muted", Toast.LENGTH_SHORT).show();
            }
        });

        unmute.setOnClickListener(v -> {
            if (mediaPlayer != null && isMuted) {
                mediaPlayer.setVolume(1.0f, 1.0f);
                isMuted = false;
                Toast.makeText(this, "Unmuted", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        findViewById(R.id.nav_alarm).setOnClickListener(v ->
                startActivity(new Intent(this, AlarmActivity.class)));

        findViewById(R.id.nav_sounds).setOnClickListener(v ->
                startActivity(new Intent(this, SoundsActivity.class)));

        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));;

        findViewById(R.id.nav_report).setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class)));

        findViewById(R.id.nav_sleepHistory).setOnClickListener(v ->
                startActivity(new Intent(this, SavedAlarmsActivity.class)));
    }

    private void selectTrack(int resId, String name) {
        currentResId = resId;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        initPlayer();
        mediaPlayer.start();
        mediaPlayer.setVolume(isMuted ? 0.0f : 1.0f, isMuted ? 0.0f : 1.0f);
        Toast.makeText(this, name + " selected and playing", Toast.LENGTH_SHORT).show();
    }

    private void initPlayer() {
        mediaPlayer = MediaPlayer.create(this, currentResId);
        mediaPlayer.setLooping(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}