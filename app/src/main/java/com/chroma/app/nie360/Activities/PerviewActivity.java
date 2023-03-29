package com.chroma.app.nie360.Activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.chroma.app.nie360.BaseClasses.BaseActivity;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.TinyDB;

import java.io.File;

public class PerviewActivity extends BaseActivity {
    TinyDB tinyDB;
    ImageView ivAdminPenal, button_confirm, button_cancel;
    VideoView video_view;
    String vid_url = "";
    ImageView play, pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perview);
        getSupportActionBar().hide();
        if (getIntent() != null) {
            vid_url = getIntent().getStringExtra("vid_url");
            Log.e("vid_url", vid_url + "");
        }
        init();
        clickListeners();
    }

    private void init() {
        tinyDB = new TinyDB(PerviewActivity.this);
        ivAdminPenal = findViewById(R.id.ivAdminPenal);
        button_confirm = findViewById(R.id.button_confirm);
        button_cancel = findViewById(R.id.button_cancel);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        video_view = findViewById(R.id.vidu);

        File f = new File(vid_url);

        if (vid_url != null)
            try {
                video_view.setVideoURI(Uri.parse(vid_url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                video_view.start();
                mediaPlayer.setLooping(true);
            }
        });
        ivAdminPenal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PerviewActivity.this, AdminPenal.class);
                startActivity(i);
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (video_view.isPlaying()) {
                    video_view.pause();
                    pause.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                } else {
                    video_view.start();
                    pause.setVisibility(View.VISIBLE);
                    play.setVisibility(View.GONE);
                }
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!video_view.isPlaying()) {
                    video_view.start();
                    play.setVisibility(View.GONE);
                    pause.setVisibility(View.VISIBLE);
                } else {
                    video_view.pause();
                    play.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.GONE);
                }
            }
        });
        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PerviewActivity.this, PendingVideos.class);
                i.putExtra("vid_url", vid_url);
                startActivity(i);
                finish();
            }
        });
    }

    private void clickListeners() {
        ivAdminPenal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PerviewActivity.this, AdminPenal.class);
                startActivity(i);
            }
        });
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PerviewActivity.this, ReadyScreen.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finishAffinity();
            }
        });
    }
}