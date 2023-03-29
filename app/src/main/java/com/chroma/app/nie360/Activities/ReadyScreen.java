package com.chroma.app.nie360.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.chroma.app.nie360.BaseClasses.BaseActivity;
import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.CustomTextView;
import com.chroma.app.nie360.Utils.TinyDB;
import com.squareup.picasso.Picasso;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class ReadyScreen extends BaseActivity {
    CustomTextView tvImage, tvNext, tvVideo;
    VideoView scalableVideoView;
    private final static int IMAGE_PICKER_SELECT = 5505;
    TinyDB tinyDB;
    ImageView iv, ivAdminPenal, ivProcessingVideos;
    private static final int PERMISSION_REQUEST_CODE = 1;
    String p1 = Manifest.permission.CAMERA, p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE, p3 = Manifest.permission.READ_EXTERNAL_STORAGE, p4 = Manifest.permission.RECORD_AUDIO;
    String typ = "";
    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        init();
        clickListeners();
    }


    private void init() {
        tinyDB = new TinyDB(ReadyScreen.this);
        ivProcessingVideos = findViewById(R.id.ivProcessingVideos);
        if (tinyDB.getString(Constants.AudioType).isEmpty()) {
            tinyDB.putString(Constants.AudioType, "");
        }

        tvImage = findViewById(R.id.tvImage);
        ivAdminPenal = findViewById(R.id.ivAdminPenal);
        scalableVideoView = findViewById(R.id.video_view);
        iv = findViewById(R.id.iv);

        tvVideo = findViewById(R.id.tvVideo);
        if (!(tinyDB.getString(Constants.SplashBackgroundType).equals(""))) {
            if (tinyDB.getString(Constants.SplashBackgroundType).equalsIgnoreCase("image")) {
                scalableVideoView.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
                Picasso.get().load(tinyDB.getString(Constants.SplashBackground)).into(iv);

            } else {
                iv.setVisibility(View.GONE);
                scalableVideoView.setVisibility(View.VISIBLE);
                try {
                    scalableVideoView.setVideoURI(Uri.parse(tinyDB.getString(Constants.SplashBackground)));
                    scalableVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            scalableVideoView.start();
                            mp.setLooping(true);
                            mp.setVolume(0, 0);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        tvNext = findViewById(R.id.tvNext);
        if (firstTime) {
            ArrayList<ProcessingQueueModel> list;
            list = tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getStatus().equals(Constants.StatusProcessing) || list.get(i).getStatus().equals(Constants.StatusAdded)) {
                    ProcessingQueueModel model = new ProcessingQueueModel();
                    model.setVideo1(list.get(i).getVideo1());
                    model.setVideo2(list.get(i).getVideo2());
                    model.setStatus(Constants.StatusFinished);
                    if (list.get(i).getOutputPath().isEmpty())
                        model.setOutputPath("empty");
                    list.set(i, model);
                }
            }
            tinyDB.putProcessingListObject(Constants.ProcessingList, list);


            ArrayList<UploadingQueueModel> list1;
            list1 = tinyDB.getUploadingListObject(Constants.UploadingList, UploadingQueueModel.class);
            for (int i = 0; i < list1.size(); i++) {
                if (list.get(i).getStatus().equals("Processing")) {
                    UploadingQueueModel model = new UploadingQueueModel();
                    model.setStatus("Failed");
                    list1.set(i, model);
                }
            }
            tinyDB.putUploadingListObject(Constants.UploadingList, list1);
        }
    }

    private void clickListeners() {

        tvImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    typ = "image";
                    permissionAccess();
                }
            }
        });

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typ = "next";
                permissions();
            }
        });

        tvVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    typ = "video";
                    permissionAccessVideo();
                }
            }
        });

        ivAdminPenal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ReadyScreen.this, AdminPenal.class);
                startActivity(i);
            }
        });
        ivProcessingVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ReadyScreen.this, ProcessingActivity.class);
                startActivity(i);
            }
        });
        firstTime = false;

    }

    private void permissionAccessVideo() {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1);
            requestPermission(p1);
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2);
            requestPermission(p2);
        } else if (!checkPermission(p3)) {
            requestPermission(p3);
        } else if (!checkPermission(p4)) {
            requestPermission(p4);
        } else {
            Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("video/*");
            startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
//            Toast.makeText(CreateJob.this, "All permission granted", Toast.LENGTH_LONG).show();
        }
    }

    private void permissions() {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1);
            requestPermission(p1);
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2);
            requestPermission(p2);
        } else if (!checkPermission(p3)) {
            requestPermission(p3);
        } else if (!checkPermission(p4)) {
            requestPermission(p4);
        } else {
            Intent i = new Intent(ReadyScreen.this, CaptureVideoScreen.class);
            startActivity(i);
        }
    }

    private void permissionAccess() {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1);
            requestPermission(p1);
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2);
            requestPermission(p2);
        } else if (!checkPermission(p3)) {
            requestPermission(p3);
        } else if (!checkPermission(p4)) {
            requestPermission(p4);
        } else {
            Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
//            Toast.makeText(CreateJob.this, "All permission granted", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(ReadyScreen.this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(String permission) {

        if (ContextCompat.checkSelfPermission(ReadyScreen.this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReadyScreen.this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            //Do the stuff that requires permission...
            Log.e("TAG", "Not say request");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                Log.e("TAG", "val " + grantResults[0]);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (typ.equalsIgnoreCase("video")) {
                        permissionAccessVideo();
                    } else if (typ.equalsIgnoreCase("next")) {
                        permissions();
                    } else {
                        permissionAccess();
                    }
                } else {
                    Toast.makeText(ReadyScreen.this, "The app was not allowed permission. Hence, it cannot function properly. Please consider granting it this permission.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Uri selectedMediaUri = data.getData();

            if (selectedMediaUri.toString().contains("image")) {
                //handle image
                tinyDB.putString(Constants.SplashBackground, selectedMediaUri + "");
                tinyDB.putString(Constants.SplashBackgroundType, "image");
                scalableVideoView.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
                Picasso.get().load(selectedMediaUri).into(iv);


            } else if (selectedMediaUri.toString().contains("video")) {
                //handle video
                try {
                    tinyDB.putString(Constants.SplashBackground, selectedMediaUri + "");
                    tinyDB.putString(Constants.SplashBackgroundType, "video");
                    iv.setVisibility(View.GONE);
                    scalableVideoView.setVisibility(View.VISIBLE);
                    scalableVideoView.setVideoURI(selectedMediaUri);
                    scalableVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            scalableVideoView.start();
                            mp.setLooping(true);
                            mp.setVolume(0, 0);
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scalableVideoView != null && iv != null) {
            if (!(tinyDB.getString(Constants.SplashBackgroundType).equals(""))) {
                if (tinyDB.getString(Constants.SplashBackgroundType).equalsIgnoreCase("image")) {
                    scalableVideoView.setVisibility(View.GONE);
                    iv.setVisibility(View.VISIBLE);
                    Picasso.get().load(tinyDB.getString(Constants.SplashBackground)).into(iv);
//                Glide.with(ReadyScreen.this).load(tinyDB.getString(Constants.SplashBackground)).into(iv);

                } else {
                    iv.setVisibility(View.GONE);
                    scalableVideoView.setVisibility(View.VISIBLE);
                    try {
                        scalableVideoView.setVideoURI(Uri.parse(tinyDB.getString(Constants.SplashBackground)));
                        scalableVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                scalableVideoView.start();
                                mp.setLooping(true);
                                mp.setVolume(0, 0);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
